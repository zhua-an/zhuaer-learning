# websocket-springboot

SpringBoot整合WebSocket实现前后端互推消息

## 1、什么是WebSocket

WebSocket 是 HTML5 开始提供的一种在单个 TCP 连接上进行全双工通讯的协议。WebSocket 使得客户端和服务器之间的数据交换变得更加简单，允许服务端主动向客户端推送数据。在 WebSocket API 中，浏览器和服务器只需要完成一次握手，两者之间就直接可以创建持久性的连接，并进行双向数据传输。

在 WebSocket API 中，浏览器和服务器只需要做一个握手的动作，然后，浏览器和服务器之间就形成了一条快速通道。两者之间就直接可以数据互相传送。HTML5 定义的 WebSocket 协议，能更好的节省服务器资源和带宽，并且能够更实时地进行通讯。

## 2、实现原理

![websocket_principle](../../file/websocket/websocket_principle.png "websocket_principle")

可以看到，浏览器通过 JavaScript 向服务器发出建立 WebSocket 连接的请求，连接建立以后，客户端和服务器端就可以通过 TCP 连接直接交换数据。第一次握手是基于HTTP协议实现的，当获取 Web Socket 连接后，就可以通过 `send()` 方法来向服务器发送数据，并通过 `onmessage` 事件来接收服务器返回的数据。

## 3、为什么需要 WebSocket

答案很简单，因为 HTTP 协议有一个缺陷：通信只能由客户端发起，HTTP 协议做不到服务器主动向客户端推送信息

## 添加依赖

    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    
## application.xml配置

    server:
      port: 8081
      servlet:
        context-path: /scoket
    
    spring:
      application:
        name: @artifactId@
      freemarker:
        request-context-attribute: request
    #    prefix: /templates/
        suffix: .html
        content-type: text/html
        enabled: true
        cache: false
        charset: UTF-8
        allow-request-override: false
        expose-request-attributes: true
        expose-session-attributes: true
        expose-spring-macro-helpers: true
    #    template-loader-path: classpath:/templates/

## 配置类

启用WebSocket的支持

    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.web.socket.server.standard.ServerEndpointExporter;
    
    /**
     * @ClassName WebsocketConfig
     * @Description 开启WebSocket支持
     * @Author zhua
     * @Date 2020/10/27 16:01
     * @Version 1.0
     */
    @Configuration
    public class WebsocketConfig {
    
        /**
         * ServerEndpointExporter 作用
         * 这个Bean会自动注册使用@ServerEndpoint注解声明的websocket endpoint
         * @return
         */
        @Bean
        public ServerEndpointExporter serverEndpointExporter() {
            return new ServerEndpointExporter();
        }
    
    }

## WebSocketServer

这就是重点了，核心都在这里。

1、因为WebSocket是类似客户端服务端的形式(采用ws协议)，那么这里的WebSocketServer其实就相当于一个ws协议的Controller

2、直接`@ServerEndpoint("/imserver/{userId}")` 、`@Component`启用即可，然后在里面实现`@OnOpen`开启连接，`@onClose`关闭连接，`@onMessage`接收消息等方法。

