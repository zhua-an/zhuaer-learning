# mq-rabbit-mvc
*SpringMVC + rabbitmq 集成*

## 添加依赖

    <dependency>
      <groupId>org.springframework.amqp</groupId>
      <artifactId>spring-rabbit</artifactId>
      <version>2.2.10.RELEASE</version>
    </dependency>
    
### 增加rabbitMQ xml配置文件

### 方式一（springMVC 原生配置方式）

    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:context="http://www.springframework.org/schema/context"
           xmlns:util="http://www.springframework.org/schema/util"
           xsi:schemaLocation="http://www.springframework.org/schema/rabbit
     	http://www.springframework.org/schema/rabbit/spring-rabbit.xsd
         http://www.springframework.org/schema/context
         http://www.springframework.org/schema/context/spring-context-4.0.xsd
         http://www.springframework.org/schema/util
         http://www.springframework.org/schema/util/spring-util-2.0.xsd
     	http://www.springframework.org/schema/beans
     	http://www.springframework.org/schema/beans/spring-beans-4.0.xsd">
    
        <!-- 根据实际情况修改一下代码，其他包含setter 注入跟 constructor 注入 -->
    
        <!--配置rabbitmq开始-->
        <bean id="connectionFactoryMq" class="org.springframework.amqp.rabbit.connection.CachingConnectionFactory">
            <constructor-arg value="192.168.181.201"/>
            <property name="username" value="admin"/>
            <property name="password" value="admin"/>
            <property name="host" value="127.0.0.1"/>
            <property name="port" value="5672"/>
            <property name="publisherReturns" value="true"/>
        </bean>
        <bean id="rabbitAdmin" class="org.springframework.amqp.rabbit.core.RabbitAdmin">
            <constructor-arg ref="connectionFactoryMq"/>
        </bean>
        <!--创建rabbitTemplate消息模板类-->
        <bean id="rabbitTemplate" class="org.springframework.amqp.rabbit.core.RabbitTemplate">
            <constructor-arg ref="connectionFactoryMq"/>
            <!--消息确认回调 -->
            <property name="confirmCallback" ref="rmqProducer"/>
        </bean>
        <!--创建消息转换器为SimpleMessageConverter-->
        <bean id="serializerMessageConverter" class="org.springframework.amqp.support.converter.SimpleMessageConverter">
        </bean>
        <!--创建持久化的队列-->
        <bean  id="queue" class="org.springframework.amqp.core.Queue">
            <constructor-arg index="0" value="testQueue"></constructor-arg>
            <constructor-arg index="1" value="true"></constructor-arg>
            <constructor-arg index="2" value="false"></constructor-arg>
            <constructor-arg index="3" value="true"></constructor-arg>
        </bean>
        <!--创建交换器的类型 并持久化-->
        <bean id="topicExchange" class="org.springframework.amqp.core.TopicExchange">
            <constructor-arg index="0" value="testExchange"></constructor-arg>
            <constructor-arg index="1" value="true"></constructor-arg>
            <constructor-arg index="2" value="false"></constructor-arg>
        </bean>
        <util:map id="arguments">
    
        </util:map>
        <!--绑定交换器 队列-->
        <bean id="binding" class="org.springframework.amqp.core.Binding">
            <constructor-arg index="0" value="testQueue"></constructor-arg>
            <constructor-arg index="1" value="QUEUE"></constructor-arg>
            <constructor-arg index="2" value="testExchange"></constructor-arg>
            <constructor-arg index="3" value="testQueue"></constructor-arg>
            <constructor-arg index="4" value="#{arguments}"></constructor-arg>
        </bean>
        <!--用于接收消息的处理类-->
        <bean id="rqmConsumer" class="com.zhuaer.learning.mq.rabbit.mvc.handler.RecvHandler"></bean>
    
        <bean id="messageListenerAdapter" class="org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter">
            <constructor-arg ref="rqmConsumer" />
            <property name="defaultListenerMethod" value="rmqProducerMessage"></property>
            <property name="messageConverter" ref="serializerMessageConverter"></property>
        </bean>
        <!-- 用于消息的监听的容器类SimpleMessageListenerContainer,监听队列  queues可以传多个-->
        <bean id="listenerContainer"  class="org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer">
            <property name="queues" ref="queue"></property>
            <property name="connectionFactory" ref="connectionFactoryMq"></property>
            <property name="messageListener" ref="messageListenerAdapter"></property>
        </bean>
        <bean id="rmqProducer" class="com.zhuaer.learning.mq.rabbit.mvc.handler.RmqProducer"></bean>
        <!--配置rabbitmq结束-->
    
    </beans>

### 方式二（rabbit标签配置方式）

新增rabbitmq.properties配置文件

    #IP地址
    rabbitmq.host=127.0.0.1
    #端口号
    rabbitmq.port=5672
    #用户名
    rabbitmq.username=root
    #密码
    rabbitmq.password=123456
    
    rabbitmq.virtualHost=/user_center

