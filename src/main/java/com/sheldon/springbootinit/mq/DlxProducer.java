package com.sheldon.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DlxProducer {

    private static final String EXCHANGE_NAME = "direct2-exchange";

    private static final String DEAD_EXCHANGE_NAME = "dlx-direct2-exchange";

    public static void main(String[] argv) throws Exception {
        Scanner scanner = new Scanner(System.in);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 声明死信交换机
            channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");

            // 声明死信队列
            String queueName1 = "laoban_dlx_queue";
            channel.queueDeclare(queueName1, false, false, false, null);
            channel.queueBind(queueName1, DEAD_EXCHANGE_NAME, "laoban");

            String queueName2 = "waibao_dlx_queue";
            channel.queueDeclare(queueName2, false, false, false, null);
            channel.queueBind(queueName2, DEAD_EXCHANGE_NAME, "waibao");

            while (scanner.hasNext()) {
                String userInput = scanner.nextLine();
                if (userInput.equals("exit")) {
                    break;
                }
                String[] split = userInput.split(" ");
                if (split.length != 2) {
                    System.out.println("Please input severity and message");
                    continue;
                }
                String severity = split[0];
                String message = split[1];

                channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + severity + "':'" + message + "'");
            }
        }
    }
}