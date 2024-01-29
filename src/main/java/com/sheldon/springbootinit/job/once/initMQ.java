package com.sheldon.springbootinit.job.once;

import com.sheldon.springbootinit.bizmq.BiInitMain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 初始化 MQ 的交换机和队列
 *
 * @author <a href="https://github.com/sheldon-3601e">sheldon</a>
 * @from <a href="https://github.com/sheldon-3601e">github</a>
 */
@Component
@Slf4j
public class initMQ implements CommandLineRunner {

    @Override
    public void run(String... args) {
        BiInitMain.biInit();
    }
}
