package com.qianxun.qianxunojbackenduserservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.qianxun.qianxunojbackenduserservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.qianxun")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.qianxun.qianxunojbackendserviceclient.service"})
public class QianxunBackendUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QianxunBackendUserServiceApplication.class, args);
    }

}
