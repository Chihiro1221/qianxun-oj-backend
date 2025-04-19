package com.qianxun.qianxunojbackendwebsocketservice.handler;

import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.qianxun.qianxunojbackendcommon.common.ErrorCode;
import com.qianxun.qianxunojbackendcommon.exception.BusinessException;
import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.QuestionSubmitRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.User;
import com.qianxun.qianxunojbackendmodel.model.enums.JudgeInfoMessageEnum;
import com.qianxun.qianxunojbackendmodel.model.vo.JudgeStatusVO;
import com.qianxun.qianxunojbackendserviceclient.service.JudgeFeignClient;
import com.qianxun.qianxunojbackendserviceclient.service.QuestionFeignClient;
import com.qianxun.qianxunojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/judge/{userId}")
@Component
@Slf4j
public class WebSocketServer {
    private static int onlineCount = 0;
    //private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * socket session会话
     */
    private static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**
     * userId -> session
     */
    private static ConcurrentHashMap<String, Long> userIdSessionMap = new ConcurrentHashMap<>();
    private static JudgeFeignClient judgeFeignClient;
    private static QuestionFeignClient questionFeignClient;
    private static UserFeignClient userFeignClient;

    private Session session;

    @Resource
    public void setJudgeFeignClient(JudgeFeignClient judgeFeignClient) {
        WebSocketServer.judgeFeignClient = judgeFeignClient;
    }

    @Resource
    public void setQuestionFeignClient(QuestionFeignClient questionFeignClient) {
        WebSocketServer.questionFeignClient = questionFeignClient;
    }

    @Resource
    public void setUserFeignClient(UserFeignClient userFeignClient) {
        WebSocketServer.userFeignClient = userFeignClient;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        this.session = session;
        webSocketMap.put(session.getId(), this);
        userIdSessionMap.put(session.getId(), userId);
        User user = userFeignClient.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        addOnlineCount();
        //log.info("有新窗口开始监听:" + sid + ", 当前在线人数为" + getOnlineCount());
        //try {
        //    //sendMessage("连接成功");
        //} catch (IOException e) {
        //    log.error("websocket IO异常");
        //}
    }

    @OnClose
    public void onClose() {
        webSocketMap.remove(this.session.getId());
        userIdSessionMap.remove(this.session.getId());
        subOnlineCount();
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        QuestionSubmitRequest req = JSONUtil.toBean(message, QuestionSubmitRequest.class);
        JudgeStatusVO judgeStatusVO = new JudgeStatusVO();
        RemoteEndpoint.Basic basicRemote = session.getBasicRemote();
        switch (req.getActivity()) {
            case "problem_run_code":
                try (Entry wsProblemRunCode = SphU.entry("ws_problem_run_code");) {
                    judgeStatusVO.setStatus(JudgeInfoMessageEnum.RUNNING.getText());
                    basicRemote.sendText(JSONUtil.toJsonStr(judgeStatusVO));
                    JudgeStatusVO res = judgeFeignClient.debug(req);
                    basicRemote.sendText(JSONUtil.toJsonStr(res));
                } catch (IOException e) {
                    log.error("执行代码出错");
                } catch (BlockException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "problem_submit_code":
                req.setUserId(userIdSessionMap.get(session.getId()));
                req.setSid(this.session.getId());
                questionFeignClient.doQuestionSubmit(req);
                break;
            case "heartbeat":
                try {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("activity", "heartbeat");
                    basicRemote.sendText(JSONUtil.toJsonStr(map));
                } catch (IOException e) {
                    log.error("执行代码出错");
                }
                break;
        }
        //session.getBasicRemote().sendText();
        //for (WebSocketServer item : webSocketSet) {
        //    try {
        //        item.sendMessage(message);
        //    } catch (IOException e) {
        //        e.printStackTrace();
        //    }
        //}
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    public static void sendMessageById(String message, String sid) throws IOException {
        //log.info("推送消息到窗口" +  + "，推送内容:" + message);
        webSocketMap.get(sid).session.getBasicRemote().sendText(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

}
