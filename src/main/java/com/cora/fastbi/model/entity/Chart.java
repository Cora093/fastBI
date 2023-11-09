package com.cora.fastbi.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 图表信息表
 * @TableName chart
 */
@TableName(value ="chart")
@Data
public class Chart implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
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

    /**
     * 生成的图表数据
     */
    private String generateChart;

    /**
     * 生成的分析结论
     */
    private String generateResult;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}