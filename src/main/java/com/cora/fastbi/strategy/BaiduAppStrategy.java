package com.cora.fastbi.strategy;

import com.baidubce.appbuilder.console.agentbuilder.AgentBuilder;
import com.baidubce.appbuilder.model.agentbuilder.AgentBuilderIterator;
import com.cora.fastbi.common.ErrorCode;
import com.cora.fastbi.config.KeyConfig;
import com.cora.fastbi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaiduAppStrategy implements AIStrategy {
    @Override
    public String AIQuestion(String question) {
        System.setProperty("APPBUILDER_TOKEN", KeyConfig.getBaiduSecretkey());

        String appId = KeyConfig.getBaiduModelID();

        AgentBuilder agentBuilder = new AgentBuilder(appId);
        AgentBuilderIterator itor = null;
        try {
            String conversationId = agentBuilder.createConversation();
            // System.out.println("conversationId: " + conversationId);
            itor = agentBuilder.run(question, conversationId, new String[]{}, false);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_INTERFACE_ERROR);
        }

        log.info("baiduapp成功获取到结果");
        String res = itor.next().getAnswer();
//        log.info(res);
        return res;



//        while (itor.hasNext()) {
//            AgentBuilderResult response = itor.next();
//            System.out.print(response.getAnswer());
//        }

    }
}
