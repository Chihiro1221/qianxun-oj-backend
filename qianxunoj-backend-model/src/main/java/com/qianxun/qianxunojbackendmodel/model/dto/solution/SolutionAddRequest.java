package com.qianxun.qianxunojbackendmodel.model.dto.solution;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 *   
 *  
 */
@Data
public class SolutionAddRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 题目 id
     */
    private Long questionId;


    private static final long serialVersionUID = 1L;
}