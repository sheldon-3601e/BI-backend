package com.sheldon.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 智能生成图表
 *
 * @author <a href="https://github.com/sheldon-3601e">sheldon</a>
 * @from <a href="https://github.com/sheldon-3601e">github</a>
 */
@Data
public class ChartGenderUpdateRequest implements Serializable {

    /**
     * 图表Id
     */
    private Long chartId;

    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}