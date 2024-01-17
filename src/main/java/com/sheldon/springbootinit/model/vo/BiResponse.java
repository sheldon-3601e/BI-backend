package com.sheldon.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName BiRespone
 * @Author 26483
 * @Date 2024/1/22 17:34
 * @Version 1.0
 * @Description TODO
 */
@Data
public class BiResponse implements Serializable {

    /**
     * 生成的图表数据
     */
    private Long id;

    /**
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    private static final long serialVersionUID = 1L;

}
