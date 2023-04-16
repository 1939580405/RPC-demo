package com.rrtv.rpc.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

//@Data ： 注在类上，提供类的get、set、equals、hashCode、toString等方法
@Data
//在 SpringBoot 中，当想需要获取到配置文件数据时，除了可以用 Spring 自带的 @Value 注解外，SpringBoot 还提供了一种更加方便的方式
// @ConfigurationProperties。只要在 Bean 上添加上了这个注解，指定好配置文件的前缀，那么对应的配置文件数据就会自动填充到 Bean 中。
@ConfigurationProperties(prefix = "rpc.server")
public class RpcServerProperties {

    /**
     *  服务启动端口
     */
    private Integer port = 8090;

    /**
     *  服务名称
     */
    private String appName;

    /**
     *  注册中心地址
     */
    private String registryAddr = "127.0.0.1:2181";

}
