package com.albert.learning.mq;

public class MQTest {
    public static void main(String[] args) throws Exception {
        MessageQueue mq = new RocketMQImpl("producerGroup", "consumerGroup", "192.168.44.129:9876");
        // MessageQueue mq = new RabbitMQImpl("localhost");
        // MessageQueue mq = new KafkaMQImpl("localhost:9092", "group1");

        mq.receive("test-topic", message -> System.out.println("Received: " + message));
        mq.send("test-topic", "Hello MQ");
    }
}

