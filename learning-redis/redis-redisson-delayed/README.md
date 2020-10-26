# redis-redisson-delayed

springboot redisson 延时队列

## 添加依赖

    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson-spring-boot-starter</artifactId>
        <version>3.13.6</version>
    </dependency>

# 方式一

## application.yml 配置redis信息

官方文档：[https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95](https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95 "https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95")

redisson官方发布了redisson-spring-boot-starter，具体可以参考：[https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter#spring-boot-starter](https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter#spring-boot-starter "https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter#spring-boot-starter")

可以使用普通的springboot设置或Redisson设置。

    # common spring boot settings
    
    spring:
      redis:
        database: 
        host:
        port:
        password:
        ssl: 
        timeout:
        cluster:
          nodes:
        sentinel:
          master:
          nodes:
    
      # Redisson settings
        
    #path to config - redisson.yaml
    spring:
     redis: 
      redisson: 
        file: classpath:redisson.yaml
        config: |
          clusterServersConfig:
            idleConnectionTimeout: 10000
            connectTimeout: 10000
            timeout: 3000
            retryAttempts: 3
            retryInterval: 1500
            failedSlaveReconnectionInterval: 3000
            failedSlaveCheckInterval: 60000
            password: null
            subscriptionsPerConnection: 5
            clientName: null
            loadBalancer: !<org.redisson.connection.balancer.RoundRobinLoadBalancer> {}
            subscriptionConnectionMinimumIdleSize: 1
            subscriptionConnectionPoolSize: 50
            slaveConnectionMinimumIdleSize: 24
            slaveConnectionPoolSize: 64
            masterConnectionMinimumIdleSize: 24
            masterConnectionPoolSize: 64
            readMode: "SLAVE"
            subscriptionMode: "SLAVE"
            nodeAddresses:
            - "redis://127.0.0.1:7004"
            - "redis://127.0.0.1:7001"
            - "redis://127.0.0.1:7000"
            scanInterval: 1000
            pingConnectionInterval: 0
            keepAlive: false
            tcpNoDelay: false
          threads: 16
          nettyThreads: 32
          codec: !<org.redisson.codec.FstCodec> {}
          transportMode: "NIO"

实战配置：

    spring:
      application:
        name: @artifactId@
      redis:
        redisson:
          # 配置单点模式
          file: "classpath:redisson.yaml"
          
## 开启监听类RedisDelayedQueueInit


    /**
     * @ClassName RedisDelayedQueueInit
     * @Description 初始化队列监听
     * @Author zhua
     * @Date 2020/8/21 15:38
     * @Version 1.0
     */
    @Component
    @Slf4j
    public class RedisDelayedQueueInit implements ApplicationContextAware {
    
        @Autowired
        RedissonClient redissonClient;
    
        /**
         * 获取应用上下文并获取相应的接口实现类
         *
         * @param applicationContext
         * @throws BeansException
         */
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            Map<String, RedisDelayedQueueListener> map = applicationContext.getBeansOfType(RedisDelayedQueueListener.class);
            map.entrySet().stream().forEach(taskEventListenerEntry->{
                String listenerName = taskEventListenerEntry.getValue().getClass().getName();
                startThread(listenerName, taskEventListenerEntry.getValue());
            });
        }
    
        /**
         * 启动线程获取队列*
         *
         * @param queueName                 queueName
         * @param redisDelayedQueueListener 任务回调监听
         * @param <T>                       泛型
         * @return
         */
        private <T> void startThread(String queueName, RedisDelayedQueueListener redisDelayedQueueListener) {
            RBlockingQueue<T> blockingFairQueue = redissonClient.getBlockingQueue(queueName);
            //由于此线程需要常驻，可以新建线程，不用交给线程池管理
            Thread thread = new Thread(() -> {
                log.info("启动监听队列线程" + queueName);
                while (true) {
                    try {
                        T t = blockingFairQueue.take();
                        log.info("监听队列线程{},获取到值:{}", queueName, JSON.toJSONString(t));
                        new Thread(() -> redisDelayedQueueListener.invoke(t)).start();
                    } catch (Exception e) {
                        log.info("监听队列线程错误,", e);
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException ex) {
                            log.error("线程出现异常,", ex);
                        }
                    }
                }
            });
            thread.setName(queueName);
            thread.start();
        }
    
    }
    
## 新建一个接口RedisDelayedQueueListener

    /**
     * @ClassName RedisDelayedQueueListener
     * @Description 队列事件监听接口，需要实现这个方法
     * @Author zhua
     * @Date 2020/8/21 15:39
     * @Version 1.0
     */
    public interface RedisDelayedQueueListener <T> {
        /**
         * 执行方法
         *
         * @param t
         */
        void invoke(T t);
    }

