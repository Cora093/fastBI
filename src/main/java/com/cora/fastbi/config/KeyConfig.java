package com.cora.fastbi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 获取配置文件中的keys
 */
@Configuration
public class KeyConfig {

    @Getter
    private static String xunfeiAppid;

    @Getter
    private static String xunfeiApiSecret;

    @Getter
    private static String xunfeiApiKey;

    @Getter
    private static String openAiApiKey;

    @Getter
    private static String yucongmingAccessKey;

    @Getter
    private static String yucongmingSecretKey;

    @Autowired
    public KeyConfig(Environment environment) {
        xunfeiAppid = environment.getProperty("mykeys.xunfei.appid");
        xunfeiApiSecret = environment.getProperty("mykeys.xunfei.apisecret");
        xunfeiApiKey = environment.getProperty("mykeys.xunfei.apikey");
        openAiApiKey = environment.getProperty("mykeys.openai.key");
        yucongmingAccessKey = environment.getProperty("mykeys.yucongming.accesskey");
        yucongmingSecretKey = environment.getProperty("mykeys.yucongming.secretkey");
    }

}