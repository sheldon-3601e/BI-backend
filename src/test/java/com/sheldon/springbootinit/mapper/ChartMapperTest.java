package com.sheldon.springbootinit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ClassName ChartMapperTest
 * @Author 26483
 * @Date 2024/1/24 2:56
 * @Version 1.0
 * @Description TODO
 */
@SpringBootTest
class ChartMapperTest {

    @Resource
    private ChartMapper chartMapper;

    @Test
    void insertChartInfo() {
    }

    @Test
    void getChartInfoById() {
        List<Map<String, Object>> chartInfoById = chartMapper.getChartInfoById("select * from chart_1749844002250702849");
        System.out.println(chartInfoById);
    }
}