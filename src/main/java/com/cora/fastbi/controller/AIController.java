package com.cora.fastbi.controller;

import cn.hutool.core.io.FileUtil;
import com.cora.fastbi.common.BaseResponse;
import com.cora.fastbi.common.ErrorCode;
import com.cora.fastbi.common.ResultUtils;
import com.cora.fastbi.exception.BusinessException;
import com.cora.fastbi.exception.ThrowUtils;
import com.cora.fastbi.model.dto.chart.GenChartByAIRequest;
import com.cora.fastbi.model.entity.Chart;
import com.cora.fastbi.model.entity.User;
import com.cora.fastbi.model.vo.BiResponse;
import com.cora.fastbi.service.ChartService;
import com.cora.fastbi.service.UserService;
import com.cora.fastbi.utils.AI.AIUtils;
import com.cora.fastbi.utils.AI.XunfeiAIUtil;
import com.cora.fastbi.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        // 校验参数
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "图表名称不能为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标不能为空");
        // 校验文件
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        long size = multipartFile.getSize();
        ThrowUtils.throwIf(size > 1024 * 1024 * 10, ErrorCode.PARAMS_ERROR, "文件不能大于10M");
        String originalFilename = multipartFile.getOriginalFilename();
        final List<String> validSuffix = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validSuffix.contains(FileUtil.getSuffix(originalFilename)), ErrorCode.PARAMS_ERROR, "文件格式不正确");
        String suffix = FileUtil.getSuffix(originalFilename);


        User loginUser = userService.getLoginUser(httpServletRequest);

        // 图表数据压缩
        String originData = ExcelUtils.convertExcelToCsv(multipartFile);

        StringBuilder newQuestion = new StringBuilder(AIUtils.getPrompt());
        newQuestion.append("分析需求：\n").append(goal);
        if (StringUtils.isNotBlank(chartType)) {
            newQuestion.append("，请使用图表类型：").append(chartType).append("\n");
        }
        newQuestion.append("原始数据：\n").append(originData);

        String totalResult = "";

        // 封装请求调用AI接口
        try {
            String authUrl = XunfeiAIUtil.getAuthUrl();
            OkHttpClient client = new OkHttpClient.Builder().build();
            String url = authUrl
                    .replace("http://", "ws://")
                    .replace("https://", "wss://");
            Request request = new Request.Builder().url(url).build();
            CompletableFuture<String> future = new CompletableFuture<>();
            WebSocket webSocket = client.newWebSocket(request,
                    new XunfeiAIUtil(newQuestion.toString(), loginUser.getId() + "", false, totalAnswer -> {
                        // 当 WebSocket 连接完成时，将结果设置到 CompletableFuture
                        future.complete(totalAnswer);
                    }));
            totalResult += future.get();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI接口回调异常");
        }
        String[] strings = totalResult.split("【【【");
        String generateChart = strings[1].trim();
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


    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    /*
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
    */
}
