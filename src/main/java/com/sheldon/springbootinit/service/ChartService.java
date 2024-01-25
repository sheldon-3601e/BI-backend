package com.sheldon.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sheldon.springbootinit.model.dto.chart.ChartQueryRequest;
import com.sheldon.springbootinit.model.dto.post.PostQueryRequest;
import com.sheldon.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sheldon.springbootinit.model.entity.Post;

import java.util.List;
import java.util.Map;

/**
* @author 26483
* @description 针对表【chart(图表)】的数据库操作Service
* @createDate 2024-01-18 01:58:17
*/
public interface ChartService extends IService<Chart> {

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

}
