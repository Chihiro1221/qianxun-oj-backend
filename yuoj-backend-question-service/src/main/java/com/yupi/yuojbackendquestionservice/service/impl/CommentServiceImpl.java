package com.yupi.yuojbackendcommentservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuojbackendcommon.common.ErrorCode;
import com.yupi.yuojbackendcommon.constant.CommonConstant;
import com.yupi.yuojbackendcommon.exception.BusinessException;
import com.yupi.yuojbackendcommon.exception.ThrowUtils;
import com.yupi.yuojbackendcommon.utils.SqlUtils;
import com.yupi.yuojbackendmodel.model.dto.comment.CommentQueryRequest;
import com.yupi.yuojbackendmodel.model.entity.Comment;
import com.yupi.yuojbackendmodel.model.entity.Solution;
import com.yupi.yuojbackendmodel.model.entity.User;
import com.yupi.yuojbackendmodel.model.enums.CommentableTypeEnum;
import com.yupi.yuojbackendmodel.model.vo.CommentVO;
import com.yupi.yuojbackendquestionservice.mapper.CommentMapper;
import com.yupi.yuojbackendquestionservice.service.CommentService;
import com.yupi.yuojbackendquestionservice.service.SolutionService;
import com.yupi.yuojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author heart
 * @description 针对表【comment(评论表)】的数据库操作Service实现
 * @createDate 2025-04-10 21:42:10
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {
    @Resource
    private SolutionService solutionService;

    @Resource
    private UserFeignClient userFeignClient;

    /**
     * 校验评论是否合法
     *
     * @param comment
     * @param add
     */
    @Override
    public void validComment(Comment comment, boolean add) {
        if (comment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // Todo:对内容进行敏感性校验
        String content = comment.getContent();
        String commentableType = comment.getCommentableType();
        Long targetId = comment.getTargetId();
        Long commentableId = comment.getCommentableId();
        Long replyUserId = comment.getReplyUserId();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(content, commentableType), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(content) && content.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容过长");
        }
        CommentableTypeEnum enumByValue = CommentableTypeEnum.getEnumByValue(commentableType);
        if (enumByValue == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean hasExist = true;
        switch (enumByValue) {
            case SOLUTION:
                Solution solution = solutionService.getById(targetId);
                if (solution == null) {
                    hasExist = false;
                }
                break;
        }
        if (commentableId != null) {
            Comment byId = this.getById(commentableId);
            if (byId == null) {
                hasExist = false;
            }
        }
    }

    @Override
    public QueryWrapper<Comment> getQueryWrapper(CommentQueryRequest commentQueryRequest) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        if (commentQueryRequest == null) {
            return queryWrapper;
        }
        Long id = commentQueryRequest.getId();
        Long targetId = commentQueryRequest.getTargetId();
        Long userId = commentQueryRequest.getUserId();
        String sortField = commentQueryRequest.getSortField();
        String sortOrder = commentQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(targetId), "targetId", targetId);
        queryWrapper.isNull("commentableId");
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<CommentVO> getCommentVOPage(Page<Comment> commentPage, HttpServletRequest request) {
        List<Comment> records = commentPage.getRecords();
        Page<CommentVO> commentVOPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        if (CollectionUtils.isEmpty(records)) {
            return commentVOPage;
        }
        ArrayList<CommentVO> commentVOS = new ArrayList<>();
        for (Comment comment : records) {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            // 设置评论用户详细信息
            User user = userFeignClient.getById(comment.getUserId());
            commentVO.setUserVO(userFeignClient.getUserVO(user));
            // 查询所有子评论
            QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
            commentQueryWrapper.eq("commentableId", comment.getId());
            List<Comment> replies = this.list(commentQueryWrapper);
            ArrayList<CommentVO> subCommentVOS = new ArrayList<>();

            for (Comment reply : replies) {
                CommentVO subCommentVO = new CommentVO();
                BeanUtils.copyProperties(reply, subCommentVO);
                // 设置评论用户详细信息
                User subUser = userFeignClient.getById(reply.getUserId());
                User replyUser = userFeignClient.getById(reply.getReplyUserId());
                subCommentVO.setUserVO(userFeignClient.getUserVO(subUser));
                subCommentVO.setReplyUserVO(userFeignClient.getUserVO(replyUser));
                subCommentVOS.add(subCommentVO);
            }
            commentVO.setReplies(subCommentVOS);
            commentVOS.add(commentVO);
        }
        commentVOPage.setRecords(commentVOS);
        return commentVOPage;
    }
}




