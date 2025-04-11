package com.qianxun.qianxunojbackendjudgeservice.judge;

import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.JudgeStatusRequest;
import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.QuestionSubmitRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.QuestionSubmit;
import com.qianxun.qianxunojbackendmodel.model.vo.JudgeStatusVO;
import com.qianxun.qianxunojbackendmodel.model.vo.TokenVO;

import java.util.List;

/**
 * 判题服务
 */
public interface JudgeService {

    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    QuestionSubmit doJudge(long questionSubmitId);

    /**
     * debug
     * @return
     */
    JudgeStatusVO debug(QuestionSubmitRequest questionSubmitRequest);

    /**
     * 获取执行结果
     * @param tokenVOList
     * @return
     */
    String getJudgeResult(List<TokenVO> tokenVOList);

    /**
     * 更新题目提交状态
     * @param judgeStatus
     */
    void updateJudgeStatus(JudgeStatusRequest judgeStatus);
}
