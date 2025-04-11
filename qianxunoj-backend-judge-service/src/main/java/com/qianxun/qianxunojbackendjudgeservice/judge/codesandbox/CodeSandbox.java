package com.qianxun.qianxunojbackendjudgeservice.judge.codesandbox;

import com.qianxun.qianxunojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.qianxun.qianxunojbackendmodel.model.codesandbox.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
