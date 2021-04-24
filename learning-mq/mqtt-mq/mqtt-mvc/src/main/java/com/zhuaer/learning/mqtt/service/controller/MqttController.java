package com.zhuaer.learning.mqtt.service.controller;

import com.zhuaer.learning.mqtt.service.MqttService;
import com.zhuaer.learning.mqtt.service.handler.MqttMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName MqttController
 * @Description TODO
 * @Author zhua
 * @Date 2021/4/24 17:25
 * @Version 1.0
 */
@RestController
@RequestMapping("/mqttController")
@Slf4j
public class MqttController {

    @Autowired
    private MqttService mqttService;

    @Autowired
    private MqttMessageSender mqttMessageSender;

    @RequestMapping(value = "testSend")
    @ResponseBody
    public String testSend(HttpServletRequest request, HttpServletResponse response) {
        try {
            String topic = "TOPIC_TEST";
            String content = "content";
            this.mqttService.send(topic,content);
            this.mqttMessageSender.sendMessage(topic, content);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("发送失败", ex);
        }
        return "发送成功";
    }
}
