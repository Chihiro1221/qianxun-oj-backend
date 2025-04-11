package com.qianxun.qianxunojbackendserviceclient.service;


import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.QuestionSubmitRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.QuestionSubmit;
import com.qianxun.qianxunojbackendmodel.model.vo.JudgeStatusVO;
import com.qianxun.qianxunojbackendmodel.model.vo.TokenVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 判题服务
 */
@FeignClient(name = "qianxun-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {

    /**
     * 判题
     *
     * @param questionSubmitId
     * @return
     */
    @PostMapping("/do")
    QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId);

    /**
     * debug
     */
    @PostMapping("/debug")
    JudgeStatusVO debug(@RequestBody QuestionSubmitRequest questionSubmitRequest);

    /**
     * 获取执行结果
     * @param tokenVOList
     * @return
     */
    @PostMapping("/get/judgeResult")
    String getJudgeResult(List<TokenVO> tokenVOList);
}
