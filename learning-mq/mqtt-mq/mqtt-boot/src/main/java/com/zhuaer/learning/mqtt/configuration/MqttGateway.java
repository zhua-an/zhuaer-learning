package com.zhuaer.learning.mqtt.configuration;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @ClassName MqttGateway
 * @Description 推送接口
 * 对外提供推送消息的接口
 * 1. 使用@MessagingGateway注解，配置MQTTMessageGateway消息推送接口
 * 2. 使用defaultRequestChannel值，调用时将向其发送消息的默认通道
 * 3. 配置灵活的topic主题
 * @Author zhua
 * @Date 2021/4/24 15:57
 * @Version 1.0
 */
@Component
@MessagingGateway(defaultRequestChannel = MqttConfig.OUTBOUND_CHANNEL)
public interface MqttGateway {

    void sendToMqtt(String payload);

    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, String payload);

    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos, String payload);

}
