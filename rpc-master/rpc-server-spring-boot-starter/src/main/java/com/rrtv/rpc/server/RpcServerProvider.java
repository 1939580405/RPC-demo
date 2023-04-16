package com.rrtv.rpc.server;

import com.rrtv.rpc.core.common.ServiceInfo;
import com.rrtv.rpc.core.common.ServiceUtil;
import com.rrtv.rpc.core.register.RegistryService;
import com.rrtv.rpc.server.annotation.RpcService;
import com.rrtv.rpc.server.config.RpcServerProperties;
import com.rrtv.rpc.server.store.LocalServerCache;
import com.rrtv.rpc.server.transport.RpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.PreDestroy;
import java.net.InetAddress;

//@Slf4j是用作日志输出的
/*(1)、加入@Component注解后，就可以将对象交给spring管理。
  (2)、当Spring 容器初始化完成后, Spring会遍历所有实现CommandLineRunner接口的类, 并运行其run() 方法.
  这里是配合@Bean使用
 */
@Slf4j
public class RpcServerProvider implements BeanPostProcessor, CommandLineRunner {
    //ZooKeeper注册中心
    private RegistryService registryService;

    //ZooKeeper的相关配置
    private RpcServerProperties properties;

    private RpcServer rpcServer;

    public RpcServerProvider(RegistryService registryService, RpcServer rpcServer, RpcServerProperties properties) {
        this.registryService = registryService;
        this.properties = properties;
        this.rpcServer = rpcServer;
    }


    /**
     * 所有bean 实例化之后处理
     * <p>
     * 暴露服务注册到注册中心
     * <p>
     * 容器启动后开启netty服务处理请求
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //所有带有RpcService注解的bean都要经过处理
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        if (rpcService != null) {
            try {
                //获取实体类的名字
                String serviceName = rpcService.interfaceType().getName();
                String version = rpcService.version();
                //将服务放到缓存中
                LocalServerCache.store(ServiceUtil.serviceKey(serviceName, version), bean);

                ServiceInfo serviceInfo = new ServiceInfo();
                serviceInfo.setServiceName(ServiceUtil.serviceKey(serviceName, version));
                serviceInfo.setPort(properties.getPort());
                serviceInfo.setAddress(InetAddress.getLocalHost().getHostAddress());
                //这里的AppName是从provider的yaml文件中取出来的
                serviceInfo.setAppName(properties.getAppName());

                // 服务注册，在ZooKeeper中多个一个节点：com.rrtv.rpc.api.service.HelloWordService-1.0
                //端口号，ip地址目前还没看见
                //但是在服务名的节点下有一个 [3ff39ca7-521d-4e34-a24d-55bcc1ed4896]
                //这个节点的名称是服务的id由Apache Curator里面的实例随机生成的
                /*{"name":"com.rrtv.rpc.api.service.HelloWordService-1.0","id":"549805e7-954d-4621-8602-5a5cb984c91f",
                "address":"192.168.120.1","port":9991,"sslPort":null,
                "payload":{"@class":"com.rrtv.rpc.core.common.ServiceInfo","appName":"provider1","serviceName":"com.rrtv.rpc.api.service.HelloWordService-1.0","version":null,"address":"192.168.120.1","port":9991},
                "registrationTimeUTC":1681453859048,
                "serviceType":"DYNAMIC","uriSpec":null}*/
                //TODO 到时候看看消费者是怎么消费的
                registryService.
                        register(serviceInfo);
            } catch (Exception ex) {
                log.error("服务注册出错:{}", ex);
            }
        }
        return bean;
    }

    /*当Spring 容器初始化完成后, Spring会遍历所有实现CommandLineRunner接口的类, 并运行其run() 方法.
            这里是配合@Bean使用*/
    /**
     * 启动rpc服务 处理请求
     *
     * @param args
     */
    @Override
    public void run(String... args) {
        new Thread(() -> rpcServer.start(properties.getPort())).start();
        log.info(" rpc server :{} start, appName :{} , port :{}", rpcServer, properties.getAppName(), properties.getPort());
        //只有在应用停止之后，后面的删除才会触发
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // 关闭之后把服务从ZK上清楚
                registryService.destroy();
            }catch (Exception ex){
                log.error("", ex);
            }

        }));
    }

}
