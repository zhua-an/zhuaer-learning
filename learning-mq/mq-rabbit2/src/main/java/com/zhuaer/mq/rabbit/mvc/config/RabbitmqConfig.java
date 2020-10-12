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
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
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
    @Autowired
    private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;


    /**
     * 定义连接工厂
     * @return
     */
    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory factory = new CachingConnectionFactory(
                env.getProperty("rabbitmq.host"),
                env.getProperty("rabbitmq.port", Integer.class)
        );
        factory.setUsername(env.getProperty("rabbitmq.username"));
        factory.setPassword(env.getProperty("rabbitmq.password"));
        factory.setVirtualHost(env.getProperty("rabbitmq.virtualHost"));

//        factory.setUri("amqp://admin:123456@127.0.0.1:5672");

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
        //全局设置rabbitTemplate绑定的消息队列
        //template.setRoutingKey(env.getProperty("rabbitmq.produce.queuename"));
        return template;
    }

    /**
     * 定义队列
     * @return
     */
    @Bean
    public Queue myQueue() {
        return new Queue(QUEUE_NAME);
    }

    /**
     * Direct交换机
     * @return
     */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    /**
     * Fanout交换机
     * @return
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanout:"+ EXCHANGE_NAME);
    }

    /**
     * Direct交换机绑定队列
     * @return
     */
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(this.myQueue()).to(this.exchange()).with(ROUTING_KEY);
    }

    /**
     * Fanout交换机绑定队列
     * @return
     */
    @Bean
    public Binding fanoutBinding() {
        return BindingBuilder.bind(this.myQueue()).to(this.fanoutExchange());
    }

    /**
     * 单一消费者
     * @return
     */
    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        //消息类型转换器
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        //调整批量获取消息条数
        factory.setPrefetchCount(1);
        factory.setBatchSize(1);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }

    /**
     * 多个消费者
     * @return
     */
    @Bean(name = "multiListenerContainer")
    public SimpleRabbitListenerContainerFactory multiListenerContainer(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factoryConfigurer.configure(factory,connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        //并发设置
        //并发消费者的初始化值
        factory.setConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.concurrency",int.class));
        //并发消费者的最大值
        factory.setMaxConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.max-concurrency",int.class));
        //每个消费者每次监听时可拉取处理的消息数量
        factory.setPrefetchCount(env.getProperty("spring.rabbitmq.listener.prefetch",int.class));
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

//    /**
//     * 定义消息监听容器
//     * @return
//     * @throws InstantiationException
//     * @throws IllegalAccessException
//     * @throws ClassNotFoundException
//     */
//    @Bean
//    public ChannelAwareMessageListener channelAwareMessageListener() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
//        return  (ChannelAwareMessageListener) Class.forName(env.getProperty("rabbitmq.listener.class")).newInstance();
//    }

}
