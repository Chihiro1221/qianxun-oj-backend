package com.qianxun.qianxunojbackendmodel.model.dto.solution;

import com.qianxun.qianxunojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 *   
 *  
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SolutionActionRequest extends PageRequest implements Serializable {

    private String action;

    private static final long serialVersionUID = 1L;
}