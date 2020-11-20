package com.zhuaer.learning.mq.active.handler;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * @ClassName ActiveMqConsumer
 * @Description TODO
 * @Author zhua
 * @Date 2020/11/20 15:02
 * @Version 1.0
 */

@Component
public class ActiveMqConsumer {

//    destination对应配置类中ActiveMQQueue("springboot.queue")设置的名字
//    @JmsListener(destination = "sms.queue")
//    public void receiveMsg(String text) {
//        System.out.println("接收到消息 : "+text);
//    }
//
//
//    @JmsListener(destination = "sms.topic")
//    public void receiveTopic1(String text) {
//        System.out.println("receiveTopic1接收到Topic消息 : " + text);
//    }
//
//    @JmsListener(destination = "sms.topic")
//    public void receiveTopic2(String text) {
//        System.out.println("receiveTopic2接收到Topic消息 : " + text);
//    }

    //destination对应配置类中ActiveMQTopic("springboot.topic")设置的名字
    //containerFactory对应配置类中注册JmsListenerContainerFactory的bean名称
    @JmsListener(destination = "sms.queue", containerFactory = "queueListenerFactory")
    public void receiveMsg(String text) {
        System.out.println("接收到消息 : " + text);
    }

    @JmsListener(destination = "sms.topic", containerFactory = "topicListenerFactory")
    public void receiveTopic1(String text) {
        System.out.println("receiveTopic1接收到Topic消息 : " + text);
    }


}
