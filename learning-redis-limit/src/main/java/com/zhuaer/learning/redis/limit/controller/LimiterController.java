package com.zhuaer.learning.redis.limit.controller;

import com.zhuaer.learning.redis.limit.annotation.Limit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName LimiterController
 * @Description TODO
 * @Author zhua
 * @Date 2021/9/28 11:23
 * @Version 1.0
 */
@RestController
public class LimiterController {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    @Limit(key = "test", period = 100, count = 10)
    @GetMapping("/test")
    public int testLimiter() {
        // 意味著 100S 内最多允許訪問10次
        return ATOMIC_INTEGER.incrementAndGet();
    }
}
