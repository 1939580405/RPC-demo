package com.rrtv.rpc.client.processor;

import com.rrtv.rpc.client.annotation.RpcAutowired;
import com.rrtv.rpc.client.config.RpcClientProperties;
import com.rrtv.rpc.client.proxy.ClientStubProxyFactory;
import com.rrtv.rpc.core.discovery.DiscoveryService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @Classname RpcClientProcessor
 * @Description bean 后置处理器 获取所有bean
 * 判断bean字段是否被 {@link com.rrtv.rpc.client.annotation.RpcAutowired } 注解修饰
 * 动态修改被修饰字段的值为代理对象 {@link ClientStubProxyFactory}
 */
public class RpcClientProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    private ClientStubProxyFactory clientStubProxyFactory;

    private DiscoveryService discoveryService;

    private RpcClientProperties properties;

    private ApplicationContext applicationContext;

    public RpcClientProcessor(ClientStubProxyFactory clientStubProxyFactory, DiscoveryService discoveryService, RpcClientProperties properties) {
        this.clientStubProxyFactory = clientStubProxyFactory;
        this.discoveryService = discoveryService;
        this.properties = properties;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            //返回指定bean的注册BeanDefinition，允许访问其属性值和构造函数参数值（可以在bean工厂后期处理期间进行修改）。
            //返回的BeanDefinition对象不应是副本，而应是在工厂中注册的原始定义对象。这意味着，如果需要，它应该可以转换为更具体的实现类型
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            //返回此bean定义的当前bean类名。
            //请注意，如果子定义覆盖/继承其父级的类名，则不必是运行时使用的实际类名。
            // 此外，这可能只是调用工厂方法的类，或者在调用方法的工厂bean引用的情况下，它甚至可能是空的。
            // 因此，不要将其视为运行时的最终bean类型，而只将其用于单个bean定义级别的解析
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.getClass().getClassLoader());
                //遍历class的所有属性，如果发现有RpcAutowired的属性就设置代理
                ReflectionUtils.doWithFields(clazz, field -> {
                    RpcAutowired rpcAutowired = AnnotationUtils.getAnnotation(field, RpcAutowired.class);
                    if (rpcAutowired != null) {
                        Object bean = applicationContext.getBean(clazz);
                        //将此对象的可访问标志设置为指示的布尔值。
                        //值true表示反射对象在使用时应禁止Java语言访问检查。
                        //值false表示反射的对象应该强制执行Java语言访问检查。
                        //首先，如果存在安全管理器，则使用ReflectPermission（“suppressAccessChecks”）权限调用其checkPermission方法。
                        //如果标志为true，但此对象的可访问性可能不会更改，则会引发SecurityException
                        field.setAccessible(true);
                        // 修改为代理对象
                        ReflectionUtils.setField(field, bean, clientStubProxyFactory.getProxy(field.getType(), rpcAutowired.version(), discoveryService, properties));
                    }
                });
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
