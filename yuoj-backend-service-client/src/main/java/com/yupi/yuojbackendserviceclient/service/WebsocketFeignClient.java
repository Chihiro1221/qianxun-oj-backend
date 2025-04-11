package com.yupi.yuojbackendserviceclient.service;


import com.yupi.yuojbackendmodel.model.dto.questionsubmit.QuestionSubmitRequest;
import com.yupi.yuojbackendmodel.model.dto.websocket.WsMessageRequest;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import com.yupi.yuojbackendmodel.model.vo.JudgeStatusVO;
import com.yupi.yuojbackendmodel.model.vo.TokenVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * websocket服务
 */
@FeignClient(name = "yuoj-backend-websocket-service", path = "/api/ws/inner")
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
