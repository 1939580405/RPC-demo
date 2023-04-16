package com.rrtv.rpc.core.discovery;

import com.rrtv.rpc.core.balancer.LoadBalance;
import com.rrtv.rpc.core.common.ServiceInfo;
import com.rrtv.rpc.core.common.ServiceUtil;
import com.rrtv.rpc.core.register.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;


@Slf4j
public class ZookeeperDiscoveryService implements DiscoveryService {

    public static final int BASE_SLEEP_TIME_MS = 1000;
    public static final int MAX_RETRIES = 3;
    public static final String ZK_BASE_PATH = "/demo_rpc";

    //ZooKeeper库中用来发现服务的接口
    private ServiceDiscovery<ServiceInfo> serviceDiscovery;

    private LoadBalance loadBalance;

    public ZookeeperDiscoveryService(String registryAddr, LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
        try {
            //CuratorFramework：ZooKeeper框架式客户端
            //newClient:创建一个具有默认会话超时和默认连接超时的新客户端
            //registryAddr:是ZooKeeper服务器的地址
            CuratorFramework client = CuratorFrameworkFactory.newClient(registryAddr, new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
            client.start();
            JsonInstanceSerializer<ServiceInfo> serializer = new JsonInstanceSerializer<>(ServiceInfo.class);
            this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceInfo.class)
                    .client(client)
                    .serializer(serializer)
                    .basePath(ZK_BASE_PATH)
                    .build();
            this.serviceDiscovery.start();
        } catch (Exception e) {
            log.error("serviceDiscovery start error :{}", e);
        }
    }


    /**
     *  服务发现
     * @param serviceName
     * @return
     * @throws Exception
     */
    @Override
    public ServiceInfo discovery(String serviceName) throws Exception {
        Collection<ServiceInstance<ServiceInfo>> serviceInstances = serviceDiscovery.queryForInstances(serviceName);
        //查询服务之后，如果有相应的服务，就根据负载均衡策略，返回一个服务的信息回去
        return CollectionUtils.isEmpty(serviceInstances) ? null
                : loadBalance.chooseOne(serviceInstances.stream().map(ServiceInstance::getPayload).collect(Collectors.toList()));
    }

}
