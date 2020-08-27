package com.zhuaer.learning.redisson.delayed.controller;

import com.zhuaer.learning.redisson.delayed.config.RedisDelayedQueue;
import com.zhuaer.learning.redisson.delayed.config.TestListener;
import com.zhuaer.learning.redisson.delayed.dto.TaskBodyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName RedisTestController
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/21 15:43
 * @Version 1.0
 */
@RestController
@RequestMapping(value = "/test")
public class RedisTestController {

    @Autowired
    private RedisDelayedQueue redisDelayedQueue;

    @GetMapping("/redis")
    private void test(){
        TaskBodyDTO taskBody = new TaskBodyDTO();
        taskBody.setBody("测试DTO,3秒之后执行");
        taskBody.setName("测试DTO,3秒之后执行");
        //添加队列3秒之后执行
        redisDelayedQueue.addQueue(taskBody, 10, TimeUnit.SECONDS, TestListener.class.getName());
        taskBody.setBody("测试DTO,10秒之后执行");
        taskBody.setName("测试DTO,10秒之后执行");
        //添加队列10秒之后执行
        redisDelayedQueue.addQueue(taskBody, 20, TimeUnit.SECONDS, TestListener.class.getName());
        taskBody.setBody("测试DTO,20秒之后执行");
        taskBody.setName("测试DTO,20秒之后执行");
        //添加队列30秒之后执行
        redisDelayedQueue.addQueue(taskBody, 30, TimeUnit.SECONDS, TestListener.class.getName());
    }
}
