package com.zhuaer.learning.mq.rabbit.mvc.handler;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

/**
 * @ClassName RecvHandler2
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/21 16:42
 * @Version 1.0
 */
@Component
public class RecvHandler2 extends MessageListenerAdapter {

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try{
            System.out.println("======RecvHandler2消费消息："+ new String(message.getBody(), "utf-8"));
            channel.basicAck(deliveryTag, false);

            //消息的标识，false只确认当前一个消息收到，true确认所有consumer获得的消息
            //channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); //确认成功收到消息
            //ack返回false，并重新回到队列，api里面解释得很清楚
            //channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            //拒绝消息
            //channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }catch (Exception e) {
            e.printStackTrace();
            //4.拒绝签收
            /*
            第三个参数：requeue：重回队列。如果设置为true，则消息重新回到queue，broker会重新发送该消息给消费端
             */
            channel.basicNack(deliveryTag,true,true);
            // 了解
            //channel.basicReject(deliveryTag,true);
        }

    }
}
