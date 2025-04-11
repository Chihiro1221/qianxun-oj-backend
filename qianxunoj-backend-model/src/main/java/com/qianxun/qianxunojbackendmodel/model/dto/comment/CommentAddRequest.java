package com.qianxun.qianxunojbackendmodel.model.dto.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 新增评论
 *
 * @TableName comment
 */
@Data
public class CommentAddRequest implements Serializable {
    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论类型
     */
    private String commentableType;

    /**
     * 回复的评论id（顶级评论为：null）
     */
    private Long commentableId;

    /**
     * 目标 id
     */
    private Long targetId;

    /**
     * 回复的用户 id
     */
    private Long replyUserId;


    private static final long serialVersionUID = 1L;
}