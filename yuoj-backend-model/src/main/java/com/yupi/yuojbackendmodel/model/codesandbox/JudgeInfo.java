package com.yupi.yuojbackendmodel.model.codesandbox;

import lombok.Data;

/**
 * 判题信息
 */
@Data
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 消耗内存（KB）
     */
    private Float memory;

    /**
     * 消耗时间（KB）
     */
    private Float time;

    /**
     * 通过用例数量
     */
    private Integer pass_num;

    /**
     * 总测试用例数量
     */
    private Integer total_case;
}
