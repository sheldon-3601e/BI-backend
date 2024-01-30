package com.sheldon.springbootinit.webSocket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ClassName WebSocketServiceTest
 * @Author 26483
 * @Date 2024/1/30 17:28
 * @Version 1.0
 * @Description TODO
 */
@SpringBootTest
class WebSocketServiceTest {

    @Resource
    private WebSocketService webSocketService;

    @Test
    void sendMessage() {
        try {
            webSocketService.sendMessage("11", "hello React");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}