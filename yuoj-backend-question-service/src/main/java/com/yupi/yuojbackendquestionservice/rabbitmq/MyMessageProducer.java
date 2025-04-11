package com.yupi.yuojbackendquestionservice.rabbitmq;

import com.yupi.yuojbackendmodel.model.dto.questionsubmit.JudgeStatusRequest;
import com.yupi.yuojbackendmodel.model.vo.TokenVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

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