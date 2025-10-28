package com.albert.learning.mq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.List;

public class RocketMQImpl implements MessageQueue {

    private DefaultMQProducer producer;
    private DefaultMQPushConsumer consumer;

    public RocketMQImpl(String producerGroup, String consumerGroup, String namesrvAddr) throws Exception {
        producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(namesrvAddr);
        producer.start();

        consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(namesrvAddr);
    }

    @Override
    public void send(String topic, String message) throws Exception {
        Message msg = new Message(topic, message.getBytes());
        SendResult sendResult = producer.send(msg);
        System.out.println("RocketMQ Sent: " + sendResult);
    }

    @Override
    public void receive(String topic, MessageHandler handler) throws Exception {
        consumer.subscribe(topic, "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            for (Message msg : msgs) {
                handler.onMessage(new String(msg.getBody()));
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
    }
}