3、新建一个ConcurrentHashMap webSocketMap 用于接收当前userId的WebSocket，方便IM之间对userId进行推送消息。*单机版*实现到这里就可以。

    import com.alibaba.fastjson.JSON;
    import com.alibaba.fastjson.JSONObject;
    import org.apache.commons.lang3.StringUtils;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Component;
    
    import javax.websocket.*;
    import javax.websocket.server.PathParam;
    import javax.websocket.server.ServerEndpoint;
    import java.io.IOException;
    import java.util.concurrent.ConcurrentHashMap;
    
    /**
     * @ClassName WebSocketServer
     * @Description TODO
     * @Author zhua
     * @Date 2020/10/27 16:03
     * @Version 1.0
     */
    
    @ServerEndpoint("/imserver/{userId}")
    @Component
    public class WebSocketServer {
    
        private final static Logger log = LoggerFactory.getLogger(WebSocketServer.class);
        /**静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。*/
        private static int onlineCount = 0;
        /**concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。*/
        private static ConcurrentHashMap<String,WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
        /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
        private Session session;
        /**接收userId*/
        private String userId="";
    
        /**
         * 连接建立成功调用的方法*/
        @OnOpen
        public void onOpen(Session session, @PathParam("userId") String userId) {
            this.session = session;
            this.userId=userId;
            if(webSocketMap.containsKey(userId)){
                webSocketMap.remove(userId);
                webSocketMap.put(userId,this);
                //加入set中
            }else{
                webSocketMap.put(userId,this);
                //加入set中
                addOnlineCount();
                //在线数加1
            }
    
            log.info("用户连接:"+userId+",当前在线人数为:" + getOnlineCount());
    
            try {
                sendMessage("连接成功");
            } catch (IOException e) {
                log.error("用户:"+userId+",网络异常!!!!!!");
            }
        }
    
        /**
         * 连接关闭调用的方法
         */
        @OnClose
        public void onClose() {
            if(webSocketMap.containsKey(userId)){
                webSocketMap.remove(userId);
                //从set中删除
                subOnlineCount();
            }
            log.info("用户退出:"+userId+",当前在线人数为:" + getOnlineCount());
        }
    
        /**
         * 收到客户端消息后调用的方法
         *
         * @param message 客户端发送过来的消息*/
        @OnMessage
        public void onMessage(String message, Session session) {
            log.info("用户消息:"+userId+",报文:"+message);
            //可以群发消息
            //消息保存到数据库、redis
            if(StringUtils.isNotBlank(message)){
                try {
                    //解析发送的报文
                    JSONObject jsonObject = JSON.parseObject(message);
                    //追加发送人(防止串改)
                    jsonObject.put("fromUserId",this.userId);
                    String toUserId=jsonObject.getString("toUserId");
                    //传送给对应toUserId用户的websocket
                    if(StringUtils.isNotBlank(toUserId)&&webSocketMap.containsKey(toUserId)){
                        webSocketMap.get(toUserId).sendMessage(jsonObject.toJSONString());
                    }else{
                        log.error("请求的userId:"+toUserId+"不在该服务器上");
                        //否则不在这个服务器上，发送到mysql或者redis
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    
        /**
         *
         * @param session
         * @param error
         */
        @OnError
        public void onError(Session session, Throwable error) {
            log.error("用户错误:"+this.userId+",原因:"+error.getMessage());
            error.printStackTrace();
        }
        /**
         * 实现服务器主动推送
         */
        public void sendMessage(String message) throws IOException {
            this.session.getBasicRemote().sendText(message);
        }
    
    
        /**
         * 发送自定义消息
         * */
        public static void sendInfo(String message,@PathParam("userId") String userId) throws IOException {
            log.info("发送消息到:"+userId+"，报文:"+message);
            if(StringUtils.isNotBlank(userId)&&webSocketMap.containsKey(userId)){
                webSocketMap.get(userId).sendMessage(message);
            }else{
                log.error("用户"+userId+",不在线！");
            }
        }
    
        public static synchronized int getOnlineCount() {
            return onlineCount;
        }
    
        public static synchronized void addOnlineCount() {
            WebSocketServer.onlineCount++;
        }
    
        public static synchronized void subOnlineCount() {
            WebSocketServer.onlineCount--;
        }
    
    }

**说明**

这里有几个注解需要注意一下，首先是他们的包都在 **javax.websocket** 下。并不是 spring 提供的，而 jdk 自带的，下面是他们的具体作用。

- **@ServerEndpoint**
通过这个 spring boot 就可以知道你暴露出去的 ws 应用的路径，有点类似我们经常用的@RequestMapping。比如你的启动端口是8080，而这个注解的值是ws，那我们就可以通过 ws://127.0.0.1:8080/ws 来连接你的应用
- **@OnOpen**
当 websocket 建立连接成功后会触发这个注解修饰的方法，注意它有一个 Session 参数
- **@OnClose**
当 websocket 建立的连接断开后会触发这个注解修饰的方法，注意它有一个 Session 参数
- **@OnMessage**
当客户端发送消息到服务端时，会触发这个注解修改的方法，它有一个 String 入参表明客户端传入的值
- **@OnError**
当 websocket 建立连接时出现异常会触发这个注解修饰的方法，注意它有一个 Session 参数

另外一点就是服务端如何发送消息给客户端，服务端发送消息必须通过上面说的 Session 类，通常是在@OnOpen 方法中，当连接成功后把 session 存入 Map 的 value，key 是与 session 对应的用户标识，当要发送的时候通过 key 获得 session 再发送，这里可以通过 **session.getBasicRemote_().sendText(_)** 来对客户端发送消息。

## 消息推送

至于推送新信息，可以再自己的Controller写个方法调用WebSocketServer.sendInfo();即可

    @RestController
    public class WebSocketController {
    
        @GetMapping("page")
        public ModelAndView page(){
            return new ModelAndView("websocket");
        }
    
        @RequestMapping("/push/{toUserId}")
        public ResponseEntity<String> pushToWeb(String message, @PathVariable String toUserId) throws IOException {
            WebSocketServer.sendInfo(message,toUserId);
            return ResponseEntity.ok("MSG SEND SUCCESS");
        }
    
    }

## 页面发起

页面用js代码调用`websocket`，当然，太古老的浏览器是不行的，一般新的浏览器或者谷歌浏览器是没问题的。还有一点，记得协议是`ws`的，如果使用了一些路径类，可以replace(“http”,“ws”)来替换协议。

    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <title>websocket通讯</title>
    </head>
    <script src="https://cdn.bootcss.com/jquery/3.3.1/jquery.js"></script>
    <script>
        var socket;
        function openSocket() {
            if(typeof(WebSocket) == "undefined") {
                console.log("您的浏览器不支持WebSocket");
            }else{
                console.log("您的浏览器支持WebSocket");
                //实现化WebSocket对象，指定要连接的服务器地址与端口  建立连接
                //等同于socket = new WebSocket("ws://localhost:8888/xxxx/im/25");
                //var socketUrl="${request.contextPath}/im/"+$("#userId").val();
                var socketUrl="http://localhost:8081/scoket/imserver/"+$("#userId").val();
                socketUrl=socketUrl.replace("https","ws").replace("http","ws");
                console.log(socketUrl);
                if(socket!=null){
                    socket.close();
                    socket=null;
                }
                socket = new WebSocket(socketUrl);
                //打开事件
                socket.onopen = function() {
                    console.log("websocket已打开");
                    //socket.send("这是来自客户端的消息" + location.href + new Date());
                };
                //获得消息事件
                socket.onmessage = function(msg) {
                    console.log(msg.data);
                    //发现消息进入    开始处理前端触发逻辑
                };
                //关闭事件
                socket.onclose = function() {
                    console.log("websocket已关闭");
                };
                //发生了错误事件
                socket.onerror = function() {
                    console.log("websocket发生了错误");
                }
            }
        }
        function sendMessage() {
            if(typeof(WebSocket) == "undefined") {
                console.log("您的浏览器不支持WebSocket");
            }else {
                console.log("您的浏览器支持WebSocket");
                console.log('{"toUserId":"'+$("#toUserId").val()+'","contentText":"'+$("#contentText").val()+'"}');
                socket.send('{"toUserId":"'+$("#toUserId").val()+'","contentText":"'+$("#contentText").val()+'"}');
            }
        }
    </script>
    <body>
    <p>【userId】：<div><input id="userId" name="userId" type="text" value="10"></div>
    <p>【toUserId】：<div><input id="toUserId" name="toUserId" type="text" value="20"></div>
    <p>【toUserId】：<div><input id="contentText" name="contentText" type="text" value="hello websocket"></div>
    <p>【操作】：<div><button onclick="openSocket()">开启socket</button></div>
    <p>【操作】：<div><button onclick="sendMessage()">发送消息</button></div>
    </body>
    
    </html>

完成以上工作，就可以启动项目测试了。打开两个页面，按F12调出控控制台查看测试效果：分别开启socket，再发送消息


# Spring Cloud/Boot WebSocket 无法注入其他类的解决办法

## 不能注入原因

项目启动时初始化，会初始化 websocket （非用户连接的），spring 同时会为其注入 service，该对象的 service 不是 null，被成功注入。但是，由于 spring 默认管理的是单例，所以只会注入一次 service。当新用户进入聊天时，系统又会创建一个新的 websocket 对象，这时矛盾出现了：spring 管理的都是单例，不会给第二个 websocket 对象注入 service，所以导致只要是用户连接创建的 websocket 对象，都不能再注入了。

## 解决办法

像 controller 里面有 service， service 里面有 dao。因为 controller，service ，dao 都有是单例，所以注入时不会报 null。但是 websocket 不是单例，所以使用spring注入一次后，后面的对象就不会再注入了，会报null。

## 例如


    import com.alibaba.fastjson.JSON;
    import net.sf.json.JSONObject;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.redis.core.StringRedisTemplate;
    import org.springframework.stereotype.Component;
    import org.springframework.stereotype.Service;
    import org.springframework.web.socket.*;
    import org.springframework.web.socket.handler.TextWebSocketHandler;

    import java.io.IOException;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.Set;

    /**
     * @Author:
     * @Date: 2019/9/20 14:44
     */
    @Service
    @Component
    public class MyHandler extends TextWebSocketHandler /*implements WebSocketHandler*/ {


        //@Autowired
        private static StringRedisTemplate stringRedisTemplate;

        /**
         * 项目启动时初始化，会初始化 websocket （非用户连接的），
         * spring 同时会为其注入 service，该对象的 service 不是 null，被成功注入。
         * 但是，由于 spring 默认管理的是单例，所以只会注入一次 service。
         * 当新用户进入聊天时，系统又会创建一个新的 websocket 对象，
         * 这时矛盾出现了：spring 管理的都是单例，不会给第二个 websocket 对象注入 service，
         * 所以导致只要是用户连接创建的 websocket 对象，都不能再注入了。
         * <p>
         * <p>
         * controller 里面有 service， service 里面有 dao。
         * 因为 controller，service ，dao 都有是单例，
         * 所以注入时不会报 null。但是 websocket 不是单例，
         * 所以使用spring注入一次后，后面的对象就不会再注入了，会报null。
         *
         * @param stringRedisTemplate
         */
        @Autowired
        public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
            MyHandler.stringRedisTemplate = stringRedisTemplate;
        }

        //在线用户列表
        private static final Map<String, WebSocketSession> users;


        static {
            users = new HashMap<>();
        }

        //新增socket
        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            System.out.println("成功建立连接");
            String ID = session.getUri().toString().split("ID=")[1];
            System.out.println(ID);
            if (ID != null) {
                //users.put(ID, session);
                Map<String, WebSocketSession> hashMap = new HashMap<>();
                hashMap.put(ID, session);
                stringRedisTemplate.convertAndSend("index", hashMap.toString());
                session.sendMessage(new TextMessage("成功建立socket连接"));
            }
            System.out.println("当前在线人数：" + users.size());
        }

        public void receiveMessage(String message) {
            System.out.println(message);
            Map<String, WebSocketSession> hashMap = JSON.parseObject(message, HashMap.class);
            users.putAll(hashMap);
            System.out.println("添加登陆信息：" + message);
            //这里是收到通道的消息之后执行的方法
        }


        //接收socket信息
        @Override
        public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
            try {
                JSONObject jsonobject = JSONObject.fromObject(webSocketMessage.getPayload());
                System.out.println(jsonobject.get("id"));
                System.out.println(jsonobject.get("message") + ":来自" + (String) webSocketSession.getAttributes().get("WEBSOCKET_USERID") + "的消息");
                sendMessageToUser(jsonobject.get("id") + "", new TextMessage("服务器收到了，hello!"));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * 发送信息给指定用户
         *
         * @param clientId
         * @param message
         * @return
         */
        public boolean sendMessageToUser(String clientId, TextMessage message) {
            if (users.get(clientId) == null) return false;
            WebSocketSession session = users.get(clientId);
            System.out.println("sendMessage:" + session);
            if (!session.isOpen()) return false;
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        /**
         * 广播信息
         *
         * @param message
         * @return
         */
        public boolean sendMessageToAllUsers(TextMessage message) {
            boolean allSendSuccess = true;
            Set<String> clientIds = users.keySet();
            WebSocketSession session = null;
            for (String clientId : clientIds) {
                try {
                    session = users.get(clientId);
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    allSendSuccess = false;
                }
            }

            return allSendSuccess;
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            if (session.isOpen()) {
                session.close();
            }
            System.out.println("连接出错");
            users.remove(getClientId(session));
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            System.out.println("连接已关闭：" + status);
            users.remove(getClientId(session));
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }

        /**
         * 获取用户标识
         *
         * @param session
         * @return
         */
        private Integer getClientId(WebSocketSession session) {
            try {
                Integer clientId = (Integer) session.getAttributes().get("WEBSOCKET_USERID");
                return clientId;
            } catch (Exception e) {
                return null;
            }
        }
    }

## 第二种解释

首先WebSocket的例子：

    @ServerEndpoint(value = "/websocket" )
    @Component
    public class MyWebSocket
    {
        // 与某个客户端的连接会话，需要通过它来给客户端发送数据
        private Session session;

        @Autowired
        TestInfo testInfo;

        /**
         * 连接建立成功调用的方法
         */
        @OnOpen
        public void onOpen(Session session)
        {
            System.out.println(this.hashCode());
            this.session = session;
            try
            {
                System.out.println(testInfo.name);
                sendMessage("新用户添加进来了....");
            }
            catch (IOException e)
            {
                System.out.println("IO异常");
            }
        }

        /**
         * 连接关闭调用的方法
         */
        @OnClose
        public void onClose()
        {
            System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
        }

        /**
         * 收到客户端消息后调用的方法
         *
         * @param message
         *            客户端发送过来的消息
         */
        @OnMessage
        public void onMessage(String message, Session session)
        {
            System.out.println("来自客户端的消息:" + message);
        }

        /**
         * 发生错误时调用
         */
        @OnError
        public void onError(Session session, Throwable error)
        {
            System.out.println("发生错误");
            error.printStackTrace();
        }

        public void sendMessage(String message) throws IOException
        {
            this.session.getBasicRemote().sendText(message);
        }
    }

当客户端发送请求的时候，会报空指针异常，TestInfo 为空。
创建MyWebSocket，也是通过@Bean的形式实现的。其他的地方都没有问题。

我已经autowired了，干嘛没注入啊。

TestInfo是通过Spring容器进行管理的，但是使用ServerEndpoint这个注解的时候，失效了。
猜测原因就是这个MyWebSocket这个并不是Spring容器管理的。但是这个是官方推荐的实现方法 啊。

寻寻觅觅，最后在强大的stackoverflow中找到了解决问题的办法。
https://stackoverflow.com/questions/30483094/springboot-serverendpoint-failed-to-find-the-root-webapplicationcontext

**第一种方法：**

继续用ServerEndpoint。
定义一个MyEndpointConfigure

    /**
     * 
     * @author lipengbin
     *
     */
    public class MyEndpointConfigure extends ServerEndpointConfig.Configurator implements ApplicationContextAware
    {
        private static volatile BeanFactory context;
    
        @Override
        public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException
        {
             return context.getBean(clazz);
        }
    
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
        {
            System.out.println("auto load"+this.hashCode());
            MyEndpointConfigure.context = applicationContext;
        }
    }

这个类的核心就是**getEndpointInstance(Class clazz)**这个方法。

定义了获取类实例是通过ApplicationContext获取。

    @Configuration
    public class MyConfigure
    {
    
        @Bean
        public MyEndpointConfigure newConfigure()
        {
            return new MyEndpointConfigure();
        }
    }
    
**修改MyWebSocket的注解**

@ServerEndpoint(value = “/websocket” ) 为 @ServerEndpoint(value = “/websocket”,configurator=MyEndpointConfigure.class)
大致的意思可以理解了，创建类需要通过 **MyEndpointConfigure.getEndpointInstance()** 这个来实现。

运行一切正常。

但是这种形式并不是正常的Spring容器去正常去管理这个WebSocket，个人觉得并不是很好。

这个帖子同时还给出了第二解决方法。原生的Spring实现的WebSocket的办法。

**第二种解决办法：**

与其说是第二种办法，不如说是Spring第二种实现WebSocket的方案。和第一种没有任何的联系。

代码如下：

**核心Handler，有Spring的风格**

    @Component
    public class WsHandler extends TextWebSocketHandler
    {
    
        @Autowired  
        TestInfo testInfo;
    
        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception
        {
            super.afterConnectionClosed(session, status);
            System.out.println("close....");
        }
    
        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception
        {
            super.afterConnectionEstablished(session);
            System.out.println("----->"+testInfo.test());
            System.out.println("建立新的会话");
        }
    
        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception
        {       
            System.out.println(message.getPayload());
            TextMessage msg=new TextMessage(message.getPayload());
            session.sendMessage(msg);
    
        }
    
        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception
        {
            super.handleMessage(session, message);
        }
    
        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception
        {
            super.handleTransportError(session, exception);
        }
    
    }

简单实现几个关键的方法。

TestInfo 直接注入。

编写Configure类

    @Configuration
    @EnableWebSocket
    public class WsConfigure implements WebSocketConfigurer
    {
        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
        {
            System.out.println("==========================");
            registry.addHandler(myHandler(), "/websocket").setAllowedOrigins("*");
        }
    
        @Bean
        public WsHandler myHandler()
        {
            return new WsHandler();
        }
    }

这种实现方法可以查看官方文档。
https://docs.spring.io/spring/docs/4.3.13.RELEASE/spring-framework-reference/htmlsingle/#websocket

经测试可以正常工作。

## 第三种方法

**将要注入的 service 改成 static，就不会为null了**

    @Controller
    @ServerEndpoint(value="/chatSocket")
    public class ChatSocket {
        //  这里使用静态，让 service 属于类
        private static ChatService chatService;
    
        // 注入的时候，给类的 service 注入
        @Autowired
        public void setChatService(ChatService chatService) {
            ChatSocket.chatService = chatService;
        }
    }
    
