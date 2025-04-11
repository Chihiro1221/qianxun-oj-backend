package com.qianxun.qianxunojbackendmodel.model.dto.solution;

import com.qianxun.qianxunojbackendmodel.model.dto.question.JudgeCase;
import com.qianxun.qianxunojbackendmodel.model.dto.question.JudgeConfig;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑请求
 *
 *   
 *  
 */
@Data
public class SolutionEditRequest implements Serializable {

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

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 题目难度
     */
    private Integer difficulty;

    /**
     * 判题用例
     */
    private List<JudgeCase> judgeCase;

    /**
     * 判题配置
     */
    private JudgeConfig judgeConfig;

    private static final long serialVersionUID = 1L;
}