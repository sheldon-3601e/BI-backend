package com.sheldon.springbootinit.bizmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @ClassName BiMessageProducerTest
 * @Author 26483
 * @Date 2024/1/29 16:28
 * @Version 1.0
 * @Description TODO
 */
@SpringBootTest
class MsgProducerTest {

    @Resource
    private MsgProducer msgProducer;

    @Test
    void sendWaitingMSg() {
        msgProducer.sendWaitingMsg("succeed");
    }
}