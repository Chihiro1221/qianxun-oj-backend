package com.yupi.yuojbackendmodel.model.dto.questionsubmit;

import com.yupi.yuojbackendmodel.model.vo.TokenVO;
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
