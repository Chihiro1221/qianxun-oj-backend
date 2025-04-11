package com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionSubmitRequest implements Serializable {
    /**
     * websocket会话id
     */
    private String sid;

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 題目id
     */
    private Long question_id;
    /**
     * 用户代码
     */
    private String source_code;

    /**
     * 语言id
     */
    private Integer language_id;

    /**
     * 语言名称
     */
    private String language;

    /**
     * 输入用例
     */
    private String stdin;

    /**
     * 预期输出
     */
    private String expected_output;

    /**
     * 时间限制
     */
    private Float cpu_time_limit;

    /**
     * 内存限制
     */
    private Float memory_limit;

    /**
     * 堆栈限制
     */
    private Long stack_limit;

    /**
     * 用户操作
     */
    private String activity;

    private static final long serialVersionUID = 1L;
}
