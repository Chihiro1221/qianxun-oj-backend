package com.yupi.yuojbackendmodel.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yupi.yuojbackendmodel.model.enums.CommentableTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评论VO表
 */
@Data
public class CommentVO implements Serializable {
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 支持量
     */
    private Long upvoteCount;

    /**
     * 踩量
     */
    private Long downvoteCount;

    /**
     * 评论用户 id
     */
    private Long userId;

    /**
     * 评论用户VO
     */
    private UserVO userVO;

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

    /**
     * 回复的用户VO
     */
    private UserVO replyUserVO;

    /**
     * 所有回复自己的评论
     */
    private List<CommentVO> replies;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}