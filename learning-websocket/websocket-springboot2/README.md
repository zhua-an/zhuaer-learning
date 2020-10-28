# websocket-springboot

SpringBoot整合WebSocket实现前后端互推消息

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

## 添加配置类：WebSocketConfig

一般在连接服务器时，需要验证此连接的安全性，验证用户是否登录，如果没有登录，不能连接服务器，订阅消息。

    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.server.ServerHttpRequest;
    import org.springframework.messaging.simp.config.MessageBrokerRegistry;
    import org.springframework.web.socket.WebSocketHandler;
    import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
    import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
    import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
    import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
    
    import java.security.Principal;
    import java.util.Map;
    
    /**
     * @ClassName WebsocketConfig
     * @Description WebsocketConfig 类进行了websocket的配置
     * @Author zhua
     * @Date 2020/10/27 16:01
     * @Version 1.0
     */
    @Configuration
    @EnableWebSocketMessageBroker
    public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    
        /**
         * 配置了一个简单的消息代理，如果不重载，默认情况下回自动配置一个简单的内存消息代理，用来处理以"/topic"为前缀的消息。这里重载configureMessageBroker()方法，
         * 消息代理将会处理前缀为"/userTest"和"/topicTest"的消息。
         * @param config
         */
        @Override
        public void configureMessageBroker(MessageBrokerRegistry config) {
            //启用/userTest，/topicTest,两个消息前缀
            config.enableSimpleBroker("/userTest","/topicTest");
            //如果不设置下面这一句，用convertAndSendToUser来发送消息，前端订阅只能用/user开头。
            config.setUserDestinationPrefix("/userTest");
            //客户端（html等）向服务端发送消息的前缀
            config.setApplicationDestinationPrefixes("/app");
            //可以已“.”来分割路径，看看类级别的@messageMapping和方法级别的@messageMapping  例如：/app/zhua.sendToUser
    //        config.setPathMatcher(new AntPathMatcher("."));
        }
    
        /**
         * 将"/websocket-endpoint"路径注册为STOMP端点，这个路径与发送和接收消息的目的路径有所不同，这是一个端点，客户端在订阅或发布消息到目的地址前，要连接该端点，
         * 即用户发送请求url="/applicationName/websocket-endpoint"与STOMP server进行连接。之后再转发到订阅url；
         * PS：端点的作用——客户端在订阅或发布消息到目的地址前，要连接该端点。
         * @param stompEndpointRegistry
         */
        @Override
        public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
            //客户端和服务端进行连接的endpoint
            stompEndpointRegistry.addEndpoint("/websocket-endpoint").setHandshakeHandler(new  DefaultHandshakeHandler(){
                @Override
                protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                    //key就是服务器和客户端保持一致的标记，一般可以用账户名称，或者是用户ID。
                    return new MyPrincipal("zhua");
                }
            }).setAllowedOrigins("*").withSockJS();
        }
    
    
        /**
         * 自定义的Principal
         */
        class MyPrincipal implements Principal{
    
            private String key;
    
            public MyPrincipal(String key) {
                this.key = key;
            }
    
            @Override
            public String getName() {
                return key;
            }
    
        }
    }
    
    
其中的两个路径：

1.addEndpoint添加的第一个路径，是监听WebSocket连接的Stomp代理的端点，页面请求WebSocket连接时，连接到注册的该端点上Stomp代理，之后的消息会交给Stomp代理处理。

2.该配置启用了两个个简单的消息代理，用来处理前缀为/userTest跟/topicTest的消息，也就是说，只有路径为/userTest跟/topicTest请求时，消息才会由消息代理处理

3.registry.setUserDestinationPrefix("/userTest");这句话表示给指定用户发送一对一的主题前缀是"/userTest"

4.registry.setApplicationDestinationPrefixes("/app");这句话表示客户单向服务器端发送时的主题上面需要加"/app"作为前缀。

5.stompEndpointRegistry.addEndpoint("/websocket-endpoint").setAllowedOrigins("*").withSokJS();这个和客户端创建连接时的url有关，其中setAllowedOrigins()方法表示允许连接的域名，withSockJS()方法表示支持以SockJS方式连接服务器。

## 消息管理实现类

`@MessageMapping`注解，与`@RequestMapping`注解类似，配置后台接收消息的路径以及处理函数

`@SendTo`注解，一般与`@MessageMapping`注解一起使用，该注解配置的控制器，返回的数据将发送到监听该配置路径的监听函数

`@SendToUser`，这就是发送给单一客户端的标志。本例中，客户端接收一对一消息的主题应该是“/userTest/message” ,”/userTest/”是固定的搭配，服务器会自动识别。

