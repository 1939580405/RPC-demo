package com.rrtv.rpc.server.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;


//@Target(ElementType.TYPE)——接口、类、枚举、注解，表示这么多作用范围都可以用
@Target({ElementType.TYPE})
//在运行时有效（即运行时保留）
@Retention(RetentionPolicy.RUNTIME)
//Documented注解表明这个注释是由 javadoc记录的，在默认情况下也有类似的记录工具。 如果一个类型声明被注释了文档化，它的注释成为公共API的一部分
@Documented
//让 Spring 容器帮我们自动装配 bean
@Service
public @interface RpcService {

    /**
     *  暴露服务接口类型
     * @return
     */
    //Class<?> 中的 ? 是通配符，其实就是表示任意符合泛类定义条件的类，
    // 和直接使用 Class 效果基本一致，但是这样写更加规范，在某些类型转换时可以避免不必要的 unchecked 错误。
    Class<?> interfaceType() default Object.class;

    /**
     *  服务版本
     * @return
     */
    String version() default "1.0";
}
