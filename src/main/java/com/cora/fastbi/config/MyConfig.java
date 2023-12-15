package com.cora.fastbi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 获取配置文件
 */
@Configuration
public class MyConfig {

    @Getter
    private static String xunfeiAppid;

    @Getter
    private static String xunfeiApiSecret;

    @Getter
    private static String xunfeiApiKey;

    @Autowired
    public MyConfig(Environment environment) {
        xunfeiAppid = environment.getProperty("mykeys.xunfei.appid");
        xunfeiApiSecret = environment.getProperty("mykeys.xunfei.apisecret");
        xunfeiApiKey = environment.getProperty("mykeys.xunfei.apikey");

    }

}