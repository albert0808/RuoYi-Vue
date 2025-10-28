package com.albert.learning.mq;

import com.rabbitmq.client.*;

public class RabbitMQImpl implements MessageQueue {
    private Connection connection;
    private Channel channel;

    public RabbitMQImpl(String host) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    @Override
    public void send(String topic, String message) throws Exception {
        channel.queueDeclare(topic, true, false, false, null);
        channel.basicPublish("", topic, null, message.getBytes());
        System.out.println("RabbitMQ Sent: " + message);
    }

    @Override
    public void receive(String topic, MessageHandler handler) throws Exception {
        channel.queueDeclare(topic, true, false, false, null);
        channel.basicConsume(topic, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                handler.onMessage(new String(body));
            }
        });
    }
}

