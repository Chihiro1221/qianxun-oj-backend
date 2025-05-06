package com.qianxun.qianxunojbackendquestionservice.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianxun.qianxunojbackendcommon.common.ErrorCode;
import com.qianxun.qianxunojbackendcommon.constant.CommonConstant;
import com.qianxun.qianxunojbackendcommon.exception.BusinessException;
import com.qianxun.qianxunojbackendcommon.utils.SqlUtils;
import com.qianxun.qianxunojbackendmodel.model.dto.question.JudgeCase;
import com.qianxun.qianxunojbackendmodel.model.dto.question.JudgeConfig;
import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.JudgeStatusRequest;
import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit.QuestionSubmitRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.Question;
import com.qianxun.qianxunojbackendmodel.model.entity.QuestionSubmit;
import com.qianxun.qianxunojbackendmodel.model.entity.User;
import com.qianxun.qianxunojbackendmodel.model.enums.QuestionSubmitLanguageEnum;
import com.qianxun.qianxunojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.qianxun.qianxunojbackendmodel.model.vo.QuestionSubmitVO;
import com.qianxun.qianxunojbackendmodel.model.vo.TokenVO;
import com.qianxun.qianxunojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.qianxun.qianxunojbackendquestionservice.rabbitmq.MyMessageProducer;
import com.qianxun.qianxunojbackendquestionservice.service.QuestionService;
import com.qianxun.qianxunojbackendquestionservice.service.QuestionSubmitService;
import com.qianxun.qianxunojbackendserviceclient.service.JudgeFeignClient;
import com.qianxun.qianxunojbackendserviceclient.service.UserFeignClient;
import com.qianxun.qianxunojbackendserviceclient.service.WebsocketFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.RequestContextFilter;

import jakarta.annotation.Resource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 浩楠
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2023-08-07 20:58:53
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    @Lazy
    private JudgeFeignClient judgeFeignClient;

    @Resource
    private WebsocketFeignClient websocketFeignClient;

    @Resource
    private MyMessageProducer myMessageProducer;

    @Value("${remote-url}")
    private String remoteUrl;
    @Autowired
    private RequestContextFilter requestContextFilter;

    // 在QuestionService中新增原子操作方法
    public Boolean incrementSubmitNum(Long questionId) {
        LambdaUpdateWrapper<Question> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("submitNum = submitNum + 1")
                .eq(Question::getId, questionId);
        return questionService.update(updateWrapper);
    }

    // 在QuestionService中新增原子操作方法
    public Boolean incrementAcceptedNum(Long questionId) {
        LambdaUpdateWrapper<Question> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("acceptedNum = acceptedNum + 1")
                .eq(Question::getId, questionId);
        return questionService.update(updateWrapper);
    }

    /**
     * 提交题目
     *
     * @return
     */
    @Override
    @Transactional
    public List<TokenVO> doQuestionSubmit(QuestionSubmitRequest questionSubmitRequest) {
        // 校验编程语言是否合法
        String language = questionSubmitRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        long questionId = questionSubmitRequest.getQuestion_id();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 更新提交数量
        Boolean updated = this.incrementSubmitNum(questionId);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据更新失败");
        }
        // 是否已提交题目
        Long userId = questionSubmitRequest.getUserId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitRequest.getSource_code());
        questionSubmit.setLanguage(language);
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        // 將提交记录插入数据库
        Boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        Long questionSubmitId = questionSubmit.getId();
        // 构造代码沙箱请求参数（这块代码应该放到判题服务）
        String judgeCaseStr = question.getJudgeCase();
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        HashMap<String, List<QuestionSubmitRequest>> map = new HashMap<>();
        map.put("submissions", new ArrayList<>());
        judgeCaseList.forEach((JudgeCase caseItem) -> {
            QuestionSubmitRequest submitRequest = new QuestionSubmitRequest();
            submitRequest.setLanguage_id(languageEnum.getId());
            submitRequest.setSource_code(Base64.encode(questionSubmitRequest.getSource_code()));
            submitRequest.setStdin(Base64.encode(caseItem.getInput()));
            submitRequest.setExpected_output(Base64.encode(caseItem.getOutput()));
            submitRequest.setCpu_time_limit(Float.valueOf(Instant.ofEpochMilli(judgeConfig.getTimeLimit()).getEpochSecond()));
            submitRequest.setMemory_limit(Float.valueOf(judgeConfig.getMemoryLimit()));
            submitRequest.setStack_limit(judgeConfig.getStackLimit());
            map.get("submissions").add(submitRequest);
        });
        String url = remoteUrl + "submissions/batch?base64_encoded=true";
        String json = JSONUtil.toJsonStr(map);
        // 执行
        String responseStr = HttpUtil.createPost(url)
                .body(json)
                .execute()
                .body();
        List<TokenVO> list = JSONUtil.toList(responseStr, TokenVO.class);
        JudgeStatusRequest judgeStatusRequest = new JudgeStatusRequest();
        judgeStatusRequest.setTokenVOList(list);
        judgeStatusRequest.setQuestionSubmitId(questionSubmitId);
        judgeStatusRequest.setSid(questionSubmitRequest.getSid());
        // 发送消息
        myMessageProducer.sendMessage("code_exchange", "my_routingKey", JSONUtil.toJsonStr(judgeStatusRequest));
        return list;
    }


    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 不同）提交的代码
        long userId = loginUser.getId();
        // 处理脱敏
        if (userId != questionSubmit.getUserId() && !userFeignClient.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

    /**
     * 更新题目ac数量
     *
     * @param questionSubmitId
     */
    @Override
    public void updateQuestionAcceptedNum(Long questionSubmitId) {
        QuestionSubmit questionSubmit = this.getById(questionSubmitId);
        Long questionId = questionSubmit.getQuestionId();
        incrementAcceptedNum(questionId);
    }


}




