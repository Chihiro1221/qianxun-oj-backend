package com.qianxun.qianxunojbackendmodel.model.dto.chat;

import lombok.Data;

/**
 * spring ai chat request
 *
 * @author heart
 */
@Data
public class ChatRequest {
    private Long userId;
    private Long questionId;
    private String message;
}
