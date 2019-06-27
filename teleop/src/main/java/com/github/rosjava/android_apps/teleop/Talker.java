package com.github.rosjava.android_apps.teleop;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

import std_msgs.Int16;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Talker extends AbstractNodeMain {
    public volatile short numX;
    public volatile short numY;
    public volatile boolean flaga;

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
                flaga = FALSE;
            }

            @Override
            protected void loop() throws InterruptedException {
                if(flaga) {
                    std_msgs.Int16 testX = publisher.newMessage();
                    testX.setData(numX);
                    publisher.publish(testX);
                    if(numY<25500){
                        std_msgs.Int16 testY = publisher.newMessage();
                        testY.setData(numY);
                        publisher.publish(testY);
                    }
                    sequenceNumber++;
                    flaga = FALSE;
                }
                Thread.sleep(100);
            }
        });
    }
    public void sendMessage(int msgX, int msgY){
        numX = (short) msgX;
        numY = (short) msgY;

        if(numX==numY){

        }
    }
}
