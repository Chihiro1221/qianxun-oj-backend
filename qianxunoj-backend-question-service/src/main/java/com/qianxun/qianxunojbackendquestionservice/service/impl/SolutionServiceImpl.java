package com.qianxun.qianxunojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianxun.qianxunojbackendcommon.common.ErrorCode;
import com.qianxun.qianxunojbackendcommon.constant.CommonConstant;
import com.qianxun.qianxunojbackendcommon.constant.RedisConstant;
import com.qianxun.qianxunojbackendcommon.exception.BusinessException;
import com.qianxun.qianxunojbackendcommon.exception.ThrowUtils;
import com.qianxun.qianxunojbackendcommon.utils.SqlUtils;
import com.qianxun.qianxunojbackendmodel.model.dto.solution.SolutionQueryRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.Solution;
import com.qianxun.qianxunojbackendmodel.model.entity.User;
import com.qianxun.qianxunojbackendmodel.model.vo.SolutionVO;
import com.qianxun.qianxunojbackendquestionservice.mapper.SolutionMapper;
import com.qianxun.qianxunojbackendquestionservice.service.SolutionService;
import com.qianxun.qianxunojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author heart
 * @description 针对表【solution(题解)】的数据库操作Service实现
 * @createDate 2025-04-09 10:01:37
 */
@Service
public class SolutionServiceImpl extends ServiceImpl<SolutionMapper, Solution>
        implements SolutionService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 校验题目是否合法
     *
     * @param solution
     * @param add
     */
    @Override
    public void validSolution(Solution solution, boolean add) {
        if (solution == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = solution.getTitle();
        String content = solution.getContent();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (solution.getQuestionId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目id不能为空");
        }
    }

    @Override
    public QueryWrapper<Solution> getQueryWrapper(SolutionQueryRequest solutionQueryRequest) {
        QueryWrapper<Solution> queryWrapper = new QueryWrapper<>();
        if (solutionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = solutionQueryRequest.getId();
        String title = solutionQueryRequest.getTitle();
        String content = solutionQueryRequest.getContent();
        Long solutionId = solutionQueryRequest.getQuestionId();
        Long userId = solutionQueryRequest.getUserId();
        Long questionId = solutionQueryRequest.getQuestionId();
        String sortField = solutionQueryRequest.getSortField();
        String sortOrder = solutionQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "solutionId", solutionId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<SolutionVO> getSolutionVOPage(Page<Solution> solutionPage, HttpServletRequest request) {
        List<Solution> solutionList = solutionPage.getRecords();
        Page<SolutionVO> solutionVOPage = new Page<>(solutionPage.getCurrent(), solutionPage.getSize(), solutionPage.getTotal());
        if (CollectionUtils.isEmpty(solutionList)) {
            return solutionVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = solutionList.stream().map(Solution::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userFeignClient.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        List<SolutionVO> solutionVOList = solutionList.stream().map(solution -> {
            String viewCount = redisTemplate.opsForValue().get(RedisConstant.SOLUTION_VIEW_COUNT + solution.getId());
            String likeCount = redisTemplate.opsForValue().get(RedisConstant.SOLUTION_LIKE_COUNT + solution.getId());
            String favoriteCount = redisTemplate.opsForValue().get(RedisConstant.SOLUTION_FAVORITE_COUNT + solution.getId());
            SolutionVO solutionVO = SolutionVO.objToVo(solution);
            if (viewCount != null) {
                solutionVO.setViewCount(Long.parseLong(viewCount));
            }
            if (likeCount != null) {
                solutionVO.setUpvoteCount(Long.parseLong(likeCount));
            }
            if (favoriteCount != null) {
                solutionVO.setFavoriteCount(Long.parseLong(favoriteCount));
            }
            Long userId = solution.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            solutionVO.setUserVO(userFeignClient.getUserVO(user));
            return solutionVO;
        }).collect(Collectors.toList());
        solutionVOPage.setRecords(solutionVOList);
        return solutionVOPage;
    }

    /**
     * 点赞
     *
     * @param solutionQueryRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean like(SolutionQueryRequest solutionQueryRequest, User loginUser) {
        // 添加用户到点赞集合
        Long add = redisTemplate.opsForSet().add(RedisConstant.SOLUTION_LIKE_USERS + solutionQueryRequest.getId(), loginUser.getId().toString());
        if (add != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 增加点赞数量
        Long likes = (Long) redisTemplate.opsForValue().increment(RedisConstant.SOLUTION_LIKE_COUNT + solutionQueryRequest.getId().toString());
        if (likes != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return true;
    }

    /**
     * 取消点赞
     *
     * @param solutionQueryRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean unlike(SolutionQueryRequest solutionQueryRequest, User loginUser) {
        // 移除用户到点赞集合
        redisTemplate.opsForSet().remove(RedisConstant.SOLUTION_LIKE_USERS + solutionQueryRequest.getId(), loginUser.getId().toString());

        // 增加点赞数量
        Long likes = (Long) redisTemplate.opsForValue().decrement(RedisConstant.SOLUTION_LIKE_COUNT + solutionQueryRequest.getId().toString());
        if (likes != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return true;
    }

    /**
     * 是否点赞过该题解
     *
     * @param solutionId
     * @param loginUser
     * @return
     */
    @Override
    public Boolean isLiked(Long solutionId, User loginUser) {
        Boolean member = redisTemplate.opsForSet().isMember(RedisConstant.SOLUTION_LIKE_USERS + solutionId, loginUser.getId().toString());
        return member;
    }


    /**
     * 收藏
     *
     * @param solutionQueryRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean addFavorite(SolutionQueryRequest solutionQueryRequest, User loginUser) {
        double score = System.currentTimeMillis(); // 使用时间戳作为分数（排序用）
        Boolean isAdded = redisTemplate.opsForZSet().add(RedisConstant.USER_FAVORITE_SOLUTION + loginUser.getId(), solutionQueryRequest.getId().toString(), score);
        if (!isAdded) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        redisTemplate.opsForValue().increment(RedisConstant.SOLUTION_FAVORITE_COUNT + solutionQueryRequest.getId());

        return true;
    }

    /**
     * 取消收藏
     *
     * @param solutionQueryRequest
     * @param loginUser
     */
    @Override
    public Boolean unFavorite(SolutionQueryRequest solutionQueryRequest, User loginUser) {
        Long isRemoved = redisTemplate.opsForZSet().remove(RedisConstant.USER_FAVORITE_SOLUTION + loginUser.getId(), solutionQueryRequest.getId().toString());
        if (isRemoved != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        redisTemplate.opsForValue().decrement(RedisConstant.SOLUTION_FAVORITE_COUNT + solutionQueryRequest.getId());

        return true;
    }

    /**
     * 查询当前用户是否收藏
     *
     * @param solutionId
     * @param loginUser
     * @return
     */
    @Override
    public boolean isFavorite(Long solutionId, User loginUser) {
        return redisTemplate.opsForZSet().score(RedisConstant.USER_FAVORITE_SOLUTION + loginUser.getId(),
                solutionId.toString()) != null;
    }
    // 分页查询用户收藏（按时间倒序）
    //public List<Long> getFavorites(Long userId, int page, int size) {
    //    String key = String.format(USER_FAVORITES_KEY, userId);
    //    // ZREVRANGE: 按分数从高到低查询（时间倒序）
    //    Set<Object> ids = redisTemplate.opsForZSet().reverseRange(
    //            key, page * size, (page + 1) * size - 1
    //    );
    //    return ids.stream()
    //            .map(id -> Long.parseLong(id.toString()))
    //            .collect(Collectors.toList());
    //}
}




