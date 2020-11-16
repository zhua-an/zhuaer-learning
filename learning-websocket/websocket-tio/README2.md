# websocket-tio

T-io网络通讯框架开源之后受到许多同学的喜欢，但是对于使用Spring系列技术的同学用起来稍许不适。于是乎抽时间写了个 starter,很荣幸代码被作者采纳，正式入驻T-io家族包。

## tio-websocket-server

tio-websocket-server 是基于tio的websocket协议的实现。并且，无缝对接点对点发送消息，群组消息，客户端连接维护等。所以，基于 tio-websocket-server 的 starter，在使用起来不必关心连接的维护问题，只要做好业务处理即可。在拥有了tio的便利性的同时又可以使用spring的各种特性，极大的简化了spring + websocket 的开发难度。

##快速开始

### pom.xml

    <dependency>
    	<groupId>org.t-io</groupId>
    	<artifactId>tio-websocket-spring-boot-starter</artifactId>
    	<!--此版本号跟着tio主版本号一致即可-->
    	<version>3.5.5.v20191010-RELEASE</version>
    </dependency>
   
### 添加注解

    @SpringBootApplication
    @EnableTioWebSocketServer
    public class SamplesApplication {
        public static void main(String[] args) {
            SpringApplication.run(SamplesApplication.class,args);
        }
    }
    
### application.xml

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
          
### 编写消息处理类

    public class MyWebSocketMsgHandler implements IWsMsgHandler {
     
        @Override
        public HttpResponse handshake(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
            return httpResponse;
        }
     
        @Override
        public void onAfterHandshaked(HttpRequest httpRequest, HttpResponse httpResponse, ChannelContext channelContext) throws Exception {
            System.out.println("握手成功");
        }
     
        @Override
        public Object onBytes(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
            System.out.println("接收到bytes消息");
            return null;
        }
     
        @Override
        public Object onClose(WsRequest wsRequest, byte[] bytes, ChannelContext channelContext) throws Exception {
            return null;
        }
     
        @Override
        public Object onText(WsRequest wsRequest, String s, ChannelContext channelContext) throws Exception {
            System.out.println("接收到文本消息："+s);
            return null;
        }
    }
    
### 编写简单客户端

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
    
### 运行程序，打开浏览器,Console 打印

opened
服务器收到了消息：Hello Tio WebSocket

## 原生回调接口支持

通过包扫描，反射创建新实例，赋给 TioWebSocketServerBootstrap中的属性。客户端编写相应的类即可，如下：

    //最主要的逻辑处理类，必须要写，否则 抛异常
    public class MyWebSocketMsgHandler  implements IWsMsgHandler {}
    //可不写
    public class SpringBootAioListener extends WsServerAioListener {}
    //可不写
    public class SpringBootGroupListener implements GroupListener {}
    //可不写
    public class SpringBootIpStatListener implements IpStatListener {}

## 服务端主动推送

主动推送消息，核心在于获取 ServerGroupContext。

    @RestController
    @RequestMapping("/push")
    public class PushController {
     
        @Autowired
        private TioWebSocketServerBootstrap bootstrap;
     
        @GetMapping("/msg")
        public void pushMessage(String msg){
            if (StrUtil.isEmpty(msg)){
                msg = "hello tio websocket spring boot starter";
            }
            Tio.sendToAll(bootstrap.getServerGroupContext(), WsResponse.fromText(msg,"utf-8"));
        }
    }
    
## SSL 支持

配置文件中增加如下配置

    # SSL 配置
       ssl:
         enabled: true
         key-store: key-store path
         password: password
         trust-store: trust-store path
         
## 集群支持

    # 集群配置 默认关闭
      cluster:
        enabled: false
        # 集群是通过redis的Pub/Sub实现，所以需要配置Redis
        redis:
          ip: 127.0.0.1
          port: 6379
        all: true
        group: true
        ip: true
        user: true
        
## 总结

tio-websocket-spring-boot-starter的宗旨和所有spring boot starter一致，通过配置实现快速开发。而又不会失去tio的代码灵活性。通过配置文件可以自由配置相应的功能，例如 SSL，集群等。注：本项目并未改变使用tio开发业务代码的方式。例如：Tio类的各种功能，同样适用。

## 注意

由于 bean 的加载顺序不同，可能在不同的功能类中注入tio-websocket-starter中的bean会有意想不到的问题。但是，基本很少，最常用的基本就是上文中的TioWebSocketServerBootstrap了，用它来获取ServerGroupContext，毕竟ServerGroupContext才是核心。     


官方文档地址
https://www.t-io.org/doc/tio/318

