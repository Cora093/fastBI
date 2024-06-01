package com.cora.fastbi.strategy;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.cora.fastbi.config.KeyConfig;
import com.cora.fastbi.constant.AIConstant;
import lombok.extern.slf4j.Slf4j;


/**
 * 具体策略——qwen
 */
@Slf4j
public class QwenStrategy implements AIStrategy {
    @Override
    public String AIQuestion(String prompt, String question) throws NoApiKeyException, InputRequiredException {
        String AIName = AIConstant.QWEN;
        // todo 模型名称加入配置文件
        String AIModel = Generation.Models.QWEN_MAX;
        log.info(AIName + "开始请求, 模型为:" + AIModel);

        Generation gen = new Generation();
        MessageManager msgManager = new MessageManager(10);
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(prompt)
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(question)
                .build();
        msgManager.add(systemMsg);
        msgManager.add(userMsg);
        QwenParam param = QwenParam.builder()
                .apiKey(KeyConfig.getQwenApiKey())
                .model(AIModel)
                .messages(msgManager.get())
                .resultFormat(QwenParam.ResultFormat.MESSAGE)
                .topP(0.3)
                .build();
        GenerationResult result = gen.call(param);
        String totalResult = result.getOutput().getChoices().get(0).getMessage().getContent();
        int totalTokens = result.getUsage().getTotalTokens();

        log.info(AIName + "成功获取到结果, 消耗token:" + totalTokens);
        return totalResult;
    }
}
