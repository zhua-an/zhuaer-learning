package com.zhuaer.mq.rabbit.mvc.handler;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ClassName ServiceMessageListener
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/19 13:50
 * @Version 1.0
 */
@Component
public class ServiceMessageListener implements ChannelAwareMessageListener {
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        // TODO Auto-generated method stub
        String body = new String(message.getBody(), "UTF-8");
        System.out.println("消息内容：：：：：：：：：：："+body);
        System.out.println(message.getMessageProperties().getConsumerQueue());
        boolean mqFlag=false;//业务处理
        //还有一个点就是如何获取mq消息的报文部分message？
        if(mqFlag){
            basicACK(message,channel);//处理正常--ack
        }else{
            basicNACK(message,channel);//处理异常--nack
        }
    }

    //正常消费掉后通知mq服务器移除此条mq
    private void basicACK(Message message,Channel channel){
        try{
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch(IOException e){
            System.out.println("通知服务器移除mq时异常，异常信息："+e);
        }
    }
    //处理异常，mq重回队列
    private void basicNACK(Message message,Channel channel){
        try{
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false,true);
        }catch(IOException e){
            System.out.println("mq重新进入服务器时出现异常，异常信息："+e);
        }
    }
}
