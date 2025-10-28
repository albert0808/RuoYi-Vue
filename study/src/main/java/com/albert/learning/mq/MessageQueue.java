package com.albert.learning.mq;

public interface MessageQueue {
    void send(String topic, String message) throws Exception;
    void receive(String topic, MessageHandler handler) throws Exception;

    interface MessageHandler {
        void onMessage(String message);
    }
}

