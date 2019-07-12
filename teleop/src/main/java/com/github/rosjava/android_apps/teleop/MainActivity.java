/*
 * Copyright (C) 2013 OSRF.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.github.rosjava.android_apps.teleop;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import java.lang.*;

import com.github.rosjava.android_remocons.common_tools.apps.RosAppActivity;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.view.RosImageView;
import org.ros.android.view.VirtualJoystickView;
import org.ros.namespace.NameResolver;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import org.ros.message.MessageListener;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class MainActivity extends RosAppActivity {
	//private RosImageView<sensor_msgs.CompressedImage> cameraView;
	private VirtualJoystickView virtualJoystickView;
	private Button backButton;
	private Switch switchButton;
	private EditText number;
	private Talker talker;
	private Listener listener;
	private JoyStickClass js;
	private Button btnOpen;

	Bitmap bmpStop;
	Bitmap bmpStart;
	Bitmap bmpFall;
	Bitmap bmpObstacle;

	private int x;
	private int y;
    ImageView image;
	TextView textView2;
	private ResponseInterpreter listenerResopnse;
	private WebView mWebRTCWebView;
	private String uri;
	private boolean WebRTCStarted;
	public MainActivity() {
		// The RosActivity constructor configures the notification title and ticker messages.
		super("android teleop", "android teleop");
	}

	@SuppressLint("ClickableViewAccessibility")
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {


		setDashboardResource(R.id.top_bar);
		setMainWindowResource(R.layout.main);
		super.onCreate(savedInstanceState);

		WebRTCStarted = FALSE;
		talker = new Talker();
		listener = new Listener();
		textView2 = findViewById(R.id.textView2);
		//number = findViewById(R.id.editNumber);
        switchButton = findViewById(R.id.switch_start);
		image =  (ImageView) findViewById(R.id.imageView);

		btnOpen = (Button) findViewById(R.id.button);
		btnOpen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(WebRTCStarted == TRUE){
					return;
				}else{
					WebRTCStarted = TRUE;
				}
				mWebRTCWebView.loadUrl(uri);


				mWebRTCWebView.setWebChromeClient(new WebChromeClient() {
					@Override
					public void onPermissionRequest(final PermissionRequest request) {
						//Log.d(TAG, "onPermissionRequest");
						runOnUiThread(new Runnable() {

							@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
							@Override
							public void run() {
								if(request.getOrigin().toString().equals(uri)) {
									request.grant(request.getResources());
								} else {
									request.grant(request.getResources());
								}
							}
						});
					}

				});
			}
		});

		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
			// request runtime permissions
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.CAMERA,Manifest.permission.CHANGE_NETWORK_STATE,Manifest.permission.MODIFY_AUDIO_SETTINGS,Manifest.permission.RECORD_AUDIO,Manifest.permission.BLUETOOTH,Manifest.permission.INTERNET,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_NETWORK_STATE},
					100);
		}

		mWebRTCWebView = (WebView)  findViewById(R.id.webView1);

		setUpWebViewDefaults(mWebRTCWebView);

		bmpFall =  BitmapFactory.decodeResource(getApplicationContext().getResources(),
				R.drawable.fall);
		bmpObstacle = BitmapFactory.decodeResource(getApplicationContext().getResources(),
				R.drawable.obstacle);

		bmpStop =  BitmapFactory.decodeResource(getApplicationContext().getResources(),
				R.drawable.warning);

		bmpStart = BitmapFactory.decodeResource(getApplicationContext().getResources(),
				R.drawable.greenlight);
		LinearLayout layoutJoystick = findViewById(R.id.layout_joystick);
        js = new JoyStickClass(getApplicationContext(), layoutJoystick, R.drawable.image_button);
        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(50);
		layoutJoystick.setOnTouchListener(new View.OnTouchListener() {
				public boolean onTouch(View arg0, MotionEvent arg1) {
					js.drawStick(arg1);
					if(talker.CheckIFStarted()) {
						if (arg1.getAction() == MotionEvent.ACTION_DOWN || arg1.getAction() == MotionEvent.ACTION_MOVE) {
							x = js.getX() / 2;
							x = x + 128;
							if (x > 255) {
								x = 255;
								//if(x>254)
								//	x=254;
							} else if (x < 0) {
								x = 0;
								//x = x*-1;
								//if(x>127){
								//	x = 127;
							}

							x = 5000 + x;

							y = js.getY() / -2;
							y = y + 128;
							if (y < 0) {
								y = 0;
								//if(y>254)
								//	y=254;
							} else if (y > 255) {
								//y = y*-1;
								//if(y>127){
								y = 255;
							}

							y = 4000 + y;
							talker.sendMessage(x, y);

							textView2.setText("X : " + x + "Y : " + y);

						} else if (arg1.getAction() == MotionEvent.ACTION_UP) {
							talker.sendMessage(5128, 4128);
							textView2.setText("X : " + x + "Y : " + y);
						}
						
					}
				return true;
				}
		});

        //cameraView = (RosImageView<sensor_msgs.CompressedImage>) findViewById(R.id.image);
        //cameraView.setMessageType(sensor_msgs.CompressedImage._TYPE);
        //cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());
        backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


		switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					talker.sendStart();
				} else {
					talker.sendStop();
				}
			}
		});
		final Handler handler = new Handler();
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				boolean obstacleDetected = listener.obstacleDetected;
				listenerResopnse = listener.response;
				if (listenerResopnse == null){
					handler.postDelayed(this,500);
					return;
				}
				Bitmap icon;
				if(listenerResopnse.Started == TRUE && listenerResopnse.NeedToRefresh){
					icon = bmpStart;
				}else if(listenerResopnse.NoFloorDetected && listenerResopnse.NeedToRefresh){
					icon = bmpFall;
				}else if(listenerResopnse.ObstacleDetected && listenerResopnse.NeedToRefresh){
					icon = bmpObstacle;
				}else {
					icon =bmpStop;
				}
				if(listenerResopnse.NeedToRefresh){
					image.setImageBitmap(icon);
					listenerResopnse.NeedToRefresh = FALSE;
				}

				handler.postDelayed(this,500);
			}
		};
		Thread t = new Thread(){
			@Override
			public void run() {
			try{
				while (true){
					 sleep(1000);
					 handler.post(r);
				}

			}catch(InterruptedException e){e.printStackTrace();}
			}
		};
		t.start();
	}

	@Override
	protected void init(NodeMainExecutor nodeMainExecutor) {

		super.init(nodeMainExecutor);

        try {
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();
            NodeConfiguration nodeConfiguration =
                    NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());
			nodeConfiguration.setNodeName("talker");
			nodeMainExecutor.execute(talker,nodeConfiguration);
			nodeConfiguration.setNodeName("listener");
			nodeMainExecutor.execute(listener,nodeConfiguration);
        String joyTopic = remaps.get(getString(R.string.joystick_topic));
        String camTopic = remaps.get(getString(R.string.camera_topic));

        NameResolver appNameSpace = getMasterNameSpace();



       uri =  "https://" + getMasterUri().getHost() + ":8090/stream/webrtc";

      /*  joyTopic = appNameSpace.resolve(joyTopic).toString();
        camTopic = appNameSpace.resolve(camTopic).toString();

		cameraView.setTopicName(camTopic);
        virtualJoystickView.setTopicName(joyTopic);
		
		nodeMainExecutor.execute(cameraView, nodeConfiguration
				.setNodeName("android/camera_view"));
		nodeMainExecutor.execute(virtualJoystickView,
				nodeConfiguration.setNodeName("android/virtual_joystick")); */
        }catch (IOException e) {
            // Socket problem
        }

	}
	
	  @Override
	  public boolean onCreateOptionsMenu(Menu menu){
		  menu.add(0,0,0,R.string.stop_app);

		  return super.onCreateOptionsMenu(menu);
	  }
	  
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item){
		  super.onOptionsItemSelected(item);
		  switch (item.getItemId()){
		  case 0:
			  onDestroy();
			  break;
		  }
		  return true;
	  }

	private void setUpWebViewDefaults(WebView webView) {
		WebSettings settings = webView.getSettings();

		// Enable Javascript
		settings.setJavaScriptEnabled(true);

		// Use WideViewport and Zoom out if there is no viewport defined
		settings.setUseWideViewPort(true);
		settings.setLoadWithOverviewMode(true);

		// Enable pinch to zoom without the zoom buttons
		settings.setBuiltInZoomControls(true);

		// Allow use of Local Storage
		settings.setDomStorageEnabled(true);

		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			// Hide the zoom controls for HONEYCOMB+
			settings.setDisplayZoomControls(false);
		}

		// Enable remote debugging via chrome://inspect
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}

		webView.setWebViewClient(new WebViewClient(){
			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				handler.proceed(); // Ignore SSL certificate errors
			}
		});

		// AppRTC requires third party cookies to work
		CookieManager cookieManager = CookieManager.getInstance();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			cookieManager.setAcceptThirdPartyCookies(mWebRTCWebView, true);
		}
	}
}

/*
mWebRTCWebView.loadUrl(uri);


			mWebRTCWebView.setWebChromeClient(new WebChromeClient() {
				@Override
				public void onPermissionRequest(final PermissionRequest request) {
					//Log.d(TAG, "onPermissionRequest");
					runOnUiThread(new Runnable() {

						@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
						@Override
						public void run() {
							if(request.getOrigin().toString().equals(uri)) {
								request.grant(request.getResources());
							} else {
								request.grant(request.getResources());
							}
						}
					});
				}

			});
 */