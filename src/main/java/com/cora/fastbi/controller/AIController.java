package com.cora.fastbi.controller;

import cn.hutool.core.io.FileUtil;
import com.cora.fastbi.common.BaseResponse;
import com.cora.fastbi.common.ErrorCode;
import com.cora.fastbi.common.ResultUtils;
import com.cora.fastbi.common.StatusType;
import com.cora.fastbi.constant.AIConstant;
import com.cora.fastbi.exception.BusinessException;
import com.cora.fastbi.exception.ThrowUtils;
import com.cora.fastbi.model.dto.chart.GenChartByAIRequest;
import com.cora.fastbi.model.entity.Chart;
import com.cora.fastbi.model.entity.User;
import com.cora.fastbi.model.vo.BiResponse;
import com.cora.fastbi.service.ChartService;
import com.cora.fastbi.service.UserService;
import com.cora.fastbi.strategy.*;
import com.cora.fastbi.utils.AI.AIUtils;
import com.cora.fastbi.utils.ExcelUtils;
import com.google.common.util.concurrent.RateLimiter;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

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

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    private AIStrategy strategy;

    private static final List<String> validAIName = Arrays.asList(AIConstant.XUNFEI, AIConstant.ZHIPU, AIConstant.BAIDU_APP);

//    public static final float QPS = 2f;
    public static final float QPS = 0.1f;

    RateLimiter rateLimiter = RateLimiter.create(QPS); // 限流


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
        // 获取参数
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        String AIName = genChartByAIRequest.getStrategyAIName();
        // 校验参数
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "图表名称不能为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(AIName), ErrorCode.PARAMS_ERROR, "模型名称不能为空");
        ThrowUtils.throwIf(!validAIName.contains(AIName), ErrorCode.PARAMS_ERROR, "模型名称非法");
        // 校验文件格式 大小
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > 1024 * 1024 * 10, ErrorCode.PARAMS_ERROR, "文件不能大于10M");
        String originalFilename = multipartFile.getOriginalFilename();
        final List<String> validSuffix = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validSuffix.contains(FileUtil.getSuffix(originalFilename)), ErrorCode.PARAMS_ERROR, "文件格式不正确");

        //获取当前用户
        User loginUser = userService.getLoginUser(httpServletRequest);
        //限流
        ThrowUtils.throwIf(!rateLimiter.tryAcquire(), ErrorCode.OPERATION_TOO_FREQUENT, Math.ceil(1.0 / QPS) + "秒内只能提交一次操作");

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
        String[] strings = totalResult.split("部分：");

        String generateChart = "";
        generateChart = strings[1]
                .substring(strings[1].indexOf("{"), strings[1].lastIndexOf("}") + 1);
        String generateResult = strings[2].replaceFirst("\n", "").trim();

        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setOriginData(originData);
        chart.setChartType(chartType);
        chart.setGenerateChart(generateChart);
        chart.setGenerateResult(generateResult);
        chart.setUserId(loginUser.getId());
        chart.setStatus(StatusType.SUCCEED.getStatus());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.DATABASE_ERROR, "图表保存失败");

        // 数据库表优化-单独建表保存原始数据 todo
        // getCreateTableSQL(multipartFile, chart.getId());
        // getInsertDataSQL(multipartFile, chart.getId());
        // chartService.saveOriginData(getOriginDataSQL(multipartFile));

        BiResponse biResponse = new BiResponse();
        biResponse.setId(chart.getId());
        biResponse.setGenerateChart(chart.getGenerateChart());
        biResponse.setGenerateResult(chart.getGenerateResult());

        return ResultUtils.success(biResponse);
    }

    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param httpServletRequest
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAIAsync(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAIRequest genChartByAIRequest, HttpServletRequest httpServletRequest) {
        // 获取参数
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        String chartType = genChartByAIRequest.getChartType();
        String AIName = genChartByAIRequest.getStrategyAIName();
        // 校验参数
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "图表名称不能为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(AIName), ErrorCode.PARAMS_ERROR, "模型名称不能为空");
        ThrowUtils.throwIf(!validAIName.contains(AIName), ErrorCode.PARAMS_ERROR, "模型名称非法");
        // 校验文件格式 大小
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > 1024 * 1024 * 10, ErrorCode.PARAMS_ERROR, "文件不能大于10M");
        String originalFilename = multipartFile.getOriginalFilename();
        final List<String> validSuffix = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validSuffix.contains(FileUtil.getSuffix(originalFilename)), ErrorCode.PARAMS_ERROR, "文件格式不正确");

        //获取当前用户
        User loginUser = userService.getLoginUser(httpServletRequest);
        //限流
        ThrowUtils.throwIf(!rateLimiter.tryAcquire(), ErrorCode.OPERATION_TOO_FREQUENT, Math.ceil(1.0 / QPS) + "秒内只能提交一次操作");

        // 图表数据压缩
        String originData = ExcelUtils.convertExcelToCsv(multipartFile);

        // 拼接提问字符串
        StringBuilder newQuestion = new StringBuilder(AIUtils.getPrompt());
        newQuestion.append("分析需求：\n").append(goal);
        if (StringUtils.isNotBlank(chartType)) {
            newQuestion.append("，请使用图表类型：").append(chartType).append("\n");
        }
        newQuestion.append("原始数据：\n").append(originData);

        // 先将任务存入数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setOriginData(originData);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
        chart.setStatus(StatusType.WAIT.getStatus());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.DATABASE_ERROR, "图表保存失败");

        // 提交任务到线程池
        CompletableFuture.runAsync(() -> {
            log.info(Thread.currentThread().getName() + "开始分析");
            // 设置任务状态为running
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus(StatusType.RUNNING.getStatus());
            boolean addResult = chartService.updateById(updateChart);
            ThrowUtils.throwIf(!addResult, ErrorCode.DATABASE_ERROR, "图表保存失败");

            // 设置提问策略
            setStrategy(AIName);

            // 提问
            String totalResult = strategy.AIQuestion(newQuestion.toString());
            String generateChart = null;
            String generateResult = null;

            try {
                // 拆分字符串
                String[] strings = totalResult.split("部分：");

                generateChart = "";
                generateChart = strings[1]
                        .substring(strings[1].indexOf("{"), strings[1].lastIndexOf("}") + 1);
                generateResult = strings[2].replaceFirst("\n", "").trim();
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "结果解析失败");
            }

            // 更新结果到数据库
            Chart resChart = new Chart();
            resChart.setId(chart.getId());
            resChart.setGenerateChart(generateChart);
            resChart.setGenerateResult(generateResult);
            resChart.setStatus(StatusType.SUCCEED.getStatus());
            boolean updateResult = chartService.updateById(resChart);
            ThrowUtils.throwIf(!updateResult, ErrorCode.DATABASE_ERROR, "图表保存失败");

            log.info(Thread.currentThread().getName() + "结束分析");

        }, threadPoolExecutor);

        // 数据库表优化-单独建表保存原始数据 todo
        // getCreateTableSQL(multipartFile, chart.getId());
        // getInsertDataSQL(multipartFile, chart.getId());
        // chartService.saveOriginData(getOriginDataSQL(multipartFile));

        BiResponse biResponse = new BiResponse();
        biResponse.setId(chart.getId());

        return ResultUtils.success(biResponse);
    }


    // 数据库表优化-获取原始数据的建表语句 todo
