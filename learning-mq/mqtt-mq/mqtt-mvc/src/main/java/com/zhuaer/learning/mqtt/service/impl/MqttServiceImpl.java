package com.zhuaer.learning.mqtt.service.impl;

import com.zhuaer.learning.mqtt.service.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import javax.annotation.Resource;

/**
 * @ClassName MqttServiceImpl
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/24 17:23
 * @Version 1.0
 */
@Slf4j
public class MqttServiceImpl implements MqttService {

    @Resource
    private MqttPahoMessageHandler mqttHandler;

    @Override
    public void send(String topic, String content) throws Exception {
        // 构建消息
        Message<String> messages =
                MessageBuilder.withPayload(content).setHeader(MqttHeaders.TOPIC, topic).build();
        // 发送消息
        mqttHandler.handleMessage(messages);
    }

}