## 新增任务工具类

    /**
     * @ClassName RedisDelayedQueue
     * @Description 队列
     * @Author zhua
     * @Date 2020/8/21 15:33
     * @Version 1.0
     */
    @Component
    @Slf4j
    public class RedisDelayedQueue {
    
        @Autowired
        RedissonClient redissonClient;
    
        /**
         * 添加队列
         *
         * @param t        DTO传输类
         * @param delay    时间数量
         * @param timeUnit 时间单位
         * @param <T>      泛型
         */
        public <T> void addQueue(T t, long delay, TimeUnit timeUnit, String queueName) {
            log.info("添加队列{},delay:{},timeUnit:{}" + queueName, delay, timeUnit);
            RBlockingQueue<T> blockingFairQueue = redissonClient.getBlockingQueue(queueName);
            RDelayedQueue<T> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);
            delayedQueue.offer(t, delay, timeUnit);
            delayedQueue.destroy();
        }
    }
    
## 新建自己的监听类继承上面的接口

    /**
     * @ClassName TestListener
     * @Description 监听器
     * @Author zhua
     * @Date 2020/8/21 15:45
     * @Version 1.0
     */
    @Component
    @Slf4j
    public class TestListener implements RedisDelayedQueueListener<TaskBodyDTO> {
    
        @Override
        public void invoke(TaskBodyDTO taskBodyDTO) {
            //这里调用你延迟之后的代码
            log.info("执行...." + taskBodyDTO.getBody() + "===" + taskBodyDTO.getName());
        }
    }

## 新增DTO

    /**
     * @ClassName TaskBodyDTO
     * @Description TODO
     * @Author zhua
     * @Date 2020/8/21 15:41
     * @Version 1.0
     */
    @Data
    public class TaskBodyDTO implements Serializable {
    
        private String name;
    
        private String body;
    }
    
## 运行测试

    TaskBodyDTO taskBody = new TaskBodyDTO();
    taskBody.setBody("测试DTO,3秒之后执行");
    taskBody.setName("测试DTO,3秒之后执行");
    //添加队列3秒之后执行
    redisDelayedQueue.addQueue(taskBody, 10, TimeUnit.SECONDS, TestListener.class.getName());
    taskBody.setBody("测试DTO,10秒之后执行");
    taskBody.setName("测试DTO,10秒之后执行");
    //添加队列10秒之后执行
    redisDelayedQueue.addQueue(taskBody, 20, TimeUnit.SECONDS, TestListener.class.getName());
    taskBody.setBody("测试DTO,20秒之后执行");
    taskBody.setName("测试DTO,20秒之后执行");
    //添加队列30秒之后执行
    redisDelayedQueue.addQueue(taskBody, 30, TimeUnit.SECONDS, TestListener.class.getName());
    
# 方式二

利用redisson下的封装类RedissonClient调用getMapCache方法 缓存映射名称：它能够保留插入元素的顺序，并且可以指明每个元素的过期时间（专业一点叫元素淘汰机制）。另外还为每个元素提供了监听器，提供了4种不同类型的监听器。有：添加、过期、删除、更新四大事件。当然这里我用“过期”来实现延迟发送 。

## 设置过期时间和监听类

    /**
     * @ClassName RedisDelayedQueue
     * @Description 监听类
     * @Author zhua
     * @Date 2020/10/22 19:12
     * @Version 1.0
     */
    @Slf4j
    @Component
    public class RedisDelayedQueue2 {
    
        @Autowired
        private RedissonClient redissonClient;
    
        @Autowired
        private MessageListener messageListener;
    
        public void redissonDelay(String num, String msg, Long miute) {
            //设置缓存映射的标识 这个标识自定义
            RMapCache<String, String> rMapCache = redissonClient.getMapCache("redisMessage");
            //设置缓存的时间和数据信息 TimeUnit.MILLISECONDS这个是 设置时间单位
    //        rMapCache.put(num, msg, miute, TimeUnit.MILLISECONDS);
            rMapCache.put(num, msg, miute, TimeUnit.SECONDS);
            //监听方法触发类 redis过期就执行这个 messageListene类里的方法
            rMapCache.addListener(messageListener);
        }
    }

## 设置监听的接收实现类

    /**
     * @ClassName MessageListener
     * @Description 接收过期时间并执行方法
     * @Author zhua
     * @Date 2020/10/22 19:12
     * @Version 1.0
     */
    @Slf4j
    @Component
    public class MessageListener implements EntryExpiredListener<String, String> {
    
        @Override
        public void onExpired(EntryEvent<String, String> entryEvent) {
            log.info("有收到延迟消息通知：{}", entryEvent.getKey());
            log.info("当前时间：{},收到数据key：{}，value：{}", new Date().toString(), entryEvent.getKey(), entryEvent.getValue());
        }
    }

## 使用

    redisDelayedQueue.redissonDelay("1", "延迟消息", 10L);
    