//    private String getCreateTableSQL(MultipartFile multipartFile, Long chartId) {
//        List<String> headers = ExcelUtils.getExcelHeader(multipartFile);
//        StringBuilder sb = new StringBuilder();
//        sb.append("CREATE TABLE IF NOT EXISTS chart_").append(chartId).append(" (");
//        for (String header : headers) {
//            sb.append(" ").append(header).append(" varchar(100) null,");
//        }
//        sb.deleteCharAt(sb.length() - 1);
//
//        sb.append(");");
//
//        return sb.toString();
//    }

    // 数据库表优化-获取数据的导入语句 todo
//    private String getInsertDataSQL(MultipartFile multipartFile, Long chartId) {
//        List<List<String>> excelData = ExcelUtils.getExcelData(multipartFile);
//        StringBuilder sb = new StringBuilder();
//        sb.append("INSERT INTO chart_").append(chartId).append(" VALUES  ");
//        for (List<String> data : excelData) {
//            sb.append("(");
//            for (String cell : data) {
//                sb.append("'").append(cell).append("',");
//            }
//            sb.deleteCharAt(sb.length() - 1);
//            sb.append("),");
//        }
//        sb.deleteCharAt(sb.length() - 1).append(";");
//        return sb.toString();
//    }


    // 修改策略
    public void setStrategy(String strategyAIName) {
        switch (strategyAIName) {
            case AIConstant.XUNFEI:
                this.strategy = new XunfeiStrategy();
                break;
            case AIConstant.ZHIPU:
                this.strategy = new ZhipuStrategy();
                break;
            case AIConstant.BAIDU_APP:
                this.strategy = new BaiduAppStrategy();
                break;
        }
    }
}
