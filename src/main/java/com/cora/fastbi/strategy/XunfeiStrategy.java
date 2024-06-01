package com.cora.fastbi.strategy;

import com.cora.fastbi.common.ErrorCode;
import com.cora.fastbi.constant.AIConstant;
import com.cora.fastbi.exception.BusinessException;
import com.cora.fastbi.utils.AI.XunfeiAIUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

import java.util.concurrent.CompletableFuture;

/**
 * 具体策略2
 */
@Slf4j
public class XunfeiStrategy implements AIStrategy{
    @Override
    public String AIQuestion(String prompt, String question) {
        String AIName = AIConstant.XUNFEI;
        log.info(AIName + "开始请求");

        String totalQuestion = prompt + question;
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
                    new XunfeiAIUtil(question, false, totalAnswer -> {
                        // 当 WebSocket 连接完成时，将结果设置到 CompletableFuture
                        future.complete(totalAnswer);
                    }));
            totalResult += future.get();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI接口回调异常");
        }

        log.info(AIName + "成功获取到结果");
        return totalResult;
    }
}