`@SendToUser("/message")` 等同于使用 `SimpMessagingTemplate.convertAndSendToUser(Key,"/message", "新消息");`

    /**
     * @ClassName WebSocketController
     * @Description TODO
     * @Author zhua
     * @Date 2020/10/27 16:08
     * @Version 1.0
     */
    @EnableScheduling
    @Controller
    public class WebSocketController {
    
        @Autowired
        private SimpMessagingTemplate messagingTemplate;
    
        @GetMapping("/")
        public String index() {
            return "index";
        }
    
        /**
         * index.html将message发送给后端，后端再将消息重组后发送到/topicTest/web-to-server-to-web
         * @param message
         * @return
         * @throws Exception
         */
        @MessageMapping("/send")
        @SendTo("/topicTest/web-to-server-to-web")
        public String send(String message) throws Exception {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "服务器将原消息返回: "+df.format(new Date())+" :" + message;
        }
        
        /**
         * 这里用的是@SendToUser，这就是发送给单一客户端的标志。本例中，
         * 客户端接收一对一消息的主题应该是“/userTest/” + 用户Id + “/message” ,这里的用户id可以是一个普通的字符串，只要每个用户端都使用自己的id并且服务端知道每个用户的id就行。
         * @SendToUser 此外该注解还有个broadcast属性，表明是否广播。就是当有同一个用户登录多个session时，是否都能收到。取值true/false.
         * @return
         */
        @MessageMapping("/sendToUser")
        @SendToUser("/message")
        public String handleSubscribe(String message) {
            System.out.println(message);
            System.out.println("I am a msg from SubscribeMapping('/sendToUser').");
            return "I am a msg from SubscribeMapping('/sendToUser').";
        }
    
        /**
         * 最基本的服务器端主动推送消息给前端
         * @return
         * @throws Exception
         */
        @Scheduled(fixedRate = 1000)
        public String serverTime() throws Exception {
            // 发现消息
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            messagingTemplate.convertAndSend("/topicTest/servertime", df.format(new Date()));
            return "servertime";
        }
    
        /**
         * 以下面这种方式发送消息，前端订阅消息的方式为： stompClient.subscribe('/userTest/hzb/info'
         * @return
         * @throws Exception
         */
        @Scheduled(fixedRate = 1000)
        public String serverTimeToUser() throws Exception {
            // 发现消息
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //这里虽然没有指定发送前缀为/userTest，但是在WebsocketConfig.java中设置了config.setUserDestinationPrefix("/userTest")，
            //否则默认为/user
            messagingTemplate.convertAndSendToUser("zhua","/info", df.format(new Date()));
            return "serverTimeToUser";
        }
    }
    
## 前端代码：index.html

默认情况，在消息发送给客户端之前，服务端会自动确认（acknowledged）。

客户端可以选择通过订阅一个目的地时设置一个ack header为client或client-individual来处理消息确认。

