package com.zhuaer.learning.sentinel.config;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ClassName SentinelBlockHandlerConfig
 * @Description 全局的限流异常处理
 * @Author zhua
 * @Date 2020/9/22 10:22
 * @Version 1.0
 */
@Slf4j
@ControllerAdvice
@Order(0)
public class SentinelBlockHandlerConfig {

    @ExceptionHandler(BlockException.class)
    @ResponseBody
    public String sentinelBlockHandler(BlockException e) {
        log.warn("Blocked by Sentinel: {}", e.getRule());
        // Return the customized result.
        return "error";
    }
}
