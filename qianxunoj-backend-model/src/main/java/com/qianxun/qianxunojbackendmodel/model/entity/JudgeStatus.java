package com.qianxun.qianxunojbackendmodel.model.entity;

import lombok.Data;

@Data
public class JudgeStatus {
    /**
     * 程序输出
     */
    private String stdout;

    /**
     * 运行时间
     */
    private String time;

    /**
     * 运行内存
     */
    private Long memory;

    /**
     * 编译输出
     */
    private String compile_output;


    /**
     * 消息
     */
    private String message;
    /**
     * 状态
     */
    private Status status;

    private static final long serialVersionUID = 1L;
}
