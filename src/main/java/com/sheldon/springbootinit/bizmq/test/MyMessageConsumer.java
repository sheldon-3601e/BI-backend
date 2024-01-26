package com.sheldon.springbootinit.bizmq.test;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName MyMessageProducer
 * @Author 26483
 * @Date 2024/1/26 16:48
 * @Version 1.0
 * @Description TODO
 */
@Component
@Slf4j
public class MyMessageConsumer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = {"code_test_queue"}, ackMode = "MANUAL")
    public void sendMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) {
        log.info("接收到消息：{}", message);
        try {
            // 消息确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("消息确认失败：{}", e.getMessage());
        }
    }

}
