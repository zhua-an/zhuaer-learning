# webscoket-springboot3

Spring封装

## 引入依赖

    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    <!--hutool-->
    <dependency>
        <groupId>cn.hutool</groupId>
        <artifactId>hutool-all</artifactId>
        <version>5.4.6</version>
    </dependency>

## 实现Websocket建立连接、发送消息、断开连接等时候的处理类

    @Component
    public class HttpAuthHandler extends TextWebSocketHandler {
    
        /**
         * socket 建立成功事件
         *
         * @param session
         * @throws Exception
         */
        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            Object token = session.getAttributes().get("token");
            if (token != null) {
                // 用户连接成功，放入在线用户缓存
                WsSessionManager.add(token.toString(), session);
            } else {
                throw new RuntimeException("用户登录已经失效!");
            }
        }
    
        /**
         * 接收消息事件
         *
         * @param session
         * @param message
         * @throws Exception
         */
        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            // 获得客户端传来的消息
            String payload = message.getPayload();
            Object token = session.getAttributes().get("token");
            System.out.println("server 接收到 " + token + " 发送的 " + payload);
            session.sendMessage(new TextMessage("server 发送给 " + token + " 消息 " + payload + " " + LocalDateTime.now().toString()));
        }
    
        /**
         * socket 断开连接时
         *
         * @param session
         * @param status
         * @throws Exception
         */
        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            Object token = session.getAttributes().get("token");
            if (token != null) {
                // 用户退出，移除缓存
                WsSessionManager.remove(token.toString());
            }
        }
        
        /**
         * 连接出错
         * @param session
         * @param exception
         * @throws Exception
         */
        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            if (session.isOpen()) {
                session.close();
            }
            System.out.println("连接出错");
            Object token = session.getAttributes().get("token");
            if (token != null) {
                // 用户退出，移除缓存
                WsSessionManager.remove(token.toString());
            }
        }
    }

**说明：**

通过继承 TextWebSocketHandler 类并覆盖相应方法，可以对 websocket 的事件进行处理，这里可以同原生注解的那几个注解连起来看

- **afterConnectionEstablished** 方法是在 socket 连接成功后被触发，同原生注解里的 @OnOpen 功能
- **afterConnectionClosed**方法是在 socket 连接关闭后被触发，同原生注解里的 @OnClose 功能
- **handleTextMessage**方法是在客户端发送信息时触发，同原生注解里的 @OnMessage 功能

## Session管理类WsSessionManager

这里简单通过 **ConcurrentHashMap** 来实现了一个 session 池，用来保存已经登录的 web socket 的  session。前文提过，服务端发送消息给客户端必须要通过这个 session。

    import org.springframework.web.socket.WebSocketSession;
    
    import java.io.IOException;
    import java.util.concurrent.ConcurrentHashMap;
    
    /**
     * @ClassName WsSessionManager
     * @Description TODO
     * @Author zhua
     * @Date 2020/11/12 14:30
     * @Version 1.0
     */
    public class WsSessionManager {
    
        /**
         * 保存连接 session 的地方
         */
        private static ConcurrentHashMap<String, WebSocketSession> SESSION_POOL = new ConcurrentHashMap<>();
    
        /**
         * 添加 session
         *
         * @param key
         */
        public static void add(String key, WebSocketSession session) {
            // 添加 session
            SESSION_POOL.put(key, session);
        }
    
        /**
         * 删除 session,会返回删除的 session
         *
         * @param key
         * @return
         */
        public static WebSocketSession remove(String key) {
            // 删除 session
            return SESSION_POOL.remove(key);
        }
    
        /**
         * 删除并同步关闭连接
         *
         * @param key
         */
        public static void removeAndClose(String key) {
            WebSocketSession session = remove(key);
            if (session != null) {
                try {
                    // 关闭连接
                    session.close();
                } catch (IOException e) {
                    // todo: 关闭出现异常处理
                    e.printStackTrace();
                }
            }
        }
    
        /**
         * 获得 session
         *
         * @param key
         * @return
         */
        public static WebSocketSession get(String key) {
            // 获得 session
            return SESSION_POOL.get(key);
        }
    }
    
## 拦截器 MyInterceptor

通过实现 HandshakeInterceptor 接口来定义握手拦截器，注意这里与上面 Handler 的事件是不同的，这里是建立握手时的事件，分为握手前与握手后，而  Handler 的事件是在握手成功后的基础上建立 socket 的连接。所以在如果把认证放在这个步骤相对来说最节省服务器资源。它主要有两个方法 **beforeHandshake** 与 **afterHandshake**，顾名思义一个在握手前触发，一个在握手后触发。

    import cn.hutool.core.util.CharsetUtil;
    import cn.hutool.core.util.StrUtil;
    import cn.hutool.http.HttpUtil;
    import org.springframework.http.server.ServerHttpRequest;
    import org.springframework.http.server.ServerHttpResponse;
    import org.springframework.web.socket.WebSocketHandler;
    import org.springframework.web.socket.server.HandshakeInterceptor;
    
    import java.util.Map;
    
    /**
     * @ClassName MyInterceptor
     * @Description TODO
     * @Author zhua
     * @Date 2020/11/12 14:33
     * @Version 1.0
     */
    public class MyInterceptor implements HandshakeInterceptor{
    
        /**
         * 握手前
         *
         * @param request
         * @param response
         * @param wsHandler
         * @param attributes
         * @return
         * @throws Exception
         */
        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            System.out.println("握手开始");
            // 获得请求参数
            Map<String, String> paramMap = HttpUtil.decodeParamMap(request.getURI().getQuery(), CharsetUtil.charset("utf-8"));
            String uid = paramMap.get("token");
            if (StrUtil.isNotBlank(uid)) {
                // 放入属性域
                attributes.put("token", uid);
                System.out.println("用户 token " + uid + " 握手成功！");
                return true;
            }
            System.out.println("用户登录已失效");
            return false;
        }
    
        /**
         * 握手后
         *
         * @param request
         * @param response
         * @param wsHandler
         * @param exception
         */
        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
            System.out.println("握手完成");
        }
    }
    
## WebSocketConfig

通过实现 **WebSocketConfigurer** 类并覆盖相应的方法进行 websocket 的配置。我们主要覆盖 **registerWebSocketHandlers** 这个方法。通过向 **WebSocketHandlerRegistry** 设置不同参数来进行配置。其中 **addHandler** 方法添加我们上面的写的 ws 的  handler 处理类，第二个参数是你暴露出的 ws 路径。**addInterceptors** 添加我们写的握手过滤器。 **setAllowedOrigins("\*")** 这个是关闭跨域校验，方便本地调试，线上推荐打开。

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
    import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
    
    /**
     * @ClassName WebSocketConfig
     * @Description TODO
     * @Author zhua
     * @Date 2020/11/12 14:42
     * @Version 1.0
     */
    public class WebSocketConfig implements WebSocketConfigurer {
    
        @Autowired
        private HttpAuthHandler httpAuthHandler;
        @Autowired
        private MyInterceptor myInterceptor;
    
        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry
                    .addHandler(httpAuthHandler, "myWS")
                    .addInterceptors(myInterceptor)
                    .setAllowedOrigins("*");
        }
    }

    