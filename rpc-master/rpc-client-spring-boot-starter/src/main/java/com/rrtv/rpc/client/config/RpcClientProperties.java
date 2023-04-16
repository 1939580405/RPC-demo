package com.rrtv.rpc.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
public class RpcClientProperties {

    /**
     *  负载均衡
     */
    private String balance;

    /**
     *  序列化
     */
    private String serialization;

    /**
     *  服务发现地址，注册中心的地址
     */
    private String discoveryAddr = "127.0.0.1:2181";

    /**
     *  服务调用超时
     */
    private Integer timeout;

}
