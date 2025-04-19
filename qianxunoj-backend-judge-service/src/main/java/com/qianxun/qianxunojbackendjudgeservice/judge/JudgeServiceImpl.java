package com.qianxun.qianxunojbackendjudgeservice.judge;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.qianxun.qianxunojbackendcommon.common.ErrorCode;
import com.qianxun.qianxunojbackendcommon.exception.BusinessException;
import com.qianxun.qianxunojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.qianxun.qianxunojbackendjudgeservice.judge.codesandbox.CodeSandboxFactory;
import com.qianxun.qianxunojbackendjudgeservice.judge.codesandbox.CodeSandboxProxy;
import com.qianxun.qianxunojbackendjudgeservice.judge.strategy.JudgeContext;
import com.qianxun.qianxunojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.qianxun.qianxunojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.qianxun.qianxunojbackendmodel.model.codesandbox.JudgeInfo;
import com.qianxun.qianxunojbackendmodel.model.dto.question.JudgeCase;
import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.JudgeStatusRequest;
import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.QuestionSubmitRequest;
import com.qianxun.qianxunojbackendmodel.model.dto.websocket.WsMessageRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.JudgeStatus;
import com.qianxun.qianxunojbackendmodel.model.entity.Question;
import com.qianxun.qianxunojbackendmodel.model.entity.QuestionSubmit;
import com.qianxun.qianxunojbackendmodel.model.enums.JudgeInfoMessageEnum;
import com.qianxun.qianxunojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.qianxun.qianxunojbackendmodel.model.vo.SubmissionsVO;
import com.qianxun.qianxunojbackendmodel.model.vo.JudgeStatusVO;
import com.qianxun.qianxunojbackendmodel.model.vo.TokenVO;
import com.qianxun.qianxunojbackendserviceclient.service.QuestionFeignClient;
import com.qianxun.qianxunojbackendserviceclient.service.WebsocketFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private JudgeManager judgeManager;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);
    private ConcurrentHashMap<String, String> userStatusMap = new ConcurrentHashMap<>(); // 用于存储每个websocket会话的执行状态

    @Value("${codesandbox.type:example}")
    private String type;


    @Value("${remote-url}")
    private String remoteUrl;
    @Autowired
    private WebsocketFeignClient websocketFeignClient;


    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1）传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2）如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3）更改判题（题目提交）的状态为 “判题中”，防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        // 4）调用沙箱，获取到执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();
        // 5）根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        // 6）修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionFeignClient.getQuestionSubmitById(questionId);
        return questionSubmitResult;
    }

    @Override
    public JudgeStatusVO debug(QuestionSubmitRequest questionSubmitRequest) {
        JudgeCase judgeCase = null;
        if (StrUtil.isEmpty(questionSubmitRequest.getStdin())) {
            Question questionById = questionFeignClient.getQuestionById(questionSubmitRequest.getQuestion_id());
            String judgeCaseStr = questionById.getJudgeCase();
            List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
            judgeCase = judgeCaseList.get(0);
            questionSubmitRequest.setStdin(judgeCase.getInput());
        }

        String url = remoteUrl + "submissions?wait=true";
        String json = JSONUtil.toJsonStr(questionSubmitRequest);
        String responseStr = null;
        try {
            responseStr = HttpUtil.createPost(url)
                    .body(json)
                    .execute()
                    .body();
        } catch (HttpException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        JudgeStatus result = JSONUtil.toBean(responseStr, JudgeStatus.class);
        if (result.getStatus() == null) {

        }
        JudgeStatusVO judgeStatusVO = JudgeStatusVO.objToVo(result);
        if (JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeStatusVO.getStatus()))
            judgeStatusVO.setStatus(JudgeInfoMessageEnum.FINISHED.getText());
        if (StrUtil.isEmpty(questionSubmitRequest.getStdin())) judgeStatusVO.setStdin(judgeCase.getInput());
        else judgeStatusVO.setStdin(questionSubmitRequest.getStdin());
        return judgeStatusVO;
    }

    @Override
    public String getJudgeResult(List<TokenVO> tokenVOList) {
        String url = remoteUrl + "/submissions/batch";
        String body = HttpUtil.createGet(url).execute().body();
        JudgeStatusVO result = JSONUtil.toBean(body, JudgeStatusVO.class);
        return "";
    }

    @Override
    public void updateJudgeStatus(JudgeStatusRequest judgeStatus) {
        String url = remoteUrl + "/submissions/batch?tokens=";
        for (TokenVO tokenVO : judgeStatus.getTokenVOList()) {
            url += tokenVO.getToken() + ",";
        }
        String body = HttpUtil.createGet(url).execute().body();
        WsMessageRequest wsMessageRequest = new WsMessageRequest();
        JudgeStatusVO judgeStatusVO = new JudgeStatusVO();
        wsMessageRequest.setSid(judgeStatus.getSid());
        SubmissionsVO submissions = JSONUtil.toBean(body, SubmissionsVO.class);
        List<JudgeStatus> submitResult = submissions.getSubmissions();
        String status = "";
        Float maxTime = 0F;
        Float maxMemory = 0F;
        Integer passNum = 0;
        String compile_output = "";
        String userStdout = "";

        if (submitResult == null) {
            status = JudgeInfoMessageEnum.COMPILE_ERROR.getText();
        }
        for (JudgeStatus judgeStatusItem : submitResult) {
            status = judgeStatusItem.getStatus().getDescription();
            if (!status.equals("Accepted")) {
                compile_output = judgeStatusItem.getCompile_output();
                userStdout = judgeStatusItem.getStdout();
                break;
            }
            ++passNum;
            if (judgeStatusItem.getTime() != null)
                maxTime = Math.max(maxTime, Float.valueOf(judgeStatusItem.getTime()));
            if (judgeStatusItem.getMemory() != null)
                maxMemory = Math.max(maxMemory, Float.valueOf(judgeStatusItem.getMemory()));
        }


        // 获取当前用户的状态
        String currentStatus = JudgeInfoMessageEnum.getEnumByValue(status).getText();
        String lastStatus = userStatusMap.getOrDefault(judgeStatus.getSid(), "");

        // 只有当状态变化时才发送更新
        if (!currentStatus.equals(lastStatus)) {
            if (!(currentStatus.equals("Pending") && lastStatus.equals("Running"))) {
                judgeStatusVO.setStatus(currentStatus);
                judgeStatusVO.setTime(maxTime.toString());
                judgeStatusVO.setMemory(maxMemory.toString());
                judgeStatusVO.setCompile_output(compile_output);
                judgeStatusVO.setUser_stdout(userStdout);
                wsMessageRequest.setMessage(JSONUtil.toJsonStr(judgeStatusVO));
                websocketFeignClient.sendMessageById(wsMessageRequest);
                userStatusMap.put(judgeStatus.getSid(), currentStatus);
            }
        }

        // 如果状态不是最终状态，继续轮询
        if (JudgeInfoMessageEnum.isRunningOrProcessing(status)) {
            scheduler.schedule(() -> updateJudgeStatus(judgeStatus), 50, TimeUnit.MILLISECONDS);
            return;
        }

        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setId(judgeStatus.getQuestionSubmitId());
        questionSubmit.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.getEnumByValue(status).getText());
        judgeInfo.setTime(maxTime * 1000);
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setPass_num(passNum);
        judgeInfo.setTotal_case(submitResult.size());
        // 更新题目ac数量
        if (judgeInfo.getMessage().equals(JudgeInfoMessageEnum.ACCEPTED.getText())) {
            questionFeignClient.updateQuestionAcceptedNum(judgeStatus.getQuestionSubmitId());
        }
        questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        questionFeignClient.updateQuestionSubmitById(questionSubmit);

        // 清除本次执行状态
        userStatusMap.remove(judgeStatus.getSid());
    }
}
