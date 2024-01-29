package com.sheldon.springbootinit.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
public class MsgProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendWaitingMsg(String message) {
        rabbitTemplate.convertAndSend(BiMqConstant.EXCHANGE_WAITING_NAME, BiMqConstant.ROUTING_KEY_WAITING, message);
    }

}
