package com.sheldon.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ClassName AiManagerTest
 * @Author 26483
 * @Date 2024/1/22 17:15
 * @Version 1.0
 * @Description TODO
 */
@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChart() {
        Long modelId = 1709156902984093697L;
        String data = "分析需求：\n" +
                "分析网站用户的增长情况\n" +
                "原始数据：\n" +
                "日期,用户数\n" +
                "1号,10\n" +
                "2号,20\n" +
                "3号,30";
        aiManager.doChart(modelId, data);
    }
}