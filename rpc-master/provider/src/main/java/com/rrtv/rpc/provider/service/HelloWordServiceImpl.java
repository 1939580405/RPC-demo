package com.rrtv.rpc.provider.service;

import com.rrtv.rpc.api.service.HelloWordService;
import com.rrtv.rpc.server.annotation.RpcService;


@RpcService(interfaceType = HelloWordService.class, version = "1.0")
public class HelloWordServiceImpl implements HelloWordService {

    @Override
    public String sayHello(String name) {
        return String.format("您好：%s, rpc 调用成功", name);
    }

}
