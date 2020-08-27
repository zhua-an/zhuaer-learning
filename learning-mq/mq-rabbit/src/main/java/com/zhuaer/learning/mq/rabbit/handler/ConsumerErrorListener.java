package com.zhuaer.learning.mq.rabbit.handler;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName ConsumerListener
 * @Description 消费者
 * @Author zhua
 * @Date 2020/8/11 15:01
 * @Version 1.0
 */
@Component
@Slf4j
public class ConsumerErrorListener {

    /**
     * 监听某个队列的消息
     * @param message 接收到的消息
     */
    @SneakyThrows
    @RabbitListener(queues = "topic_queue",
            // 注意此类为spring容器中的bean名称
            errorHandler = "myReceiverListenerErrorHandler",
            returnExceptions = "false")
    public void myListener(Message message , Channel channel){
        log.info("receive: " + new String(message.getBody())+"《线程名：》"+Thread.currentThread().getName()+"《线程id:》"+Thread.currentThread().getId());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        try {
            //告诉服务器收到这条消息 无需再发了 否则消息服务器以为这条消息没处理掉 后续还会在发
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            log.info("receiver success");
        } catch (IOException e) {
            e.printStackTrace();
            //丢弃这条消息
            log.info("receiver fail");
        }
        throw new RuntimeException("业务异常");
    }

    @Component(value = "myReceiverListenerErrorHandler")
    public class MyReceiverListenerErrorHandler implements RabbitListenerErrorHandler {

//        private static ConcurrentSkipListMap<Object, AtomicInteger> map = new ConcurrentSkipListMap();

        /**
         */
        @Override
        public Object handleError(Message amqpMessage,
                                  org.springframework.messaging.Message <?> message,
                                  ListenerExecutionFailedException exception)
                throws Exception {
            log.error("消息接收发生了错误，消息内容:{},错误信息:{}",
                    JSON.toJSONString(message.getPayload()),
                    JSON.toJSONString(exception.getCause().getMessage()));
            throw new AmqpRejectAndDontRequeueException("超出次数");
        }
    }

}
