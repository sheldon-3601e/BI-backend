package com.sheldon.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MqInitMain
 * @Author 26483
 * @Date 2024/1/26 16:57
 * @Version 1.0
 * @Description 用于创建程序中需要用到的MQ交换机、队列、绑定关系，只需要执行一次
 */
@Slf4j
public class BiInitMain {

    public static void biInit() {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // 声明交换机
            // 声明等待任务交换机
            channel.exchangeDeclare(BiMqConstant.EXCHANGE_WAITING_NAME, BiMqConstant.EXCHANGE_TYPE,true);
            // 声明失败任务交换机，即死信交换机
            channel.exchangeDeclare(BiMqConstant.EXCHANGE_FAILED_NAME, BiMqConstant.EXCHANGE_TYPE,true);
            log.info("MQ 交换机声明完成");
            // 声明死信任务队列
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("x-dead-letter-exchange", BiMqConstant.EXCHANGE_FAILED_NAME);
            args.put("x-dead-letter-routing-key", BiMqConstant.ROUTING_KEY_FAILED);
            channel.queueDeclare(BiMqConstant.QUEUE_WAITING_NAME, true, false, false, args);
            // 绑定等待任务交换机
            channel.queueBind(BiMqConstant.QUEUE_WAITING_NAME, BiMqConstant.EXCHANGE_WAITING_NAME, BiMqConstant.ROUTING_KEY_WAITING);
            log.info("声明等待任务队列完成");

            // 声明失败任务队列
            channel.queueDeclare(BiMqConstant.QUEUE_FAILED_NAME, true, false, false, null);
            // 绑定死信交换机
            channel.queueBind(BiMqConstant.QUEUE_FAILED_NAME, BiMqConstant.EXCHANGE_FAILED_NAME, BiMqConstant.ROUTING_KEY_FAILED);
            log.info("声明失败任务队列完成");

            // 声明成功任务队列
            channel.queueDeclare(BiMqConstant.QUEUE_SUCCEED_NAME, true, false, false, null);
            log.info("声明成功任务队列完成");

            log.info("MQ初始化成功");

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
