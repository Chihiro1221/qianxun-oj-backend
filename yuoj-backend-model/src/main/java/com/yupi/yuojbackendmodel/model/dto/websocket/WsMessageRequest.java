package com.yupi.yuojbackendmodel.model.dto.websocket;

import lombok.Data;

import java.io.Serializable;

@Data
public class WsMessageRequest implements Serializable {
    private String sid;
    private String message;
}
