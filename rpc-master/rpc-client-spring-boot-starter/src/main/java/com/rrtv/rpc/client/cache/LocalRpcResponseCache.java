package com.rrtv.rpc.client.cache;

import com.rrtv.rpc.client.transport.RpcFuture;
import com.rrtv.rpc.core.common.RpcResponse;
import com.rrtv.rpc.core.protocol.MessageProtocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  请求和响应映射对象
 */
public class LocalRpcResponseCache {
    //concurrentHashMap是一个支持高并发更新与查询的哈希表
    //RpcFuture<MessageProtocol<RpcResponse>>中，第一个泛型是在RpcFuture中的响应，第二个泛型是指MessageProtocol中的响应
    //MessageProtocol是为了给响应加上响应头
    private static Map<String, RpcFuture<MessageProtocol<RpcResponse>>> requestResponseCache = new ConcurrentHashMap<>();


    /**
     *  添加请求和响应的映射关系
     * @param reqId
     * @param future
     */

    public static void add(String reqId, RpcFuture<MessageProtocol<RpcResponse>> future){
        requestResponseCache.put(reqId, future);
    }

    /**
     *  设置响应数据，将响应从缓存中取出来
     * @param reqId
     * @param messageProtocol
     */
    public static void fillResponse(String reqId, MessageProtocol<RpcResponse> messageProtocol){
        // 获取缓存中的 future
        RpcFuture<MessageProtocol<RpcResponse>> future = requestResponseCache.get(reqId);

        // 设置数据
        future.setResponse(messageProtocol);

        // 移除缓存
        //TODO 为啥要将请求id移除
        requestResponseCache.remove(reqId);
    }
}
