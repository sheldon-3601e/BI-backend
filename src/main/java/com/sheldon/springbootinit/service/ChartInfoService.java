package com.sheldon.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sheldon.springbootinit.model.dto.chart.ChartQueryRequest;
import com.sheldon.springbootinit.model.entity.Chart;

import java.util.List;
import java.util.Map;

/**
* @author 26483
* @description 针对表【chart(图表)】的数据库操作Service
* @createDate 2024-01-18 01:58:17
*/
public interface ChartInfoService extends IService<Chart> {

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    /**
     * 根据图表 Id 和图表内容动态创建数据库表
     * @param data
     * @param chartId
     */
    void createChartInfo(String data, Long chartId);

    /**
     * 根据图表 Id 动态查询数据库表
     * @param chartId
     * @param chartId
     */
    List<Map<String, Object>> getChartInfoById(Long chartId);

    /**
     * 根据图表 Id 动态删除数据库表
     * @param chartId
     * @param chartId
     */
    boolean deleteChartInfoById(Long chartId);

    /**
     * 异步请求 AI 生成结果
     * @param chart
     * @param data
     */
    void genderChartInfo(Chart chart);

}
