package com.qianxun.qianxunojbackendmodel.model.dto.questionsubmit;

import com.qianxun.qianxunojbackendmodel.model.vo.TokenVO;
import lombok.Data;

import java.util.List;

/**
 * 消息队列数据实体
 */
@Data
public class JudgeStatusRequest {
    private List<TokenVO> tokenVOList;
    private Long questionSubmitId;
    private String sid;
}
