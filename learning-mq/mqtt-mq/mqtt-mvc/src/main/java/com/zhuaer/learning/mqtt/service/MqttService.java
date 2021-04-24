package com.zhuaer.learning.mqtt.service;

import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * @ClassName MqttService
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/24 17:22
 * @Version 1.0
 */
public interface MqttService {

    public void send(@Header(MqttHeaders.TOPIC) String topic,String content) throws Exception;
}
