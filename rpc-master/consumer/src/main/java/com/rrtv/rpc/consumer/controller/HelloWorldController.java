package com.rrtv.rpc.consumer.controller;

import com.rrtv.rpc.api.service.HelloWordService;
import com.rrtv.rpc.client.annotation.RpcAutowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HelloWorldController {

    @RpcAutowired(version = "1.0")
    private HelloWordService helloWordService;

    @GetMapping("/hello/world")
    public ResponseEntity<String> pullServiceInfo(@RequestParam("name") String name){
        //相当于将响应体反馈给前端
        return  ResponseEntity.ok(helloWordService.sayHello(name));
    }


}
