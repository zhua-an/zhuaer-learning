package com.zhuaer.mq.rabbit.mvc.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * @ClassName RabbitmqConfig
 * @Description TODO
 * @Author zhua
 * @Date 2020/8/17 14:49
 * @Version 1.0
 */
@PropertySource("classpath:rabbitmq.properties")
@Configuration
public class RabbitmqConfig {

    public final static  String EXCHANGE_NAME = "TEST_EX";
    public final static String QUEUE_NAME = "QUEUE_TEST";
    public final static String ROUTING_KEY ="ROUTING_KEY";

    @Autowired
    Environment env;

    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory factory = new CachingConnectionFactory(
                env.getProperty("rabbitmq.host"),
                env.getProperty("rabbitmq.port", Integer.class)
        );
        factory.setUsername(env.getProperty("rabbitmq.username"));
        factory.setPassword(env.getProperty("rabbitmq.password"));
        factory.setVirtualHost(env.getProperty("rabbitmq.virtualHost"));

//        factory.setUri("amqp://admin:123456@192.168.1.131:5672");
        return factory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        //设置启动spring容器时自动加载这个类(这个参数现在默认已经是true，可以不用设置)
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());

        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        template.setRetryTemplate(retryTemplate);
        //template.setRoutingKey(env.getProperty("rabbitmq.produce.queuename"));
        return template;
    }

    @Bean
    public Queue myQueue() {
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanout:"+ EXCHANGE_NAME);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(this.myQueue()).to(this.exchange()).with(ROUTING_KEY);
    }

    @Bean
    public Binding fanoutBinding() {
        return BindingBuilder.bind(this.myQueue()).to(this.fanoutExchange());
    }


    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }


    /*
     *     简单消息监听容器
     */
//    @Bean
//    public SimpleMessageListenerContainer messageContainer(ConnectionFactory connectionFactory) {
//        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
//        //同时监听多个队列
//        container.setQueues(queue001(), queue002(), queue003(), queue_image(), queue_pdf());
//        //设置当前的消费者数量
//        container.setConcurrentConsumers(1);
//        container.setMaxConcurrentConsumers(5);
//        //设置是否重回队列
//        container.setDefaultRequeueRejected(false);
//        //设置自动签收
//        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
//        //设置监听外露
//        container.setExposeListenerChannel(true);
//        //设置消费端标签策略
//        container.setConsumerTagStrategy(new ConsumerTagStrategy() {
//            @Override
//            public String createConsumerTag(String queue) {
//                return queue + "_" + UUID.randomUUID().toString();
//            }
//        });
//        //设置消息监听
//        container.setMessageListener(new ChannelAwareMessageListener() {
//            @Override
//            public void onMessage(Message message, Channel channel) throws Exception {
//                String msg = new String(message.getBody(), "utf-8");
//                System.out.println("-----------消费者：" + msg);
//            }
//        });
//        return container;
//    }

    /*@Bean
    public MessageListener messageListener() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return  (MessageListener) Class.forName(env.getProperty("rabbitmq.listener.class")).newInstance();
    }*/

    @Bean
    public ChannelAwareMessageListener channelAwareMessageListener() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return  (ChannelAwareMessageListener) Class.forName(env.getProperty("rabbitmq.listener.class")).newInstance();
    }

}
