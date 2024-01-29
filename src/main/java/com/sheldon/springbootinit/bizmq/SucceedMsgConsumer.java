package com.sheldon.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.sheldon.springbootinit.service.ChartInfoService;
import com.sheldon.springbootinit.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @ClassName MyMessageProducer
 * @Author 26483
 * @Date 2024/1/26 16:48
 * @Version 1.0
 * @Description TODO
 */
@Component
@Slf4j
public class SucceedMsgConsumer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private ChartService chartService;

    @Resource
    private ChartInfoService chartInfoService;

    @RabbitListener(queues = {BiMqConstant.QUEUE_SUCCEED_NAME}, ackMode = "MANUAL")
    public void sendMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) {
        log.info("接收分析成功图表信息：{}", message);
        try {
            channel.basicAck(deliveryTag, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /*try {

        } catch (Exception e) {
            log.error("消息确认失败：{}", e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("消息拒绝失败：{}", ex.getMessage());
            }
        }*/
    }

}
