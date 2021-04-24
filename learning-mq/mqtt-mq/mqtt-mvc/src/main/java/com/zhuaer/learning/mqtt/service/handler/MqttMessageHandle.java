package com.zhuaer.learning.mqtt.service.handler;

import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

/**
 * @ClassName MqttMessageHandle
 * @Description 入站消息接收类
 * @Author zhua
 * @Date 2021/4/24 18:03
 * @Version 1.0
 */
public class MqttMessageHandle {

    public void handleMessage(Message<String> message) throws MessagingException {
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();
        String content = message.getPayload();
        System.out.println("收到消息");
        System.out.println("主题:" + topic);
        System.out.println("内容:" + content);
    }
}