package com.cora.fastbi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.junit.jupiter.api.Test;


public class MqTest {

    private final static String QUEUE_NAME = "hello";

    @Test
    public void testSingleProducer() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("106.54.237.17");
        factory.setPort(15672);
        factory.setUsername("cora");
        factory.setPassword("ysmhdouism123");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Hello World!";
            String message2 = "Hello World2!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            channel.basicPublish("", QUEUE_NAME, null, message2.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
            System.out.println(" [x] Sent '" + message2 + "'");
        }
    }

    @Test
    public void testSingleConsumer() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("106.54.237.17");
        factory.setPort(15672);
        factory.setUsername("cora");
        factory.setPassword("ysmhdouism123");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
