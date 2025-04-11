package com.qianxun.qianxunojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qianxun.qianxunojbackendmodel.model.dto.comment.CommentQueryRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.Comment;
import com.qianxun.qianxunojbackendmodel.model.vo.CommentVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author heart
 * @description 针对表【comment(评论表)】的数据库操作Service
 * @createDate 2025-04-10 21:42:10
 */
public interface CommentService extends IService<Comment> {

    /**
     * 校验新增/更新参数
     *
     * @param comment
     * @param add     是否为新增
     */
    void validComment(Comment comment, boolean add);

    /**
     * 获取查询条件
     *
     * @param commentQueryRequest
     * @return
     */
    QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest);

    /**
     * 分页获取题目封装
     *
     * @param commentPage
     * @param request
     * @return
     */
    Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request);

}
