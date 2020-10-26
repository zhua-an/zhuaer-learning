package com.zhuaer.learning.redisson.delayed.controller;

import com.zhuaer.learning.redisson.delayed.config2.RedisDelayedQueue2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName RedisTestController
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/21 15:43
 * @Version 1.0
 */
@RestController
@RequestMapping(value = "/test")
public class RedisTestController2 {

    @Autowired
    private RedisDelayedQueue2 redisDelayedQueue;

    @GetMapping("/redis2")
    private void test(){
        redisDelayedQueue.redissonDelay("1", "延迟消息", 10L);

    }
}
