package com.sheldon.springbootinit.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sheldon.springbootinit.constant.CommonConstant;
import com.sheldon.springbootinit.mapper.ChartInfoMapper;
import com.sheldon.springbootinit.mapper.ChartMapper;
import com.sheldon.springbootinit.model.dto.chart.ChartQueryRequest;
import com.sheldon.springbootinit.model.entity.Chart;
import com.sheldon.springbootinit.model.enums.ChartStatueEnum;
import com.sheldon.springbootinit.service.ChartInfoService;
import com.sheldon.springbootinit.service.ChartService;
import com.sheldon.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 26483
 * @description 针对表【chart(图表)】的数据库操作Service实现
 * @createDate 2024-01-18 01:58:17
 */
@Service
@Slf4j
public class ChartInfoServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartInfoService {

    @Resource
    private ChartInfoMapper chartInfoMapper;

    @Resource
    private ChartService chartService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

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
        chartInfoMapper.insertChartInfo(createTableStatement.toString());

        // 插入数据
//        System.out.println("Generated INSERT INTO statement:\n" + insertStatement.toString());
        chartInfoMapper.insertChartInfo(insertStatement.toString());
    }

    @Override
    public List getChartInfoById(Long chartId) {

        String tableName = "chart_" + chartId;
        String sql = "select * from " + tableName;
        List<Map<String, Object>> chartInfoById = chartInfoMapper.getChartInfoById(sql);
        return chartInfoById;
    }

    @Override
    public boolean deleteChartInfoById(Long chartId) {

        String tableName = "chart_" + chartId;
        // 删除数据表
        String sql = "drop table " + tableName;
        boolean res = chartInfoMapper.deleteChartInfo(sql);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 只回滚 Exception 及其子类异常
    public void genderChartInfo(Chart chart, String data) {

        String name = chart.getName();
        String goal = chart.getGoal();
        String chartType = chart.getChartType();


        // 构建 AI 服务需要的输入参数
        StringBuilder userInput = new StringBuilder();
        if (StrUtil.isNotEmpty(name)) {
            userInput.append("图表名称为").append(chartType).append("\n");
        }
        if (StrUtil.isNotEmpty(chartType)) {
            userInput.append("，请使用").append(chartType).append("\n");
        }
        userInput.append("'Analysis goal:").append("\n");
        userInput.append(goal);
        userInput.append("\n");
        userInput.append("Raw data：").append("\n");
        userInput.append(data).append("'").append("\n");
        String userInputString = userInput.toString();

        // 异步执行图表分析任务
        CompletableFuture.runAsync(() -> {
            try {
                // 更新图表状态为“执行中”
                Chart updateChart = new Chart();
                updateChart.setId(chart.getId());
                updateChart.setStatus(ChartStatueEnum.WORKING.getValue());
                boolean res = chartService.updateById(updateChart);
                if (!res) {
                    log.warn("图表分析保存失败");
                }

    /*            // 调用 AI 服务进行图表分析
                String result = aiManager.doChart(MODEL_ID, userInputString);
                if (StrUtil.isEmpty(result)) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI服务异常");
                }

                // 解析结果
                String[] split = result.split("【【【【【");
                if (split.length != 3) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI服务异常");
                }

                String genCart = split[1];
                String genResult = split[2];
                // 提取生成的代码
                String regex = "\\{([^{}]+)\\}";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(genCart);

                while (matcher.find()) {
                    String matchedGenCart = matcher.group(1);
                }*/

                // 创建模拟数据
                String matchedGenCart = ("{\n" +
                        "    \"title\": {\n" +
                        "        \"text\": \"网站用户人数趋势\",\n" +
                        "        \"subtext\": \"数据来源：Raw data\"\n" +
                        "    },\n" +
                        "    \"xAxis\": {\n" +
                        "        \"type\": \"category\",\n" +
                        "        \"data\": [\"1\", \"2\", \"3\"]\n" +
                        "    },\n" +
                        "    \"yAxis\": {\n" +
                        "        \"type\": \"value\"\n" +
                        "    },\n" +
                        "    \"series\": [\n" +
                        "        {\n" +
                        "            \"name\": \"用户人数\",\n" +
                        "            \"type\": \"bar\",\n" +
                        "            \"data\": [10, 20, 30]\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}");
                String genResult = ("网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多," +
                        "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多" +
                        "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多," +
                        "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多，" +
                        "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多，" +
                        "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多，" +
                        "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多，" +
                        "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多，" +
                        "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多");

                Chart updateChartResult = new Chart();
                updateChartResult.setId(chart.getId());
                updateChartResult.setStatus(ChartStatueEnum.SUCCEED.getValue());
                updateChartResult.setGenChart(matchedGenCart);
                updateChartResult.setGenResult(genResult);
                boolean updateResult = chartService.updateById(updateChartResult);
                if (!updateResult) {
                    log.error("图表分析保存失败");
                    throw new RuntimeException("图表分析保存失败");
                }

            } catch (Exception e) {
                // 处理图表分析过程中的异常
                log.error("图表分析失败", e);
                throw new RuntimeException("图表分析失败");
            }
        }, threadPoolExecutor);

    }

}




