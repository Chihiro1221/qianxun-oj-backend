package com.qianxun.qianxunojbackendserviceclient.service;


import com.qianxun.qianxunojbackendmodel.model.dto.websocket.WsMessageRequest;
import feign.Logger;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * websocket服务
 */
@FeignClient(name = "qianxun-backend-websocket-service", path = "/api/ws/inner")
public interface WebsocketFeignClient {

    /**
     * 发送消息向指定sessionId
     *
     * @param wsMessageRequest
     * @return
     */
    @PostMapping("/sendMessage")
    void sendMessageById(@RequestBody WsMessageRequest wsMessageRequest);

}
