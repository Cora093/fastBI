package com.cora.fastbi.utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterUtilsTest {

    @Test
    void testInstant() throws InterruptedException {
        Instant ins1 = Instant.now(); // 获取当前时间作为开始时间
        // 模拟一些操作
        Thread.sleep(2000); // 模拟耗时操作，这里休眠2秒

        Instant ins2 = Instant.now(); // 获取当前时间作为结束时间

        // 计算两个时间的差值
        Duration duration = Duration.between(ins1, ins2);
        long seconds = duration.getSeconds();
        long millis = duration.toMillis();

        System.out.println("Start Time: " + ins1);
        System.out.println("End Time: " + ins2);
        System.out.println("Duration in seconds: " + seconds);
        System.out.println("Duration in milliseconds: " + millis);
    }

}