- 生产端（spring-rabbit-send.xml）


    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:rabbit="http://www.springframework.org/schema/rabbit"
           xmlns:context="http://www.springframework.org/schema/context"
           xsi:schemaLocation="http://www.springframework.org/schema/rabbit
     	http://www.springframework.org/schema/rabbit/spring-rabbit.xsd
         http://www.springframework.org/schema/context
         http://www.springframework.org/schema/context/spring-context-4.0.xsd
     	http://www.springframework.org/schema/beans
     	http://www.springframework.org/schema/beans/spring-beans-4.0.xsd">
    
        <context:property-placeholder location="classpath:rabbitmq.properties" ignore-unresolvable="true" />
        <!-- 配置连接工厂 -->
        <rabbit:connection-factory id="connectionFactory" publisher-returns="true" publisher-confirms="true"
                                   host="${rabbitmq.host}" port="${rabbitmq.port}" username="${rabbitmq.username}" password="${rabbitmq.password}" virtual-host="${rabbitmq.virtualHost}"/>
    
        <!-- 定义mq管理 -->
        <!--通过指定下面的admin信息，当前producer中的exchange和queue会在rabbitmq服务器上自动生成 -->
        <rabbit:admin connection-factory="connectionFactory" />
    
        <!-- 声明队列 -->
        <rabbit:queue name="queue" auto-declare="true" durable="true" exclusive="false"/>
    
        <!-- 延迟队列 -->
        <rabbit:queue name="delay_queue" auto-declare="true">
            <rabbit:queue-arguments>
                <entry key="x-message-ttl" value="5000" value-type="java.lang.Long" />
                <entry key="x-dead-letter-exchange" value="exchange_delay" />
                <entry key="x-dead-letter-routing-key" value="task_queue" />
            </rabbit:queue-arguments>
        </rabbit:queue>
    
        <!-- 定义交换机绑定队列（路由模式） -->
        <rabbit:direct-exchange name="IExchange" id="IExchange" durable="true" auto-delete="true">
            <rabbit:bindings>
                <rabbit:binding queue="queue" key="queuekey" />
            </rabbit:bindings>
        </rabbit:direct-exchange>
        <rabbit:fanout-exchange name="FExchange" id="FExchange" durable="true" auto-delete="true">
            <rabbit:bindings>
                <rabbit:binding queue="queue" />
            </rabbit:bindings>
        </rabbit:fanout-exchange>
    
        <!-- 消息对象json转换类 -->
        <bean id="jsonMessageConverter"
              class="org.springframework.amqp.support.converter.SimpleMessageConverter" />
    
        <!--定义rabbit template用于数据的接收和发送 -->
        <rabbit:template id="rabbitTemplate"
                         connection-factory="connectionFactory" exchange="IExchange"
                         message-converter="jsonMessageConverter" />
    
    </beans>

- 消费端（spring-rabbit-recv.xml）


    <beans xmlns="http://www.springframework.org/schema/beans"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:rabbit="http://www.springframework.org/schema/rabbit"
           xmlns:context="http://www.springframework.org/schema/context"
           xsi:schemaLocation="http://www.springframework.org/schema/rabbit
     	http://www.springframework.org/schema/rabbit/spring-rabbit.xsd
         http://www.springframework.org/schema/context
         http://www.springframework.org/schema/context/spring-context-4.0.xsd
     	http://www.springframework.org/schema/beans
     	http://www.springframework.org/schema/beans/spring-beans-4.0.xsd">
    
    
        <context:property-placeholder location="classpath:rabbitmq.properties" ignore-unresolvable="true" />
    
        <!-- 配置连接工厂 -->
        <rabbit:connection-factory id="connectionFactory"
                                   host="${rabbitmq.host}" port="${rabbitmq.port}" username="${rabbitmq.username}" password="${rabbitmq.password}" virtual-host="${rabbitmq.virtualHost}"/>
    
        <!-- 定义mq管理 -->
        <!--通过指定下面的admin信息，当前producer中的exchange和queue会在rabbitmq服务器上自动生成 -->
        <rabbit:admin connection-factory="connectionFactory" />
    
        <!-- 声明队列 -->
        <rabbit:queue name="queue" auto-declare="true" durable="true" exclusive="false"/>
    
        <!-- 延迟队列 -->
        <rabbit:queue name="delay_queue" auto-declare="true">
            <rabbit:queue-arguments>
                <entry key="x-message-ttl" value="5000" value-type="java.lang.Long" />
                <entry key="x-dead-letter-exchange" value="exchange_delay" />
                <entry key="x-dead-letter-routing-key" value="task_queue" />
            </rabbit:queue-arguments>
        </rabbit:queue>
    
        <!-- 定义消费者 -->
        <bean name="queuehandler" class="com.zhuaer.learning.mq.rabbit.mvc.handler.RecvHandler" />
        <bean name="queuehandler3" class="com.zhuaer.learning.mq.rabbit.mvc.handler.RecvHandler3"/>
    
        <!-- 定义消费者监听队列 -->
        <!-- 配置监听acknowledeg="manual"设置手动应答，它能够保证即使在一个worker处理消息的时候用CTRL+C来杀掉这个worker，或者一个consumer挂了(channel关闭了、connection关闭了或者TCP连接断了)，也不会丢失消息。因为RabbitMQ知道没发送ack确认消息导致这个消息没有被完全处理，将会对这条消息做re-queue处理。如果此时有另一个consumer连接，消息会被重新发送至另一个consumer会一直重发,直到消息处理成功,监听容器acknowledge="auto" concurrency="30"设置发送次数,最多发送30次 -->
        <rabbit:listener-container
                connection-factory="connectionFactory" acknowledge="manual">
    <!--        <rabbit:listener ref="queuehandler" queues="queue" />-->
            <rabbit:listener ref="queuehandler3" queues="queue" />
        </rabbit:listener-container>
    
    </beans>


消费者实现`MessageListener`或者`ChannelAwareMessageListener`接口，监听消息

