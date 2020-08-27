package com.zhuaer.learning.redis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @ClassName PublishService
 * @Description 发布service
 * @Author zhua
 * @Date 2020/7/17 17:07
 * @Version 1.0
 */
@RequestMapping("/mq")
@RestController
public class PublishController {

    @Autowired
    StringRedisTemplate redisTemplate;


    /**
     * 发布方法、
     * @return
     */
    @RequestMapping("/publish/{id}")
    public String publish(@PathVariable String id) {
        // 该方法封装的 connection.publish(rawChannel, rawMessage);
        for(int i = 1; i <= 5; i++) {
            redisTemplate.convertAndSend("topic", String.format("我是消息{%d}号: %tT", i, new Date()));
        }
        return "success";
    }
}
