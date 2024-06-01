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
    private static String zhipuApiKey;

    @Getter
    private static String qwenApiKey;

    @Getter
    private static String proxyHost;

    @Getter
    private static String proxyPort;

    @Getter
    @Deprecated
    private static String openAiApiKey;

    @Getter
    @Deprecated
    private static String yucongmingAccessKey;

    @Getter
    @Deprecated
    private static String yucongmingSecretKey;

    @Getter
    @Deprecated
    private static String baiduModelID;

    @Getter
    @Deprecated
    private static String baiduSecretkey;


    @Autowired
    public KeyConfig(Environment environment) {
        xunfeiAppid = environment.getProperty("mykeys.xunfei.appid");
        xunfeiApiSecret = environment.getProperty("mykeys.xunfei.apisecret");
        xunfeiApiKey = environment.getProperty("mykeys.xunfei.apikey");
        zhipuApiKey = environment.getProperty("mykeys.zhipu.apikey");
        qwenApiKey = environment.getProperty("mykeys.qwen.apikey");
        proxyHost = environment.getProperty("proxy.host");
        proxyPort = environment.getProperty("proxy.port");
//        openAiApiKey = environment.getProperty("mykeys.openai.key");
//        yucongmingAccessKey = environment.getProperty("mykeys.yucongming.accesskey");
//        yucongmingSecretKey = environment.getProperty("mykeys.yucongming.secretkey");
//        baiduModelID = environment.getProperty("mykeys.baiduapp.modelid");
//        baiduSecretkey = environment.getProperty("mykeys.baiduapp.secretkey");
    }

}