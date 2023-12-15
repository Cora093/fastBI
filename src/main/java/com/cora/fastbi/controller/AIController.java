package com.cora.fastbi.controller;

import cn.hutool.core.io.FileUtil;
import com.cora.fastbi.common.BaseResponse;
import com.cora.fastbi.common.ErrorCode;
import com.cora.fastbi.common.ResultUtils;
import com.cora.fastbi.constant.AIConstant;
import com.cora.fastbi.exception.ThrowUtils;
import com.cora.fastbi.model.dto.chart.GenChartByAIRequest;
import com.cora.fastbi.model.entity.Chart;
import com.cora.fastbi.model.entity.User;
import com.cora.fastbi.model.vo.BiResponse;
import com.cora.fastbi.service.ChartService;
import com.cora.fastbi.service.UserService;
import com.cora.fastbi.strategy.AIStrategy;
import com.cora.fastbi.strategy.OpenAIStrategy;
import com.cora.fastbi.strategy.XunfeiStrategy;
import com.cora.fastbi.utils.AI.AIUtils;
import com.cora.fastbi.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * AI接口
 */
@RestController
@RequestMapping("/AI")
@Slf4j
public class AIController {

    @Resource
    private UserService userService;

    @Resource
    private ChartService chartService;

    private AIStrategy strategy;

    /**
     * 智能分析
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAI(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAIRequest genChartByAIRequest, HttpServletRequest httpServletRequest) {
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        String AIName = genChartByAIRequest.getStrategyAIName();
        // 校验参数
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "图表名称不能为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(AIName), ErrorCode.PARAMS_ERROR, "模型名称不能为空");
        final List<String> validAIName = Arrays.asList(AIConstant.XUNFEI, AIConstant.OPENAI_API, AIConstant.YUCONGMING);
        ThrowUtils.throwIf(!validAIName.contains(AIName), ErrorCode.PARAMS_ERROR, "模型名称非法");
        // 校验文件格式 大小
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > 1024 * 1024 * 10, ErrorCode.PARAMS_ERROR, "文件不能大于10M");
        String originalFilename = multipartFile.getOriginalFilename();
        final List<String> validSuffix = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validSuffix.contains(FileUtil.getSuffix(originalFilename)), ErrorCode.PARAMS_ERROR, "文件格式不正确");

        User loginUser = userService.getLoginUser(httpServletRequest);

        // 图表数据压缩
        String originData = ExcelUtils.convertExcelToCsv(multipartFile);

        // 拼接提问字符串
        StringBuilder newQuestion = new StringBuilder(AIUtils.getPrompt());
        newQuestion.append("分析需求：\n").append(goal);
        if (StringUtils.isNotBlank(chartType)) {
            newQuestion.append("，请使用图表类型：").append(chartType).append("\n");
        }
        newQuestion.append("原始数据：\n").append(originData);

        // 设置提问策略
        setStrategy(AIName);

        // 提问
        String totalResult = strategy.AIQuestion(newQuestion.toString());

        // 拆分字符串
        String[] strings = totalResult.split("：");

        String generateChart = strings[1].substring(
                strings[1].indexOf("option"), strings[1].lastIndexOf("};") + 2);
        String generateResult = strings[2].trim();

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setOriginData(originData);
        chart.setChartType(chartType);
        chart.setGenerateChart(generateChart);
        chart.setGenerateResult(generateResult);
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.DATABASE_ERROR, "图表保存失败");

        BiResponse biResponse = new BiResponse();
        biResponse.setId(chart.getId());
        biResponse.setGenerateChart(chart.getGenerateChart());
        biResponse.setGenerateResult(chart.getGenerateResult());

        return ResultUtils.success(biResponse);
    }


    // 修改策略
    public void setStrategy(String strategyAIName) {
        if (strategyAIName.equals(AIConstant.XUNFEI)) {
            this.strategy = new XunfeiStrategy();
        } else if (strategyAIName.equals(AIConstant.OPENAI_API)) {
            this.strategy = new OpenAIStrategy();
        }
    }
}
