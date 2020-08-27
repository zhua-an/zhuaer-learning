package com.zhuaer.learning.redis.bean;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * @ClassName SubscribeListener
 * @Description 订阅监听类
 * @Author zhua
 * @Date 2020/7/17 17:05
 * @Version 1.0
 */
public class SubscribeListener implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] bytes) {
        // 缓存消息是序列化的，需要反序列化。然而new String()可以反序列化，但静态方法valueOf()不可以
        System.out.println(new String(bytes) + "主题发布：" + new String(message.getBody()));
    }
}
