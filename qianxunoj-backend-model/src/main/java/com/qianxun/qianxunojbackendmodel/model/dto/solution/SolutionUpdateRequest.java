package com.qianxun.qianxunojbackendmodel.model.dto.solution;

import com.qianxun.qianxunojbackendmodel.model.dto.question.JudgeCase;
import com.qianxun.qianxunojbackendmodel.model.dto.question.JudgeConfig;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新请求
 */
@Data
public class SolutionUpdateRequest implements Serializable {


    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;


    private static final long serialVersionUID = 1L;
}