package com.cora.fastbi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑请求
 *
 */
@Data
public class ChartEditRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 任务分析目标
     */
    private String goal;

    /**
     * 原始输入数据
     */
    private String originData;

    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}