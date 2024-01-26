package com.sheldon.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sheldon.springbootinit.annotation.AuthCheck;
import com.sheldon.springbootinit.bizmq.BiMessageProducer;
import com.sheldon.springbootinit.common.BaseResponse;
import com.sheldon.springbootinit.common.DeleteRequest;
import com.sheldon.springbootinit.common.ErrorCode;
import com.sheldon.springbootinit.common.ResultUtils;
import com.sheldon.springbootinit.constant.AiConstant;
import com.sheldon.springbootinit.constant.UserConstant;
import com.sheldon.springbootinit.exception.BusinessException;
import com.sheldon.springbootinit.exception.ThrowUtils;
import com.sheldon.springbootinit.manager.AiManager;
import com.sheldon.springbootinit.manager.RedisLimiterManager;
import com.sheldon.springbootinit.model.dto.chart.*;
import com.sheldon.springbootinit.model.entity.Chart;
import com.sheldon.springbootinit.model.entity.User;
import com.sheldon.springbootinit.model.enums.ChartStatueEnum;
import com.sheldon.springbootinit.model.vo.BiResponse;
import com.sheldon.springbootinit.service.ChartInfoService;
import com.sheldon.springbootinit.service.ChartService;
import com.sheldon.springbootinit.service.UserService;
import com.sheldon.springbootinit.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private ChartInfoService chartInfoService;

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageProducer biMessageProducer;

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
    @Transactional
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 添加事务
        boolean res2 = chartInfoService.deleteChartInfoById(id);
        boolean res1 = chartService.removeById(id);
        return ResultUtils.success(res1 && !res2);
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
     * 智能分析图表（同步）
     *
     * @param multipartFile
     * @param chartGenderRequest
     * @param request
     * @return
     */
    @PostMapping("/gender")
    @Transactional
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 ChartGenderRequest chartGenderRequest, HttpServletRequest request) {
        String name = chartGenderRequest.getName();
        String goal = chartGenderRequest.getGoal();
        String chartType = chartGenderRequest.getChartType();

        // 校验参数
        ThrowUtils.throwIf(StrUtil.hasEmpty(goal), ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(goal.length() > 50, ErrorCode.PARAMS_ERROR, "参数长度过长");

        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final Long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1MB");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> ALLOWED_SUFFIX = Arrays.asList("xls", "xlsx");
        ThrowUtils.throwIf(!ALLOWED_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀不合规");

        // 获取登录用户
        User loginUser = null;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
        }
        // 限流
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

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

        // 调用AI服务
        String result = aiManager.doChart(AiConstant.MODEL_ID, userInput.toString());
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

        String matchedGenCart = null;
        while (matcher.find()) {
            matchedGenCart = matcher.group(1);
        }

        // 保存图表到数据库
        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setGenChart(matchedGenCart);
        chart.setGenResult(genResult);
        boolean saveResult = chartService.save(chart);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表保持失败");
        }

        // 将原始数据单独保存
        chartInfoService.createChartInfo(data, chart.getId());

        // 封装返回值
        BiResponse biResponse = new BiResponse();
        biResponse.setGenChart(matchedGenCart);
        biResponse.setGenResult(genResult);
        biResponse.setId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（异步 -> 线程池）
     *
     * @param multipartFile      上传的 Excel 文件
     * @param chartGenderRequest 图表分析请求对象
     * @param request            HTTP 请求对象
     * @return 返回异步执行结果
     */
    @PostMapping("/gender/async")
    @Transactional(rollbackFor = Exception.class) // 只回滚 Exception 及其子类异常
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                      ChartGenderRequest chartGenderRequest, HttpServletRequest request) {
        // 从请求对象中获取参数
        String name = chartGenderRequest.getName();
        String goal = chartGenderRequest.getGoal();
        String chartType = chartGenderRequest.getChartType();

        // 参数校验
        ThrowUtils.throwIf(StrUtil.hasEmpty(goal), ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(goal.length() > 50, ErrorCode.PARAMS_ERROR, "参数长度过长");

        // 文件校验
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final Long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1MB");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> ALLOWED_SUFFIX = Arrays.asList("xls", "xlsx");
        ThrowUtils.throwIf(!ALLOWED_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀不合规");

        // 获取登录用户
        User loginUser = null;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
        }
        // 限流
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        // 将 Excel 转为 Csv 文件
        String data = ExcelUtils.excelToCsv(multipartFile);

        // 保存图表到数据库
        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setStatus(ChartStatueEnum.WAIT.getValue());
        boolean saveResult = chartService.save(chart);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表保存失败");
        }

        // 将原始数据单独保存
        chartInfoService.createChartInfo(data, chart.getId());

        // 异步执行图表分析任务
        CompletableFuture.runAsync(() -> {
            // 调用 AI 服务
            String result = aiManager.doChart(AiConstant.MODEL_ID, data);
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

            // 更新图表
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setGenChart(genCart);
            updateChart.setGenResult(genResult);
            updateChart.setStatus(ChartStatueEnum.SUCCEED.getValue());
            boolean updateResult = chartService.updateById(updateChart);
            if (!updateResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表更新失败");
            }
        }, threadPoolExecutor);

        // 封装返回值
        BiResponse biResponse = new BiResponse();
        biResponse.setId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（异步 -> 消息队列）
     *
     * @param multipartFile      上传的 Excel 文件
     * @param chartGenderRequest 图表分析请求对象
     * @param request            HTTP 请求对象
     * @return 返回异步执行结果
     */
    @PostMapping("/gender/async/mq")
    @Transactional(rollbackFor = Exception.class) // 只回滚 Exception 及其子类异常
    public BaseResponse<BiResponse> genChartByAiAsyncMq(@RequestPart("file") MultipartFile multipartFile,
                                                      ChartGenderRequest chartGenderRequest, HttpServletRequest request) {
        // 从请求对象中获取参数
        String name = chartGenderRequest.getName();
        String goal = chartGenderRequest.getGoal();
        String chartType = chartGenderRequest.getChartType();

        // 参数校验
        ThrowUtils.throwIf(StrUtil.hasEmpty(goal), ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(goal.length() > 50, ErrorCode.PARAMS_ERROR, "参数长度过长");

        // 文件校验
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final Long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1MB");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> ALLOWED_SUFFIX = Arrays.asList("xls", "xlsx");
        ThrowUtils.throwIf(!ALLOWED_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀不合规");

        // 获取登录用户
        User loginUser = null;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
        }
        // 限流
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        // 将 Excel 转为 Csv 文件
        String data = ExcelUtils.excelToCsv(multipartFile);

        // 获取图表的 keyList
        String[] split = data.split("\n");
        String keys = split[0];

        // 保存图表到数据库
        Chart chart = new Chart();
        chart.setUserId(loginUser.getId());
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setChartData(keys);
        chart.setStatus(ChartStatueEnum.WAIT.getValue());
        boolean saveResult = chartService.save(chart);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表保存失败");
        }
        Long newChartId = chart.getId();

        // 将原始数据单独保存
        chartInfoService.createChartInfo(data, newChartId);

        // 使用异步消息队列执行图表分析任务
        biMessageProducer.sendMessage(String.valueOf(newChartId));

        // 封装返回值
        BiResponse biResponse = new BiResponse();
        biResponse.setId(newChartId);
        return ResultUtils.success(biResponse);
    }

    /**
     * 修改智能分析（异步）
     *
     * @param multipartFile      上传的 Excel 文件
     * @param chartGenderUpdateRequest 图表分析请求对象
     * @param request            HTTP 请求对象
     * @return 返回异步执行结果
     */
    @Transactional
    @PostMapping("/gender/update/async")
    public BaseResponse<BiResponse> genUpdateChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                            ChartGenderUpdateRequest chartGenderUpdateRequest, HttpServletRequest request) {
        // 从请求对象中获取参数
        Long chartId = chartGenderUpdateRequest.getChartId();
        String name = chartGenderUpdateRequest.getName();
        String goal = chartGenderUpdateRequest.getGoal();
        String chartType = chartGenderUpdateRequest.getChartType();

        // 参数校验
        ThrowUtils.throwIf(StrUtil.hasEmpty(goal), ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(goal.length() > 50, ErrorCode.PARAMS_ERROR, "参数长度过长");

        // 文件校验
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final Long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1MB");
        // 校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> ALLOWED_SUFFIX = Arrays.asList("xls", "xlsx");
        ThrowUtils.throwIf(!ALLOWED_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀不合规");

        // 获取登录用户
        User loginUser = null;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
        }
        // 限流
        redisLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());

        // 将 Excel 转为 Csv 文件
        String data = ExcelUtils.excelToCsv(multipartFile);

        // 获取图表的 keyList
        String[] split = data.split("\n");
        String keys = split[0];

        // 保存图表到数据库
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setUserId(loginUser.getId());
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setChartData(keys);
        chart.setStatus(ChartStatueEnum.WAIT.getValue());
        boolean saveResult = chartService.updateById(chart);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表保存失败");
        }

        // 将原始数据单独保存
        boolean deleteUpdate = chartInfoService.deleteChartInfoById(chartId);
        if (!deleteUpdate) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表更新失败");
        }
        Long newChartId = chart.getId();
        chartInfoService.createChartInfo(data, newChartId);

        // 使用异步消息队列执行图表分析任务
        biMessageProducer.sendMessage(String.valueOf(newChartId));

        // 封装返回值
        BiResponse biResponse = new BiResponse();
        biResponse.setId(newChartId);
        return ResultUtils.success(biResponse);
    }

    /**
     * 根据 id 获取图表原始数据
     *
     * @param id
     * @return
     */
    @GetMapping("/get/chartInfo")
    public BaseResponse<List<Map<String, Object>>> getChartInfoById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<Map<String, Object>> chartInfo = chartInfoService.getChartInfoById(id);
        if (chartInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chartInfo);
    }
}