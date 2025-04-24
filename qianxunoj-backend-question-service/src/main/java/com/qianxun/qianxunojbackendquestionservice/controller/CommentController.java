package com.qianxun.qianxunojbackendquestionservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianxun.qianxunojbackendcommon.annotation.AuthCheck;
import com.qianxun.qianxunojbackendcommon.common.BaseResponse;
import com.qianxun.qianxunojbackendcommon.common.DeleteRequest;
import com.qianxun.qianxunojbackendcommon.common.ErrorCode;
import com.qianxun.qianxunojbackendcommon.common.ResultUtils;
import com.qianxun.qianxunojbackendcommon.constant.UserConstant;
import com.qianxun.qianxunojbackendcommon.exception.BusinessException;
import com.qianxun.qianxunojbackendcommon.exception.ThrowUtils;
import com.qianxun.qianxunojbackendmodel.model.dto.comment.CommentAddRequest;
import com.qianxun.qianxunojbackendmodel.model.dto.comment.CommentQueryRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.Comment;
import com.qianxun.qianxunojbackendmodel.model.entity.User;
import com.qianxun.qianxunojbackendmodel.model.vo.CommentVO;
import com.qianxun.qianxunojbackendquestionservice.service.CommentService;
import com.qianxun.qianxunojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private UserFeignClient userFeignClient;

    // region 增删改查

    /**
     * 新增评论
     *
     * @param commentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addComment(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentAddRequest, comment);
        commentService.validComment(comment, true);
        User loginUser = userFeignClient.getLoginUser(request);
        comment.setUserId(loginUser.getId());
        boolean result = commentService.save(comment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newCommentId = comment.getId();
        return ResultUtils.success(newCommentId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userFeignClient.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Comment oldComment = commentService.getById(id);
        ThrowUtils.throwIf(oldComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldComment.getUserId().equals(user.getId()) && !userFeignClient.isAdmin(user)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean res = commentService.removeById(id);
        if (res) {
            QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
            commentQueryWrapper.eq("commentableId", deleteRequest.getId());
            commentService.remove(commentQueryWrapper);
        }
        return ResultUtils.success(res);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param commentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentQueryRequest commentQueryRequest,
                                                             HttpServletRequest request) {
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 获取顶级评论
        Page<Comment> commentPage = commentService.page(new Page<>(current, size),
                commentService.getQueryWrapper(commentQueryRequest));
        return ResultUtils.success(commentService.getCommentVOPage(commentPage, request));
    }

    // endregion


}
