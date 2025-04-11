package com.yupi.yuojbackendjudgeservice.controller;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/judge") // WebSocket 路径
public class JudgeWebSocketEndpoint {
    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessions.put(userId, session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 处理客户端消息（如判题结果推送）
    }

    @OnClose
    public void onClose(Session session) {
        sessions.values().remove(session);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    // 主动推送消息给客户端
    public static void sendMessage(String userId, String message) throws IOException {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(message);
        }
    }
}
