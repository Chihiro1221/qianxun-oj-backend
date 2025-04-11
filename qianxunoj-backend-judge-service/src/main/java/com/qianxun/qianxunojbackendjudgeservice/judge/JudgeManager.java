package com.qianxun.qianxunojbackendjudgeservice.judge;

import com.qianxun.qianxunojbackendjudgeservice.judge.strategy.DefaultJudgeStrategy;
import com.qianxun.qianxunojbackendjudgeservice.judge.strategy.JavaLanguageJudgeStrategy;
import com.qianxun.qianxunojbackendjudgeservice.judge.strategy.JudgeContext;
import com.qianxun.qianxunojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.qianxun.qianxunojbackendmodel.model.codesandbox.JudgeInfo;
import com.qianxun.qianxunojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
