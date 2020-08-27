package com.zhuaer.mq.rabbit.mvc.handler;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * @ClassName ServiceMessageListener
 * @Description 消息监听类虚实现MessageListener接口
 * @Author zhua
 * @Date 2020/8/19 13:50
 * @Version 1.0
 */
@Component
public class ServiceMessageListener2 implements MessageListener {

    //消息监听类虚实现MessageListener接口
    @Override
    public void onMessage(Message message) {
        try {
            //消息内容
            String body = new String(message.getBody(), "UTF-8");
            System.out.println("消息内容：：：：：：：：：：："+body);
            //消息队列
            System.out.println(message.getMessageProperties().getConsumerQueue());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
