package com.zhuaer.mq.rabbit.mvc.handler;

import org.springframework.amqp.core.Message;

import java.util.Map;

/**
 * @ClassName MessageHandler
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/19 11:37
 * @Version 1.0
 */
public class MessageHandler {

    public void onMessage(byte[] message){
        System.out.println("---------onMessage----byte-------------");
        System.out.println(new String(message));
    }


    public void onMessage(String message){
        System.out.println("---------onMessage---String-------------");
        System.out.println(message);
    }

    public void onMessage(Map message){
        System.out.println("---------onMessage---Map-------------");
        System.out.println(message);
    }
}
