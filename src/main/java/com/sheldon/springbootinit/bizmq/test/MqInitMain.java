package com.sheldon.springbootinit.bizmq.test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @ClassName MqInitMain
 * @Author 26483
 * @Date 2024/1/26 16:57
 * @Version 1.0
 * @Description 用于创建程序中需要用到的MQ交换机、队列、绑定关系，只需要执行一次
 */
@Slf4j
public class MqInitMain {
    private static final String EXCHANGE_NAME = "code_test_exchange";

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 声明队列
            String queueName = "code_test_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME, "my_routingKey");
        } catch (Exception e) {
            log.error("MQ初始化失败", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    log.error("MQ连接关闭失败", e);
                }
            }
        }

    }
}
