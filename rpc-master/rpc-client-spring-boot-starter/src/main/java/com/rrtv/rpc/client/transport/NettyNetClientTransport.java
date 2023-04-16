package com.rrtv.rpc.client.transport;

import com.rrtv.rpc.client.cache.LocalRpcResponseCache;
import com.rrtv.rpc.client.handler.RpcResponseHandler;
import com.rrtv.rpc.core.codec.RpcDecoder;
import com.rrtv.rpc.core.codec.RpcEncoder;
import com.rrtv.rpc.core.common.RpcRequest;
import com.rrtv.rpc.core.common.RpcResponse;
import com.rrtv.rpc.core.common.ServiceInfo;
import com.rrtv.rpc.core.protocol.MessageProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;


@Slf4j
public class NettyNetClientTransport implements NetClientTransport {

    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    private final RpcResponseHandler handler;


    public NettyNetClientTransport() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        handler = new RpcResponseHandler();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                // 解码 是入站操作 将二进制解码成消息
                                .addLast(new RpcDecoder())
                                // 接收响应 入站操作
                                .addLast(handler)
                                // 编码 是出站操作 将消息编写二进制
                                .addLast(new RpcEncoder<>());
                    }
                });
    }

    @Override
    public MessageProtocol<RpcResponse> sendRequest(RequestMetadata metadata) throws Exception {
        //从请求元数据取出协议，协议中有消息头
        MessageProtocol<RpcRequest> protocol = metadata.getProtocol();
        //RpcFuture里面的泛型要放响应类型的数据
        RpcFuture<MessageProtocol<RpcResponse>> future = new RpcFuture<>();
        //将请求id和future对象以键值对的形式存在map里
        LocalRpcResponseCache.add(protocol.getHeader().getRequestId(), future);

        // TCP 连接
        ChannelFuture channelFuture = bootstrap.connect(metadata.getAddress(), metadata.getPort()).sync();
        channelFuture.addListener((ChannelFutureListener) arg0 -> {
            if (channelFuture.isSuccess()) {
                log.info("connect rpc server {} on port {} success.", metadata.getAddress(), metadata.getPort());
            } else {
                log.error("connect rpc server {} on port {} failed.", metadata.getAddress(), metadata.getPort());
                channelFuture.cause().printStackTrace();
                eventLoopGroup.shutdownGracefully();
            }
        });
        // 写入数据
        channelFuture.channel().writeAndFlush(protocol);
        //异步调用，先返回future对象，如果请求有超时时间的话，就用future里的超时等待，不然就用正常的等待，
        //到这里线程会阻塞，等到future里面的响应被设置好后，才会被返回
        return metadata.getTimeout() != null ? future.get(metadata.getTimeout(), TimeUnit.MILLISECONDS) : future.get();
    }
}
