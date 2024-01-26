package com.sheldon.springbootinit.bizmq;

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
public class BiInitMain {

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(BiMqConstant.EXCHANGE_NAME, BiMqConstant.EXCHANGE_TYPE);

            // 声明队列
            channel.queueDeclare(BiMqConstant.QUEUE_NAME, true, false, false, null);
            channel.queueBind(BiMqConstant.QUEUE_NAME, BiMqConstant.EXCHANGE_NAME, BiMqConstant.ROUTING_KEY);
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
