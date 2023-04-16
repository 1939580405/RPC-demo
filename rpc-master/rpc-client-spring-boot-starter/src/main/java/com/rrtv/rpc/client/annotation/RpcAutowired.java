package com.rrtv.rpc.client.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;


@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Autowired
public @interface RpcAutowired {

    String version() default "1.0";

}
