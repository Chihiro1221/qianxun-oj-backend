package com.qianxun.qianxunojbackendmodel.model.vo;

import com.qianxun.qianxunojbackendmodel.model.entity.JudgeStatus;
import com.qianxun.qianxunojbackendmodel.model.enums.JudgeInfoMessageEnum;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * 题目提交封装类
 *
 * @TableName question
 */
@Data
public class JudgeStatusVO implements Serializable {

    /**
     * 正确程序输出
     */
    private String stdout;


    /**
     * 用户程序输出
     */
    private String user_stdout;

    /**
     * 程序輸入
     */
    private String stdin;
    /**
     * 运行时间
     */
    private String time;

    /**
     * 运行内存
     */
    private String memory;

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
    private String status;

    private static final long serialVersionUID = 1L;

    /**
     * 包装类转对象
     *
     * @param questionVO
     * @return
     */
    //public static JudgeStatus voToObj(JudgeStatusVO judgeStatusVO) {
    //    if (judgeStatusVO == null) {
    //        return null;
    //    }
    //    JudgeStatus question = new JudgeStatus();
    //    BeanUtils.copyProperties(judgeStatusVO, JudgeStatus);
    //    judgeStatus.setStatus(judgeStatus.getStatus());
    //    List<String> tagList = judgeStatusVO.getTags();
    //    if (tagList != null) {
    //        question.setTags(JSONUtil.toJsonStr(tagList));
    //    }
    //    JudgeConfig voJudgeConfig = questionVO.getJudgeConfig();
    //    if (voJudgeConfig != null) {
    //        question.setJudgeConfig(JSONUtil.toJsonStr(voJudgeConfig));
    //    }
    //    return question;
    //}

    /**
     * 对象转包装类
     *
     * @param judgeStatus
     * @return
     */
    public static JudgeStatusVO objToVo(JudgeStatus judgeStatus) {
        if (judgeStatus == null) {
            return null;
        }
        JudgeStatusVO judgeStatusVO = new JudgeStatusVO();
        BeanUtils.copyProperties(judgeStatus, judgeStatusVO);
        if(judgeStatus.getStatus() == null) {
            judgeStatusVO.setStatus(JudgeInfoMessageEnum.COMPILE_ERROR.getText());
        } else {
            judgeStatusVO.setStatus(JudgeInfoMessageEnum.getEnumByValue(judgeStatus.getStatus().getDescription()).getText());
        }
        return judgeStatusVO;
    }
}

