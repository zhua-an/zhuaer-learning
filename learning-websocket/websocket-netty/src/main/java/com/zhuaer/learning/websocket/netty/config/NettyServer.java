package com.zhuaer.learning.websocket.netty.config;

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
