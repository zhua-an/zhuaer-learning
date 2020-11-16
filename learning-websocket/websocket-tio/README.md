# websocket-tio

## pom.xml

    <dependency>
    	<groupId>org.t-io</groupId>
    	<artifactId>tio-websocket-spring-boot-starter</artifactId>
    	<version>3.5.5.v20191010-RELEASE</version>
    </dependency>
    
## application.xml

这里只配置了 ws 的启动端口，还有很多配置

    tio:
      websocket:
        server:
          port: 9876
          heartbeat-timeout: 60000
          #是否支持集群，集群开启需要redis
        cluster:
          enabled: false
        redis:
          ip: 192.168.1.225
          port: 6379
          
## 编写消息处理类

    import org.springframework.stereotype.Component;
    import org.tio.core.ChannelContext;
    import org.tio.http.common.HttpRequest;
    import org.tio.http.common.HttpResponse;
    import org.tio.websocket.common.WsRequest;
    import org.tio.websocket.server.handler.IWsMsgHandler;
    
    /**
     * @ClassName MyHandler
     * @Description TODO
     * @Author zhua
     * @Date 2020/11/12 15:06
     * @Version 1.0
     */
    @Component
    public class MyHandler implements IWsMsgHandler {
        /**
         * 握手
         *
         * @param httpRequest
         * @param httpResponse
         * @param channelContext
         * @return
         * @throws Exception
         */
        @Override
        public HttpResponse handshake(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
            return httpResponse;
        }
    
        /**
         * 握手成功
         *
         * @param httpRequest
         * @param httpResponse
         * @param channelContext
         * @throws Exception
         */
        @Override
        public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
            System.out.println("握手成功");
        }
    
        /**
         * 接收二进制文件
         *
         * @param wsRequest
         * @param bytes
         * @param channelContext
         * @return
         * @throws Exception
         */
        @Override
        public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
            return null;
        }
    
        /**
         * 断开连接
         *
         * @param wsRequest
         * @param bytes
         * @param channelContext
         * @return
         * @throws Exception
         */
        @Override
        public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
            System.out.println("关闭连接");
            return null;
        }
    
        /**
         * 接收消息
         *
         * @param wsRequest
         * @param s
         * @param channelContext
         * @return
         * @throws Exception
         */
        @Override
        public Object onText(WsRequest wsRequest, String s, ChannelContext channelContext) throws Exception {
            System.out.println("接收文本消息:" + s);
            return "success";
        }
    }
    
通过实现接口覆盖方法来进行事件处理，实现的接口是IWsMsgHandler，它的方法功能如下

- handshake
在握手的时候触发
- onAfterHandshaked
在握手成功后触发
- onBytes
客户端发送二进制消息触发
- onClose
客户端关闭连接时触发
- onText
客户端发送文本消息触发    
    
## 启动类 Application 开启 EnableTioWebSocketServer

    @SpringBootApplication
    @EnableTioWebSocketServer
    public class Application {
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }
    
## 编写简单客户端

    <script>
        var ws =new WebSocket("ws://localhost:9876");
        ws.onopen = function (event) {
            console.log("opened");
            ws.send("Hello Tio WebSocket");
        }
        ws.onmessage=function (p1) {
            console.log(p1.data);
        }
    </script>
    
## 测试

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.ResponseBody;
    import org.tio.core.Tio;
    import org.tio.websocket.common.WsResponse;
    import org.tio.websocket.starter.TioWebSocketServerBootstrap;
    
    /**
     * @ClassName TestController
     * @Description TODO
     * @Author zhua
     * @Date 2020/11/12 15:21
     * @Version 1.0
     */
    @Controller
    public class TestController {
    
        @Autowired
        private TioWebSocketServerBootstrap bootstrap;
    
        @GetMapping("/send")
        public @ResponseBody Object sendMsg(String msg) {
            Tio.sendToAll(bootstrap.getServerTioConfig(), WsResponse.fromText(msg, "utf-8"));
            return "success";
        }
    }

    


