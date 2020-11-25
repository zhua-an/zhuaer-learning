package com.zhuaer.learning.mq.active.handler;

import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * @ClassName MessageListener
 * @Description 消息监听器
 * @Author zhua
 * @Date 2020/11/25 11:46
 * @Version 1.0
 */
@Component
public class ActiveMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        if (null!=message && message instanceof TextMessage){
            TextMessage textMessage= (TextMessage) message;
            try {
                System.out.println(textMessage.getText());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
