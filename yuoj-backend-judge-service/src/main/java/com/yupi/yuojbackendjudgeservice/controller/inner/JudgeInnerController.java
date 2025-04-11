package com.yupi.yuojbackendjudgeservice.controller.inner;

import com.yupi.yuojbackendjudgeservice.judge.JudgeService;
import com.yupi.yuojbackendmodel.model.dto.questionsubmit.QuestionSubmitRequest;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import com.yupi.yuojbackendmodel.model.enums.QuestionSubmitLanguageEnum;
import com.yupi.yuojbackendmodel.model.vo.JudgeStatusVO;
import com.yupi.yuojbackendmodel.model.vo.TokenVO;
import com.yupi.yuojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
