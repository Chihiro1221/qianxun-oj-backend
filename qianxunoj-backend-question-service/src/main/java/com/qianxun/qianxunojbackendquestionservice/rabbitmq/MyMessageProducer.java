package com.qianxun.qianxunojbackendquestionservice.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class MyMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     *
     * @param exchange
     * @param routingKey
     */
    public void sendMessage(String exchange, String routingKey, String payload) {
        rabbitTemplate.convertAndSend(exchange, routingKey, payload);
    }

}