package com.sheldon.springbootinit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ClassName ChartInfoMapperTest
 * @Author 26483
 * @Date 2024/1/25 11:10
 * @Version 1.0
 * @Description TODO
 */
@SpringBootTest
class ChartInfoMapperTest {

    @Resource
    private ChartInfoMapper chartInfoMapper;

    @Test
    void insertChartInfo() {

    }

    @Test
    void getChartInfoById() {
        List<Map<String, Object>> chartInfoById = chartInfoMapper.getChartInfoById("select * from my_bi.chart_1750085619444396033");
        System.out.println(chartInfoById);
    }
}