在下面这个例子，客户端必须调用message.ack()来通知客户端它已经接收了消息。

    <!DOCTYPE html>
    <html>
    <head>
        <title>玩转spring boot——websocket</title>
        <script src="//cdn.bootcss.com/angular.js/1.5.6/angular.min.js"></script>
        <script src="https://cdn.bootcss.com/sockjs-client/1.1.4/sockjs.min.js"></script>
        <script src="https://cdn.bootcss.com/stomp.js/2.3.3/stomp.min.js"></script>
        <script type="text/javascript">
            var stompClient = null;
    
            var app = angular.module('app', []);
            app.controller('MainController', function($rootScope, $scope, $http) {
    
                $scope.data = {
                    connected : false,
                    sendMessage : '',
                    receivMessages : []
                };
    
                //连接
                $scope.connect = function() {
                    var socket = new SockJS('/websocket-endpoint');
                    stompClient = Stomp.over(socket);
                    stompClient.connect({}, function(frame) {
                        // 订阅后端主动推消息到前端的topic
                        //消息确认ack
                        stompClient.subscribe('/topicTest/servertime', function(r) {
                            $scope.data.time = '当前服务器时间：' + r.body;
                            $scope.data.connected = true;
                            $scope.$apply();
                            r.ack();
                        },{ack: 'client'});
                        // 订阅后端为目标用户发送消息的topic
                        stompClient.subscribe('/userTest/zhua/message', function(r) {
                            $scope.data.receivMessages.push(r.body);
                            $scope.data.connected = true;
                            $scope.$apply();
                        });
                        // 阅后端主动推消息到前端的topic,只有指定的用户(zhua)收到的的消息
                        stompClient.subscribe('/userTest/zhua/info', function(r) {
                            $scope.data.zhuatime = '当前服务器时间：' + r.body;
                            $scope.data.connected = true;
                            $scope.$apply();
                        });
                        // 订阅前端发到后台，后台又将消息返回前台的topic
                        stompClient.subscribe('/topicTest/web-to-server-to-web', function(msg) {
                            $scope.data.receivMessages.push(msg.body);
                            $scope.data.connected = true;
                            $scope.$apply();
                        });
    
    
                        $scope.data.connected = true;
                        $scope.$apply();
                    });
                };
    
                $scope.disconnect = function() {
                    if (stompClient != null) {
                        stompClient.disconnect();
                    }
                    $scope.data.connected = false;
                }
    
                $scope.send = function() {
                    /*client.send(destination url[, headers[, body]]);
                    其中
                    destination url 为服务器 controller中 @MessageMapping 中匹配的URL，字符串，必须参数；
                    headers 为发送信息的header，JavaScript 对象，可选参数；
                    body 为发送信息的 body，字符串，可选参数；*/
                    stompClient.send("/app/send", {}, JSON.stringify({
                        'message' : $scope.data.sendMessage
                    }));
                }
    
                $scope.sendToUser = function() {
                    stompClient.send("/app/sendToUser", {}, JSON.stringify({
                        'message' : $scope.data.sendMessage
                    }));
                }
                            
            });
        </script>
    </head>
    <body ng-app="app" ng-controller="MainController">
    
    <h2>websocket示例</h2>
    <label>WebSocket连接状态:</label>
    <button type="button" ng-disabled="data.connected" ng-click="connect()">连接</button>
    <button type="button" ng-click="disconnect()" ng-disabled="!data.connected">断开</button>
    <br/>
    <br/>
    <div ng-show="data.connected">
        <h4>以下是websocket的服务端主动推送消息到页面的例子</h4>
        <label>{{data.time}}</label> <br/> <br/>
    </div>
    <div ng-show="data.connected">
        <h4>以下是websocket的服务端主动推送消息到页面的例子,只有zhua这个用户收到</h4>
        <label>{{data.zhuatime}}</label> <br/> <br/>
    </div>
    <div ng-show="data.connected">
        <h4>以下是websocket的客户端发消息到服务端，服务端再将该消息返回到客户端（页面）的例子</h4>
        <input type="text" ng-model="data.sendMessage" placeholder="请输入内容..." />
        <button ng-click="send()" type="button">发送</button>
        <button ng-click="sendToUser()" type="button">发送给用户</button>
        <br/>
        <table>
            <thead>
            <tr>
                <th>消息内容:</th>
            </tr>
            </thead>
            <tbody>
            <tr ng-repeat="messageContent in data.receivMessages">
                <td>{{messageContent}}</td>
            </tr>
            </tbody>
        </table>
    </div>
    </body>
    </html>

ack()接受headers参数用来附加确认消息。例如，将消息作为事务(transaction)的一部分，当要求接收消息时其实代理（broker）已经将ACK STOMP frame处理了。

    var tx = client.begin();
    message.ack({ transaction: tx.id, receipt: 'my-receipt' });
    tx.commit();

## 事务支持

可以在将消息的发送和确认接收放在一个事务中。

客户端调用自身的begin()方法就可以开始启动事务了，begin()有一个可选的参数transaction，一个唯一的可标识事务的字符串。如果没有传递这个参数，那么库会自动构建一个。

这个方法会返回一个object。这个对象有一个id属性对应这个事务的ID，还有两个方法：

    commit()提交事务
    abort()中止事务

在一个事务中，客户端可以在发送/接受消息时指定transaction id来设置transaction。

    // start the transaction
    var tx = client.begin();
    
    // send the message in a transaction
    client.send("/queue/test", {transaction: tx.id}, "message in a transaction");
    
    // commit the transaction to effectively send the message
    tx.commit();
    
如果你在调用send()方法发送消息的时候忘记添加transction header，那么这不会称为事务的一部分，这个消息会直接发送，不会等到事务完成后才发送。

    var txid = "unique_transaction_identifier";
     
    // start the transaction
     
    var tx = client.begin();
     
    // oops! send the message outside the transaction
     
    client.send("/queue/test", {}, "I thought I was in a transaction!");
     
    tx.abort(); // Too late! the message has been sent

## 注：

广播模式，只要所有程序监听同一个后台广播路径就可以了

点对点通信模式，可以在Js端使用随机数或者根据TokenId开启监听路径，后台根据用户的TokenId派发到不同端点就可以了