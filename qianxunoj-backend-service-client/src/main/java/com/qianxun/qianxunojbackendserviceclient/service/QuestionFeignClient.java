package com.qianxun.qianxunojbackendserviceclient.service;


import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.QuestionSubmitRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.Question;
import com.qianxun.qianxunojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 李鱼皮
 * @description 针对表【question(题目)】的数据库操作Service
 * @createDate 2023-08-07 20:58:00
 */
@FeignClient(name = "qianxun-backend-question-service", path = "/api/question/inner")
public interface QuestionFeignClient {

    @GetMapping("/get/id")
    Question getQuestionById(@RequestParam("questionId") long questionId);

    @GetMapping("/question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam("questionId") long questionSubmitId);

    @PostMapping("/question_submit/update")
    boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);

    /**
     * 提交题目
     *
     * @return 提交记录的 id
     */
    @PostMapping("/question_submit/do")
    void doQuestionSubmit(@RequestBody QuestionSubmitRequest questionSubmitRequest);

    /**
     * 更新题目ac数量
     *
     * @return 提交记录的 id
     */
    @PostMapping("/question_submit/question/update")
    void updateQuestionAcceptedNum(@RequestBody Long questionSubmitId);
}
