package com.rrtv.rpc.core.register;

import com.rrtv.rpc.core.common.ServiceInfo;

import java.io.IOException;

/**
 * @Classname RegistryService
 * @Description 服务注册发现
 */
public interface RegistryService {
    /***
     *
     * @param serviceInfo 注册的服务的基本信息
     * @throws Exception
     */
    void register(ServiceInfo serviceInfo) throws Exception;

    void unRegister(ServiceInfo serviceInfo) throws Exception;

    void destroy() throws IOException;

}
