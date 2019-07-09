package com.github.rosjava.android_apps.teleop;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import std_msgs.String;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Listener extends AbstractNodeMain {
    public boolean obstacleDetected;
    public java.lang.String ReceivedText;
    private  ResponseInterpreter responseInterpreter;
    public  ResponseInterpreter response;
    Listener(){
        this.responseInterpreter = new ResponseInterpreter();
    }
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava/listener");
    }

    @Override
    public void onStart(ConnectedNode connectedNode){

        obstacleDetected = FALSE;
        response = new ResponseInterpreter();
        final Log log = connectedNode.getLog();
        Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("RaspberryControlWriter", String._TYPE);
        subscriber.addMessageListener(new MessageListener<String>() {
            @Override
            public void onNewMessage(std_msgs.String message) {
                java.lang.String test = message.getData();
                response.Analyze(test);
                /*if(test.charAt(0) == 'S'){
                    java.lang.String StateValue = test.substring(1);
                    if (((java.lang.String) StateValue).equalsIgnoreCase("1")==TRUE){
                        obstacleDetected = TRUE;
                    }
                }*/
                ReceivedText = test;
                log.info(message.getData());


            }
        });
    }
}
