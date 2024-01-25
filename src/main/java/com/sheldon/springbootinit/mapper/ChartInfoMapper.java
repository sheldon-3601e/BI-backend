package com.sheldon.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sheldon.springbootinit.model.entity.Chart;

import java.util.List;
import java.util.Map;

/**
* @author 26483
* @description 针对表【chart(图表)】的数据库操作Mapper
* @createDate 2024-01-18 01:58:17
* @Entity com.sheldon.springbootinit.model.entity.Chart
*/
public interface ChartInfoMapper extends BaseMapper<Chart> {

    /**
     * 动态创建 图表详细信息表，并插入数据
     *
     * @param insertSql
     * @return
     */
    boolean insertChartInfo(String insertSql);

    /**
     * 动态查询 图表详细信息
     *
     * @param querySql
     * @return
     */
    List<Map<String, Object>> getChartInfoById(String querySql);

    /**
     * 动态删除 图表详细信息表
     *
     * @param deleteSql
     * @return
     */
    boolean deleteChartInfo(String deleteSql);

}




