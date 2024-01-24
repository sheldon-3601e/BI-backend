package com.sheldon.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sheldon.springbootinit.constant.CommonConstant;
import com.sheldon.springbootinit.mapper.ChartMapper;
import com.sheldon.springbootinit.model.dto.chart.ChartQueryRequest;
import com.sheldon.springbootinit.model.entity.Chart;
import com.sheldon.springbootinit.service.ChartService;
import com.sheldon.springbootinit.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author 26483
 * @description 针对表【chart(图表)】的数据库操作Service实现
 * @createDate 2024-01-18 01:58:17
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {

    @Resource
    private ChartMapper chartMapper;

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {

        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();

        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        Long userId = chartQueryRequest.getUserId();
        String goal = chartQueryRequest.getGoal();
        String chartData = chartQueryRequest.getChartData();
        String chartType = chartQueryRequest.getChartType();
        String genChart = chartQueryRequest.getGenChart();
        String genResult = chartQueryRequest.getGenResult();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.like(StringUtils.isNotBlank(chartData), "chartData", chartData);
        queryWrapper.like(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.like(StringUtils.isNotBlank(genChart), "genChart", genChart);
        queryWrapper.like(StringUtils.isNotBlank(genResult), "genResult", genResult);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void createChartInfo(String input, Long chartId) {

        String[] lines = input.split("\n");
        String tableName = "chart_" + chartId; // 替换为你的表名

        // Extract column names from the first line
        String[] columns = lines[0].split(",");

        // Generate CREATE TABLE statement
        StringBuilder createTableStatement = new StringBuilder("CREATE TABLE " + tableName + " (\n");
        createTableStatement.append("`id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id',\n");
        for (String column : columns) {
            createTableStatement.append("  ").append(column).append(" varchar(256) COLLATE utf8mb4_unicode_ci,\n");
        }
        createTableStatement.deleteCharAt(createTableStatement.lastIndexOf(","));
        createTableStatement.append(");\n");

        // Generate INSERT INTO statement
        StringBuilder insertStatement = new StringBuilder("INSERT INTO " + tableName + " (");
        for (String column : columns) {
            insertStatement.append(column).append(", ");
        }
        insertStatement.delete(insertStatement.lastIndexOf(", "), insertStatement.length()).append(") VALUES\n");

        // Process data lines and generate values for INSERT INTO statement
        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(",");
            insertStatement.append("(");
            for (String value : values) {
                insertStatement.append(value).append(", ");
            }
            insertStatement.delete(insertStatement.lastIndexOf(", "), insertStatement.length()).append("),\n");
        }
        insertStatement.deleteCharAt(insertStatement.lastIndexOf(","));
        insertStatement.append(";\n");

        // 建立数据存储表
//        System.out.println("Generated CREATE TABLE statement:\n" + createTableStatement);
        chartMapper.insertChartInfo(createTableStatement.toString());

        // 插入数据
//        System.out.println("Generated INSERT INTO statement:\n" + insertStatement.toString());
        chartMapper.insertChartInfo(insertStatement.toString());
    }

    @Override
    public List getChartInfoById(Long chartId) {

        String tableName = "chart_" + chartId;
        String sql = "select * from " + tableName;
        List<Map<String, Object>> chartInfoById = chartMapper.getChartInfoById(sql);
        return chartInfoById;
    }
}




