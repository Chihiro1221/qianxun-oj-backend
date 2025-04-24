package com.qianxun.qianxunojbackendquestionservice.controller.inner;

import com.qianxun.qianxunojbackendcommon.common.ErrorCode;
import com.qianxun.qianxunojbackendcommon.exception.BusinessException;
import com.qianxun.qianxunojbackendmodel.model.dto.chat.ChatRequest;
import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.QuestionSubmitRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.Question;
import com.qianxun.qianxunojbackendmodel.model.entity.QuestionSubmit;
import com.qianxun.qianxunojbackendmodel.model.vo.TokenVO;
import com.qianxun.qianxunojbackendquestionservice.service.QuestionService;
import com.qianxun.qianxunojbackendquestionservice.service.QuestionSubmitService;
import com.qianxun.qianxunojbackendserviceclient.service.QuestionFeignClient;
import com.qianxun.qianxunojbackendserviceclient.service.UserFeignClient;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 该服务仅内部调用，不是给前端的
 */
@RestController
@RequestMapping("/inner")
public class QuestionInnerController implements QuestionFeignClient {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;
    @Resource
    private UserFeignClient userFeignClient;

    @GetMapping("/get/id")
    @Override
    public Question getQuestionById(@RequestParam("questionId") long questionId) {
        return questionService.getById(questionId);
    }

    @GetMapping("/question_submit/get/id")
    @Override
    public QuestionSubmit getQuestionSubmitById(@RequestParam("questionId") long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);
    }

    @PostMapping("/question_submit/update")
    @Override
    public boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit) {
        return questionSubmitService.updateById(questionSubmit);
    }

    @Override
    public void doQuestionSubmit(QuestionSubmitRequest questionSubmitRequest) {
        if (questionSubmitRequest == null || questionSubmitRequest.getQuestion_id() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<TokenVO> response = questionSubmitService.doQuestionSubmit(questionSubmitRequest);
    }

    @Override
    public void updateQuestionAcceptedNum(Long questionSubmitId) {
        questionSubmitService.updateQuestionAcceptedNum(questionSubmitId);
    }

    @Override
    public Flux<String> stream(ChatRequest chatRequest) {
        return null;
    }

}
