package com.cora.fastbi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 智能分析请求
 *
 */
@Data
public class GenChartByAIRequest implements Serializable {

    /**
     * 图表名称
     */
    private String name;

    /**
     * 任务分析目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * AI模型
     */
    private String strategyAIName;

    private static final long serialVersionUID = 2347839513350379821L;
}