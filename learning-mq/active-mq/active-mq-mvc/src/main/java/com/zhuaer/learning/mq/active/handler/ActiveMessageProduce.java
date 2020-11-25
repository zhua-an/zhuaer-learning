package com.zhuaer.learning.mq.active.handler;

import org.apache.activemq.ScheduledMessage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.jms.TextMessage;

/**
 * @ClassName ActiveMessageProduce
 * @Description 消息生产者
 * @Author zhua
 * @Date 2020/11/25 11:48
 * @Version 1.0
 */
public class ActiveMessageProduce {

    private JmsTemplate jmsTemplate;

    public void send() {
        WebApplicationContext webctx = ContextLoader.getCurrentWebApplicationContext();
        this.jmsTemplate = (JmsTemplate) webctx.getBean("jmsTemplate");
        /*队列生产者*/
        jmsTemplate.send(session -> {
            TextMessage textMessage = session.createTextMessage("发送的消息内容");

            /**
             * 延迟投递
             * 首先在ActiveMQ的安装路径 /conf/activemq.xml 修改配置文件  增加：schedulerSupport="true"
             */
//            //延迟投递的时间 毫秒
//            textMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,4000);
//            //重复投递的的时间间隔 毫秒
//            textMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD,4000);
//            //重复投递的次数
//            textMessage.setIntProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT,3);
            return textMessage;
        });
    }
}
