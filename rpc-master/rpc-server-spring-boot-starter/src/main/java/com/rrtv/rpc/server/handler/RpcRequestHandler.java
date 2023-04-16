package com.rrtv.rpc.server.handler;

import com.rrtv.rpc.core.common.RpcRequest;
import com.rrtv.rpc.core.common.RpcResponse;
import com.rrtv.rpc.core.protocol.MessageHeader;
import com.rrtv.rpc.core.protocol.MessageProtocol;
import com.rrtv.rpc.core.protocol.MsgStatus;
import com.rrtv.rpc.core.protocol.MsgType;
import com.rrtv.rpc.server.store.LocalServerCache;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 请求处理器中的消息协议的消息体是RpcRequest
 */
@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<MessageProtocol<RpcRequest>> {

    //线程池
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));

    /***
     *
     * @param channelHandlerContext
     * ChannelHandlerContext是对handler，channel和pipline的封装，ChannelHandlerContext中的业务逻辑，
     * 实际上是调用的是底层的handler的对应方法。这也是我们在自定义handler中需要实现的方法
     * @param rpcRequestMessageProtocol
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProtocol<RpcRequest> rpcRequestMessageProtocol) throws Exception {
        // 多线程处理每个请求
        threadPoolExecutor.submit(() -> {
            MessageProtocol<RpcResponse> resProtocol = new MessageProtocol<>();
            //先把响应new出来
            RpcResponse response = new RpcResponse();
            //从请求协议中将消息头给取出来
            MessageHeader header = rpcRequestMessageProtocol.getHeader();
            // 设置头部消息类型为响应
            header.setMsgType(MsgType.RESPONSE.getType());
            try {
                //先从请求中获取消息体，然后把请求中的消息体当成参数，去调用服务提供者的方法
                Object result = handle(rpcRequestMessageProtocol.getBody());
                response.setData(result);
                header.setStatus(MsgStatus.SUCCESS.getCode());
                resProtocol.setHeader(header);
                resProtocol.setBody(response);
            } catch (Throwable throwable) {
                header.setStatus(MsgStatus.FAIL.getCode());
                response.setMessage(throwable.toString());
                log.error("process request {} error", header.getRequestId(), throwable);
            }
            // 把数据写回去
            channelHandlerContext.writeAndFlush(resProtocol);
        });

    }


    //利用反射来调用provider的对应的方法
    /**
     * 反射调用获取数据
     *
     * @param request
     * @return
     */
    private Object handle(RpcRequest request) {
        try {
            Object bean = LocalServerCache.get(request.getServiceName());
            if (bean == null) {
                throw new RuntimeException(String.format("service not exist: %s !", request.getServiceName()));
            }
            // 反射调用
            Method method = bean.getClass().getMethod(request.getMethod(), request.getParameterTypes());
            return method.invoke(bean, request.getParameters());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
