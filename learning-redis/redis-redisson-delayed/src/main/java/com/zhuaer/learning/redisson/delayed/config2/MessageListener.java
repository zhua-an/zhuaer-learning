package com.zhuaer.learning.redisson.delayed.config2;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryExpiredListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @ClassName MessageListener
 * @Description 接收过期时间并执行方法
 * @Author zhua
 * @Date 2020/10/22 19:12
 * @Version 1.0
 */
@Slf4j
@Component
public class MessageListener implements EntryExpiredListener<String, String> {

    @Override
    public void onExpired(EntryEvent<String, String> entryEvent) {
        log.info("有收到延迟消息通知：{}", entryEvent.getKey());
        log.info("当前时间：{},收到数据key：{}，value：{}", new Date().toString(), entryEvent.getKey(), entryEvent.getValue());
    }
}
