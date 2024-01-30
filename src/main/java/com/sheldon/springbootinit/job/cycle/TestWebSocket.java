package com.sheldon.springbootinit.job.cycle;

import com.sheldon.springbootinit.webSocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 增量同步帖子到 es
 *
 * @author <a href="https://github.com/sheldon-3601e">sheldon</a>
 * @from <a href="https://github.com/sheldon-3601e">github</a>
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class TestWebSocket {

    @Resource
    private WebSocketService webSocketService;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 10 * 1000)
    public void run() throws IOException {
        log.info("定时任务执行");
        try {
            webSocketService.sendMessage("11", "hello React");
        } catch (IOException e) {
            log.error("定时任务执行失败", e);
        }
    }
}
