package com.zhuaer.learning.mq.rabbit.mvc.handler;

//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import java.io.IOException;

/**
 * @ClassName RecvHandler
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/17 19:09
 * @Version 1.0
 */
public class RecvHandler implements MessageListener {
//    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void onMessage(Message msg) {
        try {
            // msg就是rabbitmq传来的消息，需要的同学自己打印看一眼
            // 使用jackson解析
//            JsonNode jsonData = MAPPER.readTree(msg.getBody());
//            System.out.println("我是可爱的小猪,我的id是" + jsonData.get("id").asText() + ",我的名字是" + jsonData.get("name").asText());
            System.out.println("我是可爱的小猪,我的id是" + new String(msg.getBody()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
