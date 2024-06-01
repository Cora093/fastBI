package com.cora.fastbi.strategy;

import com.cora.fastbi.common.ErrorCode;
import com.cora.fastbi.config.KeyConfig;
import com.cora.fastbi.constant.AIConstant;
import com.cora.fastbi.exception.BusinessException;
import com.cora.fastbi.exception.ThrowUtils;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 具体策略——zhipu
 */
@Slf4j
public class ZhipuStrategy implements AIStrategy {
    @Override
    public String AIQuestion(String prompt, String question) {
        String AIName = AIConstant.ZHIPU;
        String AIModel = Constants.ModelChatGLM4;
        log.info(AIName + "开始请求, 模型为:" + AIModel);

        String totalResult = "";
        // 调用AI接口
        ClientV4 client = new ClientV4.Builder(KeyConfig.getZhipuApiKey()).build();
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage sysMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), prompt);
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), question);
        messages.add(sysMessage);
        messages.add(userMessage);
        String requestId = String.format("request_%s", System.currentTimeMillis());

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(AIModel)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .topP(0.3f)
                .build();
        ModelApiResponse modelApiResponse = client.invokeModelApi(chatCompletionRequest);
        ThrowUtils.throwIf(modelApiResponse.getCode() != 200,
                new BusinessException(ErrorCode.AI_INTERFACE_ERROR, AIName + "调用失败"));
        totalResult = modelApiResponse.getData().getChoices().get(0).getMessage().getContent().toString();

        log.info(AIName + "成功获取到结果, 消耗token:" + modelApiResponse.getData().getUsage().getTotalTokens());
        return totalResult;
    }
}
