package com.zhuaer.learning.sentinel.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Description TODO
 * @Author zhua
 * @Date 2020/9/2 18:37
 * @Version 1.0
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping(value = "/hello")
    public String hello() {
        return "Hello Sentinel";
    }

    @GetMapping(value = "/test")
    @SentinelResource(value = "test", blockHandler = "exceptionHandler")
    public String test() {
        return "sucessful";
    }

    // 限流与熔断处理
    public String exceptionHandler(BlockException ex){
        System.out.println("进入熔断");
        return "handler->Exception ->success";
    }
}
