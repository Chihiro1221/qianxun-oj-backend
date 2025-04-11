package com.yupi.yuojbackendmodel.model.dto.comment;

import com.yupi.yuojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 目标id
     */
    private Long targetId;

    /**
     * 评论对象类型
     */
    private String commentableType;

    private static final long serialVersionUID = 1L;
}