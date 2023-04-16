package com.rrtv.rpc.server.config;

import com.rrtv.rpc.core.register.RegistryService;
import com.rrtv.rpc.core.register.ZookeeperRegistryService;
import com.rrtv.rpc.server.RpcServerProvider;
import com.rrtv.rpc.server.transport.NettyRpcServer;
import com.rrtv.rpc.server.transport.RpcServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration注解的作用：声明一个类为配置类，用于取代bean.xml配置文件注册bean对象。
@Configuration
//在spring开发过程中我们常使用到@ConfigurationProperties注解，通常是用来将properties和yml配置文件属性转化为bean对象使用。
//@EnableConfigurationProperties注解使ConfigurationProperties注解生效
@EnableConfigurationProperties(RpcServerProperties.class)
public class RpcServerAutoConfiguration {
    //相当于application.properties的实例化
    @Autowired
    private RpcServerProperties properties;

    //@Bean是一个方法级别上的注解，主要用在@Configuration注解的类里，也可以用在@Component注解的类里。添加的bean的id为方法名
    @Bean
    //@ConditionalOnMissingBean，它是修饰bean的一个注解，
    //主要实现的是，当你的bean被注册之后，如果而注册相同类型的bean，就不会成功，它会保证你的bean只有一个，即你的实例只有一个。
    //该注解表示，如果存在它修饰的类的bean，则不需要再创建这个bean
    @ConditionalOnMissingBean
    public RegistryService registryService() {
        //连接到ZooKeeper，在ZooKeeper中创建节点，开启服务查找
        return new ZookeeperRegistryService(properties.getRegistryAddr());
    }

    @Bean
    @ConditionalOnMissingBean(RpcServer.class)
    RpcServer RpcServer() {
        return new NettyRpcServer();
    }

    @Bean
    @ConditionalOnMissingBean(RpcServerProvider.class)
    RpcServerProvider rpcServerProvider(@Autowired RegistryService registryService,
                                        @Autowired RpcServer rpcServer,
                                        @Autowired RpcServerProperties rpcServerProperties){
        return new RpcServerProvider(registryService, rpcServer, rpcServerProperties);
    }
}
