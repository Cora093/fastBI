package com.cora.fastbi.model.dto.chart;

import java.io.Serializable;
import lombok.Data;

/**
 * 创建请求
 */
@Data
public class ChartAddRequest implements Serializable {

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