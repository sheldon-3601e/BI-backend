package com.sheldon.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

public class DlxConsumer {

    private static final String EXCHANGE_NAME = "direct2-exchange";
    private static final String DEAD_EXCHANGE_NAME = "dlx-direct2-exchange";


    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // 指定死信队列的参数
        Map<String, Object> args1 = new HashMap<String, Object>();
        // 指定要绑定的死信交换机
        args1.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        // 指定死信要转到到哪个死信队列
        args1.put("x-dead-letter-routing-key", "waibao");

        // 声明队列
        String queueName1 = "direct2_queue_1";
        channel.queueDeclare(queueName1, true, false, false, args1);
        channel.queueBind(queueName1, EXCHANGE_NAME, "info");

        Map<String, Object> args2 = new HashMap<String, Object>();
        args2.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        args2.put("x-dead-letter-routing-key", "laoban");

        String queueName2 = "direct2_queue_2";
        channel.queueDeclare(queueName2, true, false, false, args2);
        channel.queueBind(queueName2, EXCHANGE_NAME, "error");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] " + queueName1 + " Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            // 拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] " + queueName2 + "Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
        };

        channel.basicConsume(queueName1, false, deliverCallback1, consumerTag -> {
        });
        channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> {
        });
    }
}