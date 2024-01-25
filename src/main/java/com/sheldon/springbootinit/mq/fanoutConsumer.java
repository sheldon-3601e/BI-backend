package com.sheldon.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class fanoutConsumer {
  private static final String EXCHANGE_NAME = "fanout_exchange";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection1 = factory.newConnection();
    Connection connection2 = factory.newConnection();
    Channel channel1 = connection1.createChannel();
    Channel channel2 = connection2.createChannel();

    // 声明交换机
    channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");
    // 声明队列
    String queueName1 = "queue_1";
    channel1.queueDeclare(queueName1, false, false, false, null);
    channel1.queueBind(queueName1, EXCHANGE_NAME, "");

    String queueName2 = "queue_2";
    channel2.queueDeclare(queueName2, false, false, false, null);
    channel2.queueBind(queueName2, EXCHANGE_NAME, "");

    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

    DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), "UTF-8");
        System.out.println(" [x] queue_1 Received '" + message + "'");
    };
    DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
      String message = new String(delivery.getBody(), "UTF-8");
      System.out.println(" [x] queue_2 Received '" + message + "'");
    };

    channel1.basicConsume(queueName1, true, deliverCallback1, consumerTag -> { });
    channel2.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
  }
}