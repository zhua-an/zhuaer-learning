# websocket-netty

SpringBoot+Netty+WebSocket实现实时通信

Netty 是一个利用 Java 的高级网络的能力，隐藏其背后的复杂性而提供一个易于使用的 API 的客户端/服务器框架。

用Netty搭建一个WebSocket服务器整体上需要三样东西，不管是不是用的SpringBoot框架，这三样东西是必不可少的。

1.启动服务器的类（`NettyServer`），会进行一些初步的配置工作。

2.助手类（`Handler`），有自己定义的助手类，也有Netty提供的一些基本的助手类，比如对Http、WebSocket支持的助手类。

3.初始化器（`Initializer`），我们下面使用的是主从线程模型，从线程组里会分配出不同`channel`去处理不同客户端的请求，而每个`channel`里就会有各种助手类去实现一些功能。初始化器的作用就是对各种助手类进行绑定。

## 添加依赖

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
    	<groupId>io.netty</groupId>
    	<artifactId>netty-all</artifactId>
    	<version>5.0.0.alpha2</version>
    </dependency>

## 服务器启动类

    import com.zhuaer.learning.websocket.netty.handler.MyWebSocketHandler;
    import io.netty.bootstrap.ServerBootstrap;
    import io.netty.channel.ChannelFuture;
    import io.netty.channel.ChannelInitializer;
    import io.netty.channel.ChannelOption;
    import io.netty.channel.EventLoopGroup;
    import io.netty.channel.nio.NioEventLoopGroup;
    import io.netty.channel.socket.SocketChannel;
    import io.netty.channel.socket.nio.NioServerSocketChannel;
    import io.netty.handler.codec.http.HttpObjectAggregator;
    import io.netty.handler.codec.http.HttpServerCodec;
    import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
    import io.netty.handler.stream.ChunkedWriteHandler;
    
    /**
     * @ClassName NettyServer
     * @Description 服务器启动类
     * @Author zhua
     * @Date 2020/10/28 10:20
     * @Version 1.0
     */
    public class NettyServer {
        private final int port;
    
        public NettyServer(int port) {
            this.port = port;
        }
    
        public void start() throws Exception {
            //创建主线程组，接收请求
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            //创建从线程组，处理主线程组分配下来的io操作
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                //创建netty服务器
                ServerBootstrap sb = new ServerBootstrap();
                sb.option(ChannelOption.SO_BACKLOG, 1024);
                sb.group(group, bossGroup) // 绑定线程池
                        .channel(NioServerSocketChannel.class) // 指定使用的channel
                        .localAddress(this.port)// 绑定监听端口
                        .childHandler(new ChannelInitializer<SocketChannel>() { // 绑定客户端连接时候触发操作
    
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                System.out.println("收到新连接");
                                //websocket协议本身是基于http协议的，所以这边也要使用http解编码器
                                ch.pipeline().addLast(new HttpServerCodec());
                                //以块的方式来写的处理器 支持写大数据流
                                ch.pipeline().addLast(new ChunkedWriteHandler());
                                //http聚合器
                                ch.pipeline().addLast(new HttpObjectAggregator(64*1024));
                                //添加自定义的助手类
                                ch.pipeline().addLast(new MyWebSocketHandler());
                                //websocket支持,设置路由
                                /* 说明： 1、对应webSocket，它的数据是以帧（frame）的形式传递 2、浏览器请求时 ws://localhost:8000/xxx 表示请求的uri 3、核心功能是将http协议升级为ws协议，保持长连接 */
                                ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65536 * 10));
                            }
                        })
                        //BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50
                        .option(ChannelOption.SO_BACKLOG, 128)
                        //是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                
                //启动server
                ChannelFuture cf = sb.bind().sync(); // 服务器异步创建绑定
                System.out.println(NettyServer.class + " 启动正在监听： " + cf.channel().localAddress());
                cf.channel().closeFuture().sync(); // 关闭服务器通道
            } finally {
                group.shutdownGracefully().sync(); // 释放线程池资源
                bossGroup.shutdownGracefully().sync();
            }
        }
    }

## 自定义助手类

这个类就是业务的核心，客户端的请求会在这里处理。比如客户端连接、客户端发送消息、给客户端发送消息等等。

自定义助手类需要重写的方法可以根据自己的需求重写，这里就不把每个方法都重写一遍了，完整的大家可以去找找文档看看。

