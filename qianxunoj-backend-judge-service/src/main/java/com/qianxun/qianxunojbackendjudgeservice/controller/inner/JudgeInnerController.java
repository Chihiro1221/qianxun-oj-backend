package com.qianxun.qianxunojbackendjudgeservice.controller.inner;

import com.qianxun.qianxunojbackendjudgeservice.judge.JudgeService;
import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.QuestionSubmitRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.QuestionSubmit;
import com.qianxun.qianxunojbackendmodel.model.enums.QuestionSubmitLanguageEnum;
import com.qianxun.qianxunojbackendmodel.model.vo.JudgeStatusVO;
import com.qianxun.qianxunojbackendmodel.model.vo.TokenVO;
import com.qianxun.qianxunojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {

    @Resource
    private JudgeService judgeService;

    /**
     * 判题
     *
     * @param questionSubmitId
     * @return
     */
    @Override
    public QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }

    /**
     * 调试代码
     *
     * @param questionSubmitRequest
     * @return
     */
    @Override
    @PostMapping("/debug")
    public JudgeStatusVO debug(QuestionSubmitRequest questionSubmitRequest) {
        questionSubmitRequest.setLanguage_id(QuestionSubmitLanguageEnum.getEnumByValue(questionSubmitRequest.getLanguage()).getId());
        return judgeService.debug(questionSubmitRequest);
    }

    /**
     * 查询执行结果
     *
     * @return
     */
    @Override
    @PostMapping("/get/judgeResult")
    public String getJudgeResult(List<TokenVO> tokenVOList) {
        return judgeService.getJudgeResult(tokenVOList);
    }

    /**
     * 查询执行结果
     *
     * @return
     */
    @PostMapping("/get/test")
    public void test(@RequestBody Object obj) {
        System.out.println(obj);
    }

}
