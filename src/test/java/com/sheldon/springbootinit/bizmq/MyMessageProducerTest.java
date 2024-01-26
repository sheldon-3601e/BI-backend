package com.sheldon.springbootinit.bizmq;

import com.sheldon.springbootinit.bizmq.test.MyMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @ClassName MyMessageProducerTest
 * @Author 26483
 * @Date 2024/1/26 17:04
 * @Version 1.0
 * @Description TODO
 */
@SpringBootTest
class MyMessageProducerTest {

    @Resource
    private MyMessageProducer myMessageProducer;

    @Test
    void sendMessage() {
        myMessageProducer.sendMessage("code_test_exchange", "my_routing", "hello world");
    }
}