package com.zhuaer.learning.redisson.delayed.config;

import com.zhuaer.learning.redisson.delayed.dto.TaskBodyDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName TestListener
 * @Description 监听器
 * @Author zhua
 * @Date 2020/8/21 15:45
 * @Version 1.0
 */
@Component
@Slf4j
public class TestListener implements RedisDelayedQueueListener<TaskBodyDTO> {

    @Override
    public void invoke(TaskBodyDTO taskBodyDTO) {
        //这里调用你延迟之后的代码
        log.info("执行...." + taskBodyDTO.getBody() + "===" + taskBodyDTO.getName());
    }
}
