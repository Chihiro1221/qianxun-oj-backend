package com.qianxun.qianxunojbackendjudgeservice.judge.codesandbox.impl;

import com.qianxun.qianxunojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.qianxun.qianxunojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.qianxun.qianxunojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.qianxun.qianxunojbackendmodel.model.codesandbox.JudgeInfo;
import com.qianxun.qianxunojbackendmodel.model.enums.JudgeInfoMessageEnum;
import com.qianxun.qianxunojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 示例代码沙箱（仅为了跑通业务流程）
 */
@Slf4j
public class ExampleCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("测试执行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100F);
        judgeInfo.setTime(100F);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }
}
