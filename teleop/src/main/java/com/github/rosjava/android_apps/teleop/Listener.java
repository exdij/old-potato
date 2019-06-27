package com.github.rosjava.android_apps.teleop;

import org.apache.commons.logging.Log;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

import std_msgs.String;

public class Listener extends AbstractNodeMain {
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rosjava/listener");
    }

    @Override
    public void onStart(ConnectedNode connectedNode){
        final Log log = connectedNode.getLog();
        Subscriber<std_msgs.String> subscriber = connectedNode.newSubscriber("RaspberryControlWriter", String._TYPE);
        subscriber.addMessageListener(new MessageListener<String>() {
            @Override
            public void onNewMessage(std_msgs.String message) {
                java.lang.String test = message.getData();
                log.info(message.getData());

            }
        });
    }
}
