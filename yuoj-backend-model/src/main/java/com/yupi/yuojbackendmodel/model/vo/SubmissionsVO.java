package com.yupi.yuojbackendmodel.model.vo;

import com.yupi.yuojbackendmodel.model.entity.JudgeStatus;
import lombok.Data;

import java.util.List;

@Data
public class SubmissionsVO {
    private List<JudgeStatus> submissions;
}
