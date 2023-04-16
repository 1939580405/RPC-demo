package com.rrtv.rpc.core.discovery;

import com.rrtv.rpc.core.common.ServiceInfo;


public interface DiscoveryService {

    /**
     *  发现
     * @param serviceName
     * @return
     * @throws Exception
     */
    ServiceInfo discovery(String serviceName) throws Exception;

}
