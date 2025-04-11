package com.qianxun.qianxunojbackendmodel.model.vo;

import com.qianxun.qianxunojbackendmodel.model.entity.JudgeStatus;
import lombok.Data;

import java.util.List;

@Data
public class SubmissionsVO {
    private List<JudgeStatus> submissions;
}
