# mq-rabbit2
springboot 配置文件配置 rabbitMQ

# SpringBoot 整合 RabbitMQ 实战

## 添加依赖
    <!-- RabbitMQ的Java Client库 -->
    <dependency>
    	<groupId>com.rabbitmq</groupId>
    	<artifactId>amqp-client</artifactId>
    	<version>5.9.0</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.springframework.amqp/spring-amqp -->
    <dependency>
    	<groupId>org.springframework.amqp</groupId>
    	<artifactId>spring-amqp</artifactId>
    	<version>2.2.10.RELEASE</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.springframework.amqp/spring-rabbit -->
    <dependency>
    	<groupId>org.springframework.amqp</groupId>
    	<artifactId>spring-rabbit</artifactId>
    	<version>2.2.10.RELEASE</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/org.springframework.retry/spring-retry -->
    <dependency>
    	<groupId>org.springframework.retry</groupId>
    	<artifactId>spring-retry</artifactId>
    	<version>1.3.0</version>
    </dependency>
    

##  配置rabbitMq属性
新增 `rabbitmq.properties` 配置文件

    #IP地址
    rabbitmq.host=127.0.0.1
    #端口号
    rabbitmq.port=5672
    #用户名
    rabbitmq.username=root
    #密码
    rabbitmq.password=123456
    #虚拟host
    rabbitmq.virtualHost=/user_center
    
    #并发处理
    spring.rabbitmq.listener.concurrency=10
    spring.rabbitmq.listener.max-concurrency=20
    spring.rabbitmq.listener.prefetch=5
    
    #消费者监听的队列queue
    rabbitmq.queuenames=rabbitMQ_test2,rabbitMQ_pro_server,rabbitMQ_pro_wecaht
    #生产者监听消息queue
    rabbitmq.produce.queuename=rabbitMQ_pro_server
    
    #消息监听类
    rabbitmq.listener.class=com.zhuaer.mq.rabbit.mvc.handler.ServiceMessageListener

## 新增rabbitMq初始化配置

接下来，我们需要以 Configuration 的方式配置 RabbitMQ 并以 Bean 的方式显示注入 RabbitMQ 在发送接收处理消息时相关 Bean 组件配置其中典型的配置是 `RabbitTemplate` 以及 `SimpleRabbitListenerContainerFactory`，
前者是充当消息的发送组件，后者是用于管理  RabbitMQ监听器listener 的容器工厂，其代码如下：

    @PropertySource("classpath:rabbitmq.properties")
    @Configuration
    public class RabbitmqConfig {
    
        public final static  String EXCHANGE_NAME = "TEST_EX";
        public final static String QUEUE_NAME = "QUEUE_TEST";
        public final static String ROUTING_KEY ="ROUTING_KEY";
    
        @Autowired
        Environment env;
    
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
         * 定义消息监听容器
         * @param connectionFactory
         * @return
         */
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
    
## 发送消息

使用RabbitTemplate给消息队列发送消息

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void send() {
    	for (int i = 0; i <20 ; i++) {
    		this.rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_NAME ,RabbitmqConfig.ROUTING_KEY, new Persion("zhangsn"+i,new Date()));
    	}
    }
    
## 消费消息

### 通过配置文件定义消息监听容器未指定消息消费端后，自定义消息消费端

-  实现`MessageListener`接口，重写`onMessage`方法


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
    
- 实现`ChannelAwareMessageListener`接口，重写`onMessage`方法，设置消息确认
对于某些消息而言，我们有时候需要严格的知道消息是否已经被 consumer 监听消费处理了，即我们有一种消息确认机制来保证我们的消息是否已经真正的被消费处理。在 RabbitMQ 中，消息确认处理机制有三种：Auto - 自动、Manual - 手动、None - 无需确认，而确认机制需要 listener 实现 ChannelAwareMessageListener 接口，并重写其中的确认消费逻辑


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

### 定义消费者容器的同时实现消费者

    @Configuration
    public class RecvHandler {
    
        // 消费者 监听某个队列 方式一
        @Bean
        public SimpleMessageListenerContainer messageContainer(ConnectionFactory connectionFactory) {
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
            container.setQueues(new Queue(RabbitmqConfig.QUEUE_NAME));
            container.setExposeListenerChannel(true);
            container.setMaxConcurrentConsumers(1);
            container.setConcurrentConsumers(1);
            // 设置确认模式手工确认
            container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
            container.setMessageListener(new ChannelAwareMessageListener() {
                @Override
                public void onMessage(Message message, Channel channel) throws Exception {
                    byte[] body = message.getBody();
                    System.out.println("1  receive msg : " + JSONObject.parseObject(new String(body)));
                    //不读取消息并且将当前消息抛弃掉，消息队列中删除当前消息
                    //channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
                    //不读取消息，消息队列中保留当前消息未被查看状态
                    //channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
    
                    //确认消息成功消费，删除消息队列中的消息
                    // channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    //确认消息成功消费，删除消息队列中的消息，他跟上面貌似一样
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                }
            });
            return container;
        }
    
        @Bean
        public SimpleMessageListenerContainer messageContainer2(ConnectionFactory connectionFactory) {
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
            container.setQueues(new Queue(RabbitmqConfig.QUEUE_NAME));
            container.setExposeListenerChannel(true);
            container.setMaxConcurrentConsumers(1);
            container.setConcurrentConsumers(1);
            // 设置确认模式手工确认
            container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
            container.setMessageListener(new ChannelAwareMessageListener() {
                @Override
                public void onMessage(Message message, Channel channel) throws Exception {
                    byte[] body = message.getBody();
                    System.out.println("  2  receive msg : " + JSONObject.parseObject(new String(body)));
                    //不读取消息并且将当前消息抛弃掉，消息队列中删除当前消息
                    //channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
                    //不读取消息，消息队列中保留当前消息未被查看状态
                    //channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
    
                    //确认消息成功消费，删除消息队列中的消息
                    // channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    //确认消息成功消费，删除消息队列中的消息，他跟上面貌似一样
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
                }
            });
            return container;
        }
    
        // 消费者 监听某个队列 方式二
        @Bean
        public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory){
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.setQueueNames(RabbitmqConfig.QUEUE_NAME);
    
            MessageListenerAdapter adapter = new MessageListenerAdapter(new MessageHandler());
            //指定Json转换器
            adapter.setMessageConverter(new TextMessageConverter());
            //设置处理器的消费消息的默认方法
            adapter.setDefaultListenerMethod("onMessage");
            container.setMessageListener(adapter);
    
            return container;
        }
    }


