package com.sheldon.springbootinit.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sheldon.springbootinit.annotation.AuthCheck;
import com.sheldon.springbootinit.common.BaseResponse;
import com.sheldon.springbootinit.common.DeleteRequest;
import com.sheldon.springbootinit.common.ErrorCode;
import com.sheldon.springbootinit.common.ResultUtils;
import com.sheldon.springbootinit.constant.UserConstant;
import com.sheldon.springbootinit.exception.BusinessException;
import com.sheldon.springbootinit.exception.ThrowUtils;
import com.sheldon.springbootinit.manager.AiManager;
import com.sheldon.springbootinit.model.dto.chart.*;
import com.sheldon.springbootinit.model.entity.Chart;
import com.sheldon.springbootinit.model.entity.User;
import com.sheldon.springbootinit.model.vo.BiResponse;
import com.sheldon.springbootinit.service.ChartService;
import com.sheldon.springbootinit.service.UserService;
import com.sheldon.springbootinit.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图表信息接口
 *
 * @author <a href="https://github.com/sheldon-3601e">sheldon</a>
 * @from <a href="https://github.com/sheldon-3601e">github</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    private static final Long MODEL_ID = 1709156902984093697L;

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        // 参数校验
//        chartService.validChart(chart, false);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/page/admin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartByPageAndAdmin(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        // 参数校验
//        chartService.validChart(chart, false);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 智能分析图表
     *
     * @param multipartFile
     * @param chartGenderRequest
     * @param request
     * @return
     */
    @PostMapping("/gender")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 ChartGenderRequest chartGenderRequest, HttpServletRequest request) {
        String name = chartGenderRequest.getName();
        String goal = chartGenderRequest.getGoal();
        String chartType = chartGenderRequest.getChartType();

        // 校验参数
        ThrowUtils.throwIf(StrUtil.hasEmpty(goal), ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(goal.length() > 50, ErrorCode.PARAMS_ERROR, "参数长度过长");
        User loginUser = null;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
        }

        // 转化为 Csv文件
        String data = ExcelUtils.excelToCsv(multipartFile);

        // 拼接AI请求
        StringBuilder userInput = new StringBuilder();
        userInput.append("'Analysis goal:").append("\n");
        userInput.append(goal);
        if (StrUtil.isNotEmpty(chartType)) {
            userInput.append("，请使用").append(chartType);
        }
        userInput.append("\n");
        userInput.append("Raw data：").append("\n");
        userInput.append(data).append("'").append("\n");
        String userInputString = userInput.toString();
        BiResponse test = new BiResponse();
        test.setId(12345678L);
        test.setGenChart("{\n" +
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
        test.setGenResult("网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多," +
                "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多" +
                "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多," +
                "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多，" +
                "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多，" +
                "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多，" +
                "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多，" +
                "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多，" +
                "网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多网站用户数量逐渐增长，周六和周日用户数量较多，周一用户数量较少，周二到周五用户数量逐渐增多");
        return ResultUtils.success(test);

       /* String result = aiManager.doChart(MODEL_ID, userInput.toString());
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
        }

        // 保存图表到数据库
        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(data);
        chart.setChartType(chartType);
        chart.setGenChart(matchedGenCart);
        chart.setGenResult(genResult);
        boolean saveResult = chartService.save(chart);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表保持失败");
        }

        // 封装返回值
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(matchedGenCart);
        biResponse.setGenResult(genResult);
        biResponse.setId(chart.getId());
        return ResultUtils.success(biResponse);*/
    }
}