- channelActive与客户端建立连接
- channelInactive与客户端断开连接
- messageReceived客户端发送消息处理


    import com.alibaba.fastjson.JSON;
    import io.netty.channel.Channel;
    import io.netty.channel.ChannelHandlerContext;
    import io.netty.channel.SimpleChannelInboundHandler;
    import io.netty.channel.group.ChannelGroup;
    import io.netty.channel.group.DefaultChannelGroup;
    import io.netty.handler.codec.http.FullHttpRequest;
    import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
    import io.netty.util.AttributeKey;
    import io.netty.util.concurrent.GlobalEventExecutor;
    
    import java.util.HashMap;
    import java.util.Map;
    
    /**
     * @ClassName MyWebSocketHandler
     * @Description 初始化器
     * @Author zhua
     * @Date 2020/10/28 11:28
     * @Version 1.0
     */
    public class MyWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
        //TextWebSocketFrame是netty用于处理websocket发来的文本对象
    
        //通道组池，管理所有websocket连接
        //GlobalEventExecutor.INSTANCE 是全局的事件执行器，是一个单例
        public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    
        //客户端建立连接
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("与客户端建立连接，通道开启！");
    
            //添加到channelGroup通道组
            this.channelGroup.add(ctx.channel());
            System.out.println("[新用户] - " + ctx.name() + " -  " + ctx.channel().id() + " 加入");
            sendAllMessage("[新用户] - " + ctx.name() + " -  " + ctx.channel().id() + " 加入");
        }
    
        //关闭连接
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("与客户端断开连接，通道关闭！");
            //添加到channelGroup 通道组
            this.channelGroup.remove(ctx.channel());
        }
    
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //首次连接是FullHttpRequest，处理参数
            if (null != msg && msg instanceof FullHttpRequest) {
                FullHttpRequest request = (FullHttpRequest) msg;
                String uri = request.uri();
    
                Map paramMap=getUrlParams(uri);
                System.out.println("接收到的参数是："+JSON.toJSONString(paramMap));
                //如果url包含参数，需要处理
                if(uri.contains("?")){
                    String newUri=uri.substring(0,uri.indexOf("?"));
                    System.out.println(newUri);
                    request.setUri(newUri);
                }
    
            }else if(msg instanceof TextWebSocketFrame){
                //正常的TEXT消息类型
                TextWebSocketFrame frame=(TextWebSocketFrame)msg;
                System.out.println("客户端收到服务器数据：" +frame.text());
                sendAllMessage(frame.text());
            }
            super.channelRead(ctx, msg);
        }
    
        //接收到客户都发送的消息，需要编解码的才会去用messageReceived
        @Override
        protected void messageReceived(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
    
        }
    
        //出现异常
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            Channel incoming = ctx.channel();
            System.out.println("SimpleChatClient:" + incoming.remoteAddress()+"异常");
            //异常出现就关闭连接
            cause.printStackTrace();
            ctx.close();
        }
    
        private void sendAllMessage(String message){
            //收到信息后，群发给所有channel
            this.channelGroup.writeAndFlush( new TextWebSocketFrame(message));
        }
    
        //给某个人发送消息
        private void sendMessage(ChannelHandlerContext ctx, String message) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(message));
        }
    
        //给每个人发送消息,除发消息人外
        private void sendAllMessages(ChannelHandlerContext ctx,String message) {
            for(Channel channel:this.channelGroup){
                if(!channel.id().asLongText().equals(ctx.channel().id().asLongText())){
                    channel.writeAndFlush(new TextWebSocketFrame(message));
                }
            }
        }
    
        //删除用户与channel的对应关系
        private void removeUserId(ChannelHandlerContext ctx){
            AttributeKey<String> key = AttributeKey.valueOf("userId");
            String userId = ctx.channel().attr(key).get();
            this.channelGroup.remove(userId);
        }
    
        private static Map getUrlParams(String url){
            Map<String,String> map = new HashMap<>();
            url = url.replace("?",";");
            if (!url.contains(";")){
                return map;
            }
            if (url.split(";").length > 0){
                String[] arr = url.split(";")[1].split("&");
                for (String s : arr){
                    String key = s.split("=")[0];
                    String value = s.split("=")[1];
                    map.put(key,value);
                }
                return  map;
            }else{
                return map;
            }
        }
    
    }

## 前端创建WebSocket对象进行访问

通过socket就可以用一些api进行发送消息，接收消息的操作。然后把接收的数据按各自的需求展示出来就行了

    <!DOCTYPE html>
    <html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Netty-Websocket</title>
        <script type="text/javascript">
            // by zhengkai.blog.csdn.net
            var socket;
            // if(!window.WebSocket){
            //     window.WebSocket = window.MozWebSocket;
            // }
            if(window.WebSocket){
                if ('WebSocket' in window) {
                    socket = new WebSocket("ws://127.0.0.1:8000/ws");
                } else if ('MozWebSocket' in window) {
                    socket = new MozWebSocket("ws://127.0.0.1:8000/ws");
                } else {
                    socket = new SockJS("ws://127.0.0.1:8000/ws");
                }
                socket.onmessage = function(event){
                    var ta = document.getElementById('responseText');
                    ta.value += event.data+"\r\n";
                };
                socket.onopen = function(event){
                    var ta = document.getElementById('responseText');
                    ta.value = "Netty-WebSocket服务器。。。。。。连接  \r\n";
                };
                socket.onclose = function(event){
                    var ta = document.getElementById('responseText');
                    ta.value = "Netty-WebSocket服务器。。。。。。关闭 \r\n";
                };
            }else{
                alert("您的浏览器不支持WebSocket协议！");
            }
            function send(message){
                if(!window.WebSocket){return;}
                if(socket.readyState == WebSocket.OPEN){
                    socket.send(message);
                }else{
                    alert("WebSocket 连接没有建立成功！");
                }
    
            }
    
        </script>
    </head>
    <body>
    <form onSubmit="return false;">
        <label>ID</label><input type="text" name="uid" value="${uid!!}" /> <br />
        <label>TEXT</label><input type="text" name="message" value="这里输入消息" /> <br />
        <br /> <input type="button" value="发送ws消息"
                      onClick="send(this.form.uid.value+':'+this.form.message.value)" />
        <hr color="black" />
        <h3>服务端返回的应答消息</h3>
        <textarea id="responseText" style="width: 1024px;height: 300px;"></textarea>
    </form>
    </body>
    </html>
    

## 启动（在SpringBoot启动类中加入以下内容）

    @SpringBootApplication
    public class Application {
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
            //在SpringBoot启动类中加入以下内容
            try {
                new NettyServer(8000).start();
            } catch (Exception e) {
                System.out.println("NettyServerError:" + e.getMessage());
            }
        }
    }

