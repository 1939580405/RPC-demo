package com.rrtv.rpc.core.register;

import com.rrtv.rpc.core.common.ServiceInfo;
import com.rrtv.rpc.core.common.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.io.IOException;

/**
 * @Classname ZookeeperRegistryService
 */
@Slf4j
public class ZookeeperRegistryService implements RegistryService {

    public static final int BASE_SLEEP_TIME_MS = 1000;
    public static final int MAX_RETRIES = 3;
    public static final String ZK_BASE_PATH = "/demo_rpc";

    //ZooKeeper库中用来发现服务的接口
    private ServiceDiscovery<ServiceInfo> serviceDiscovery;

    //构造方法
    //TODO 顺便在ZooKeeper中创建一个节点
    /***
     * @param registryAddr 要连接的服务器列表
     */
    public ZookeeperRegistryService(String registryAddr) {
        try {
            //CuratorFramework：ZooKeeper框架式客户端
            //newClient:创建一个具有默认会话超时和默认连接超时的新客户端
            //registryAddr:是ZooKeeper服务器的地址
            CuratorFramework client = CuratorFrameworkFactory.newClient(registryAddr, new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
            client.start();
            //使用Jackson作为JSON进行序列化/反序列化的序列化程序
            JsonInstanceSerializer<ServiceInfo> serializer = new JsonInstanceSerializer<>(ServiceInfo.class);
            this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceInfo.class)
                    .client(client)
                    .serializer(serializer)
                    .basePath(ZK_BASE_PATH)//放在ZooKeeper的对应节点下
                    .build();
            //服务发现设置好后要启动一下
            this.serviceDiscovery.start();
        } catch (Exception e) {
            log.error("serviceDiscovery start error :{}", e);
        }
    }

    @Override
    public void register(ServiceInfo serviceInfo) throws Exception {
        ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance.<ServiceInfo>builder()
                .name(serviceInfo.getServiceName())
                .address(serviceInfo.getAddress())
                .port(serviceInfo.getPort())
                .payload(serviceInfo)
                .build();
        //注册服务
        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceInfo serviceInfo) throws Exception {
        ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance
                .<ServiceInfo>builder()
                .name(serviceInfo.getServiceName())
                .address(serviceInfo.getAddress())
                .port(serviceInfo.getPort())
                .payload(serviceInfo)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public void destroy() throws IOException {
        serviceDiscovery.close();
    }

}
