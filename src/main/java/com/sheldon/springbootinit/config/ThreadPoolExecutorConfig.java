

package com.sheldon.springbootinit.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ThreadPoolExcutorConfig
 * @Author 26483
 * @Date 2024/1/24 15:05
 * @Version 1.0
 * @Description TODO
 */
@Configuration
public class ThreadPoolExecutorConfig {

    /**
     * 配置一个自定义的线程池
     *
     * @return 返回配置好的 ThreadPoolExecutor 实例
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        // 创建自定义的线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;

            /**
             * 创建新的线程
             *
             * @param r Runnable 任务
             * @return 返回新创建的线程
             */
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("custom-thread-" + count);
                count++;
                return thread;
            }
        };

        // 创建 ThreadPoolExecutor 实例
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                2,                      // 核心线程数
                4,                      // 最大线程数
                100,                    // 空闲线程存活时间
                TimeUnit.SECONDS,       // 时间单位
                new ArrayBlockingQueue<>(4),  // 阻塞队列，用于存放等待执行的任务
                threadFactory           // 线程工厂
        );

        // 返回配置好的线程池
        return threadPoolExecutor;
    }
}