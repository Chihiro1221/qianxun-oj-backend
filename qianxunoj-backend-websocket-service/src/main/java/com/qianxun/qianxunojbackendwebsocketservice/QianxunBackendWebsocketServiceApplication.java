package com.qianxun.qianxunojbackendwebsocketservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.qianxun")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.qianxun.qianxunojbackendserviceclient.service"})
public class QianxunBackendWebsocketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QianxunBackendWebsocketServiceApplication.class, args);
    }

}
