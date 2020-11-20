package com.zhuaer.learning.mq.rabbit.handler;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @ClassName ConsumerListener
 * @Description 消费者
 * @Author zhua
 * @Date 2020/8/11 15:01
 * @Version 1.0
 */
@Component
@Slf4j
public class ConsumerListener {

    /**
     * 监听某个队列的消息
     * @param message 接收到的消息
     */
    @RabbitListener(queues = "topic_queue")
    public void myListener(String message){
        //不用在手动转UTF-8 Spring自动转好了
        System.out.println(message);
        log.debug("消费者接收到的消息为：{}", message);
    }

    /**
     * 监听某个队列的消息
     * @param message 接收到的消息
     */
    @SneakyThrows
    @RabbitListener(queues = "topic_queue")
    public void myListener(Message message , Channel channel){
        log.info("receive: " + new String(message.getBody())+"《线程名：》"+Thread.currentThread().getName()+"《线程id:》"+Thread.currentThread().getId());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);

    }
}
