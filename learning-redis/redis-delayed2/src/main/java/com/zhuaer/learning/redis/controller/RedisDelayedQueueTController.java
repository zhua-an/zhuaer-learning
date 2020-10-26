package com.zhuaer.learning.redis.controller;

import com.alibaba.fastjson.JSON;
import com.zhuaer.learning.redis.config.RedisDelayedQueueT;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * @ClassName RedisDelayedQueueTController
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/21 17:20
 * @Version 1.0
 */
@RestController
@RequestMapping(value = "/test")
public class RedisDelayedQueueTController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    private RedisDelayedQueueT<String> redisDelayedQueue = new RedisDelayedQueueT<>("test-queue", String.class, this::head);
    /**
     * 异步执行方法
     * @param t
     * @param <T>
     */
    @Async
    public <T> void head(T t) {
        System.out.println("执行方法"+ JSON.toJSONString(t));
    }


    @GetMapping("/redisT")
    public String addTest(){
        redisDelayedQueue.setStringRedisTemplate(stringRedisTemplate);
        redisDelayedQueue.putForDelayedTime("测试延时定时任务",5000);
        return "success";
    }
}
