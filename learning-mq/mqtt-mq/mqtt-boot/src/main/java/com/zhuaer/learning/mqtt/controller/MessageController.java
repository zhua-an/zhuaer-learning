package com.zhuaer.learning.mqtt.controller;

import com.zhuaer.learning.mqtt.configuration.MqttGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName MessageController
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/24 15:57
 * @Version 1.0
 */
@RestController
public class MessageController {

    @Autowired
    MqttGateway mqttGateway;

    /***
     * 发布消息，用于其他客户端消息接收测试
     */

    @RequestMapping("/sendMqttMessage")
    public String sendMqttMessage(String message, String topic) {
        mqttGateway.sendToMqtt(topic, message);
        return "ok";
    }
}

