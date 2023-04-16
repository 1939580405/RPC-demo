package com.rrtv.rpc.consumer.loadbalance;

import com.rrtv.rpc.core.balancer.LoadBalance;
import com.rrtv.rpc.core.common.ServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
//@Component
public class FirstLoadBalance implements LoadBalance {

    @Override
    public ServiceInfo chooseOne(List<ServiceInfo> services) {
        log.info("---------FirstLoadBalance-----------------");
        return services.get(0);
    }
}
