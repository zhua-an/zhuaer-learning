package com.zhuaer.learning.mqtt.service.handler;

import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * @ClassName MqttMessageSender
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/24 17:43
 * @Version 1.0
 */
public interface MqttMessageSender {

    void sendMessage(@Header(MqttHeaders.TOPIC) String topic, String message);
}
