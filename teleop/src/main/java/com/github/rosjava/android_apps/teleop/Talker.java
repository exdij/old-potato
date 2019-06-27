package com.github.rosjava.android_apps.teleop;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import std_msgs.Int16;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Talker extends AbstractNodeMain {
    public volatile short numX;
    public volatile short numY;
    public volatile boolean start;
    public volatile boolean init;
    public volatile boolean sendOnce;

    public GraphName getDefaultNodeName(){
        return GraphName.of("rosjava/talker");
    }
    @Override
    public void onStart(final ConnectedNode connectedNode){
        final Publisher<std_msgs.Int16> publisher =
                connectedNode.newPublisher("RaspberryControlReader", Int16._TYPE);
        connectedNode.executeCancellableLoop(new CancellableLoop() {
            private int sequenceNumber;

            @Override
            protected void setup(){
                sequenceNumber = 0;
                numX = 0;
                numY = 0;
                start = FALSE;
            }

            @Override
            protected void loop() throws InterruptedException {
                    if (start) {
                        std_msgs.Int16 testX = publisher.newMessage();
                        testX.setData(numX);
                        publisher.publish(testX);
                        if (!init) {
                            std_msgs.Int16 testY = publisher.newMessage();
                            testY.setData(numY);
                            publisher.publish(testY);
                        } else
                            init = FALSE;
                        if(sendOnce)
                            start = FALSE;
                        sequenceNumber++;
                    }
                Thread.sleep(100);
            }
        });
    }
    public void sendMessage(int msgX, int msgY){
        numX = (short) msgX;
        numY = (short) msgY;

        if(msgX==2000){
            start = TRUE;
            init = TRUE;
            sendOnce = TRUE;
        } else if(msgX==1000){
            start = FALSE;
            init = TRUE;
            sendOnce = TRUE;
        } else
            start = TRUE;
            init = FALSE;
            sendOnce = FALSE;
    }
}
