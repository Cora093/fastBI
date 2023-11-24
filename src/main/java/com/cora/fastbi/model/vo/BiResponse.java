package com.cora.fastbi.model.vo;

import lombok.Data;

@Data
public class BiResponse {
    /**
     * id
     */
    private Long id;

    /**
     * 生成的图表数据
     */
    private String generateChart;

    /**
     * 生成的分析结论
     */
    private String generateResult;
}
