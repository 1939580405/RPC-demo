package com.rrtv.rpc.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/*相比较于传统的 Spring 应用，搭建一个 SpringBoot 应用，我们只需要引入一个注解 @SpringBootApplication，就可以成功运行*/
//TODO 为什么在自动配置的时候，可以将带有@RpcService的接口实现类给暴露出去
@SpringBootApplication
@ComponentScan("com.rrtv.rpc")
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

}
