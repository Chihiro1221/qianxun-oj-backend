package com.qianxun.qianxunojbackendmodel.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 评论表
 *
 * @TableName comment
 */
@TableName(value = "comment")
@Data
public class Comment implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论类型
     */
    private String commentableType;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Comment other = (Comment) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
                && (this.getCommentableType() == null ? other.getCommentableType() == null : this.getCommentableType().equals(other.getCommentableType()))
                && (this.getUpvoteCount() == null ? other.getUpvoteCount() == null : this.getUpvoteCount().equals(other.getUpvoteCount()))
                && (this.getDownvoteCount() == null ? other.getDownvoteCount() == null : this.getDownvoteCount().equals(other.getDownvoteCount()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getCommentableId() == null ? other.getCommentableId() == null : this.getCommentableId().equals(other.getCommentableId()))
                && (this.getTargetId() == null ? other.getTargetId() == null : this.getTargetId().equals(other.getTargetId()))
                && (this.getReplyUserId() == null ? other.getReplyUserId() == null : this.getReplyUserId().equals(other.getReplyUserId()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
                && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getCommentableType() == null) ? 0 : getCommentableType().hashCode());
        result = prime * result + ((getUpvoteCount() == null) ? 0 : getUpvoteCount().hashCode());
        result = prime * result + ((getDownvoteCount() == null) ? 0 : getDownvoteCount().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getCommentableId() == null) ? 0 : getCommentableId().hashCode());
        result = prime * result + ((getTargetId() == null) ? 0 : getTargetId().hashCode());
        result = prime * result + ((getReplyUserId() == null) ? 0 : getReplyUserId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", content=").append(content);
        sb.append(", commentableType=").append(commentableType);
        sb.append(", upvoteCount=").append(upvoteCount);
        sb.append(", downvoteCount=").append(downvoteCount);
        sb.append(", userId=").append(userId);
        sb.append(", commentableId=").append(commentableId);
        sb.append(", targetId=").append(targetId);
        sb.append(", replyUserId=").append(replyUserId);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}