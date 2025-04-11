package com.qianxun.qianxunojbackendwebsocketservice.controller.inner;

import com.qianxun.qianxunojbackendmodel.model.dto.websocket.WsMessageRequest;
import com.qianxun.qianxunojbackendserviceclient.service.WebsocketFeignClient;
import com.qianxun.qianxunojbackendwebsocketservice.handler.WebSocketServer;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/inner")
public class WebsocketInnerController implements WebsocketFeignClient {

    /**
     * 查询执行结果
     *
     * @return
     */
    @PostMapping("/get/test")
    public void test(@RequestBody Object obj) {
        System.out.println(obj);
    }

    @Override
    @PostMapping("/sendMessage")
    public void sendMessageById(WsMessageRequest wsMessageRequest) {
        try {
            WebSocketServer.sendMessageById(wsMessageRequest.getMessage(), wsMessageRequest.getSid());
        } catch (IOException e) {
            System.out.println(wsMessageRequest.getSid() + "推送消息失败：" + wsMessageRequest.getMessage());
        }
    }
}
