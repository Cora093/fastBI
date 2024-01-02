package com.cora.fastbi.strategy;

import com.cora.fastbi.common.ErrorCode;
import com.cora.fastbi.config.KeyConfig;
import com.cora.fastbi.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;


/**
 * 具体策略3
 */
public class YuCongMingStrategy implements AIStrategy{

    @Override
    public String AIQuestion(String question) {
        String accessKey = KeyConfig.getYucongmingAccessKey();
        String secretKey = KeyConfig.getYucongmingSecretKey();
        YuCongMingClient client = new YuCongMingClient(accessKey, secretKey);
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1735571798999134210L);
        devChatRequest.setMessage(question);
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应错误");
        }
        return response.getData().getContent();
    }
}
