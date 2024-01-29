package com.sheldon.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.sheldon.springbootinit.model.entity.Chart;
import com.sheldon.springbootinit.model.enums.ChartStatueEnum;
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
public class MsgConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private ChartInfoService chartInfoService;

    @RabbitListener(queues = {BiMqConstant.QUEUE_FAILED_NAME}, ackMode = "MANUAL")
    public void receiveFailedMsg(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) {
        log.info("MQ:接收分析失败图表信息：{}", message);
        try {
            // 校验消息
            Chart chart = new Chart();
            chart.setId(Long.parseLong(message));
            chart.setStatus(ChartStatueEnum.FAILED.getValue());
            boolean b = chartService.updateById(chart);
            if (!b) {
                log.error("失败图表信息确认失败：{}", "更新失败");
                channel.basicNack(deliveryTag, false, true);
            }
            channel.basicAck(deliveryTag, true);
            log.info("失败图表信息确认成功");
        } catch (IOException e) {
            log.error("失败图表信息确认失败：{}", e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("失败图表信息确认失败：{}", ex.getMessage());
            }
        }

    }


    @RabbitListener(queues = {BiMqConstant.QUEUE_SUCCEED_NAME}, ackMode = "MANUAL")
    public void receiveSucceedMsg(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) Long deliveryTag) {
        log.info("MQ:接收分析成功图表信息：{}", message);
        try {
            channel.basicAck(deliveryTag, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
