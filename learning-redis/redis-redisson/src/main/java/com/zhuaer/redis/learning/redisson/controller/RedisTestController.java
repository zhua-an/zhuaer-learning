package com.zhuaer.redis.learning.redisson.controller;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName RedisTestController
 * @Description TODO
 * @Author zhua
 * @Date 2020/7/20 10:40
 * @Version 1.0
 */
@RestController
public class RedisTestController {

    @Autowired
    private RedissonClient redissonClient;

    @RequestMapping("test1")
    public String test1() {
        // 设置字符串
        RBucket<String> keyObj = redissonClient.getBucket("k1");
        keyObj.set("v1236");
        return "success";
    }

}
