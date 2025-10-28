package com.albert.learning.mq;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaMQImpl implements MessageQueue {

    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;

    public KafkaMQImpl(String bootstrapServers, String groupId) {
        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", bootstrapServers);
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(producerProps);

        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", bootstrapServers);
        consumerProps.put("group.id", groupId);
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("auto.offset.reset", "earliest");
        consumer = new KafkaConsumer<>(consumerProps);
    }

    @Override
    public void send(String topic, String message) {
        producer.send(new ProducerRecord<>(topic, message), (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Kafka Sent: " + message);
            } else {
                exception.printStackTrace();
            }
        });
    }

    @Override
    public void receive(String topic, MessageHandler handler) {
        consumer.subscribe(Collections.singletonList(topic));
        new Thread(() -> {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    handler.onMessage(record.value());
                }
            }
        }).start();
    }
}

