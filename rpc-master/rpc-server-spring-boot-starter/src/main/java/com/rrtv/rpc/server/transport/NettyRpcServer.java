package com.rrtv.rpc.server.transport;

import com.rrtv.rpc.core.codec.RpcDecoder;
import com.rrtv.rpc.core.codec.RpcEncoder;
import com.rrtv.rpc.server.handler.RpcRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

@Slf4j
public class NettyRpcServer implements RpcServer {

    @Override
    public void  start(int port) {
        EventLoopGroup boss = new NioEventLoopGroup();//创建boos线程组，用于服务端接受客户端的连接
        EventLoopGroup worker = new NioEventLoopGroup();//创建worker线程组，用于进行SocketChannel的数据读写，处理业务逻辑

        try {
            //Java提供InetAddress类来封装IP地址或域或名
            //getLocalHost():获取本机对应的InetAddress对象
            String serverAddress = InetAddress.getLocalHost().getHostAddress();
            //netty服务端的启动类
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)//设置要被实例化的NioServerSocketChannel类
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    // 协议编码
                                    .addLast(new RpcEncoder())
                                    // 协议解码
                                    .addLast(new RpcDecoder())
                                    // 请求处理器
                                    .addLast(new RpcRequestHandler());
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            /*
            * Netty 里面的IO操作全部是异步的。这意味着，IO操作会立即返回，
            * 但是在调用结束时，无法保证IO操作已完成。取而代之，将会返回给你一个ChannelFuture 实例，提供IO操作的结果信息或状态。
            一个ChannelFuture 要么是未完成，要么是已完成。
            * 当一个IO操作开始，一个新的future对象被创建。
            * 这个新的future 初始化未完成 - 它既不是成功，也不是失败，也不是被取消。
            * 因为IO操作还没有完全结束。如果IO操作已经完成，那它要么是成功，要么是失败，要么是被取消，
            * 这个future会被标记成已完成并伴随其他信息，比如失败的原因。请注意，即使是失败和被取消已归属于完成状态。*/
            //sync()会让线程阻塞一会儿，直到Future对象完成，sync() 会等待异步事件执行完成，并且返回自身
            ChannelFuture channelFuture = bootstrap.bind(serverAddress, port).sync();
            log.info("server addr {} started on port {}", serverAddress, port);
            channelFuture.channel().closeFuture().sync();
            //bind 方法返回的 future 用于等待底层网络组件启动完成
            //closeFuture 用于等待网络组件关闭完成，目前这里没有关闭这个操作
        } catch (Exception e) {

        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
