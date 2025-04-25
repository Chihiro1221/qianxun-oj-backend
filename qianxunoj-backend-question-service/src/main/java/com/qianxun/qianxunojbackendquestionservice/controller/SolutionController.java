package com.qianxun.qianxunojbackendquestionservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.qianxun.qianxunojbackendcommon.annotation.AuthCheck;
import com.qianxun.qianxunojbackendcommon.common.BaseResponse;
import com.qianxun.qianxunojbackendcommon.common.DeleteRequest;
import com.qianxun.qianxunojbackendcommon.common.ErrorCode;
import com.qianxun.qianxunojbackendcommon.common.ResultUtils;
import com.qianxun.qianxunojbackendcommon.constant.RedisConstant;
import com.qianxun.qianxunojbackendcommon.constant.UserConstant;
import com.qianxun.qianxunojbackendcommon.exception.BusinessException;
import com.qianxun.qianxunojbackendcommon.exception.ThrowUtils;
import com.qianxun.qianxunojbackendmodel.model.dto.question.*;
import com.qianxun.qianxunojbackendmodel.model.dto.solution.SolutionAddRequest;
import com.qianxun.qianxunojbackendmodel.model.dto.solution.SolutionQueryRequest;
import com.qianxun.qianxunojbackendmodel.model.dto.solution.SolutionUpdateRequest;
import com.qianxun.qianxunojbackendmodel.model.entity.Question;
import com.qianxun.qianxunojbackendmodel.model.entity.QuestionSubmit;
import com.qianxun.qianxunojbackendmodel.model.entity.Solution;
import com.qianxun.qianxunojbackendmodel.model.entity.User;
import com.qianxun.qianxunojbackendmodel.model.vo.QuestionVO;
import com.qianxun.qianxunojbackendmodel.model.vo.SolutionVO;
import com.qianxun.qianxunojbackendmodel.model.vo.UserVO;
import com.qianxun.qianxunojbackendquestionservice.service.QuestionService;
import com.qianxun.qianxunojbackendquestionservice.service.QuestionSubmitService;
import com.qianxun.qianxunojbackendquestionservice.service.SolutionService;
import com.qianxun.qianxunojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 题解接口
 */
@RestController
@RequestMapping("/solution")
@Slf4j
public class SolutionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private SolutionService solutionService;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private QuestionSubmitService questionSubmitService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param solutionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSolution(@RequestBody SolutionAddRequest solutionAddRequest, HttpServletRequest request) {
        if (solutionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Solution solution = new Solution();
        BeanUtils.copyProperties(solutionAddRequest, solution);
        solutionService.validSolution(solution, true);
        User loginUser = userFeignClient.getLoginUser(request);
        solution.setUserId(loginUser.getId());
        boolean result = solutionService.save(solution);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newSolutionId = solution.getId();
        return ResultUtils.success(newSolutionId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSolution(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userFeignClient.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Solution oldSolution = solutionService.getById(id);
        ThrowUtils.throwIf(oldSolution == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldSolution.getUserId().equals(user.getId()) && !userFeignClient.isAdmin(user)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = solutionService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 根据 id 获取（脱敏）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<SolutionVO> getSolutionVOById(@RequestParam("id") long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Solution solution = solutionService.getById(id);
        if (solution == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        SolutionVO solutionVO = SolutionVO.objToVo(solution);
        // 1. 关联查询用户信息
        Long userId = solution.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userFeignClient.getById(userId);
        }

        String viewCount = redisTemplate.opsForValue().get(RedisConstant.SOLUTION_VIEW_COUNT + solution.getId());
        String likeCount = redisTemplate.opsForValue().get(RedisConstant.SOLUTION_LIKE_COUNT + solution.getId());
        String favoriteCount = redisTemplate.opsForValue().get(RedisConstant.SOLUTION_FAVORITE_COUNT + solution.getId());
        if (viewCount != null) {
            solutionVO.setViewCount(Long.parseLong(viewCount));
        }
        if (likeCount != null) {
            solutionVO.setUpvoteCount(Long.parseLong(likeCount));
        }
        if (favoriteCount != null) {
            solutionVO.setFavoriteCount(Long.parseLong(favoriteCount));
        }
        UserVO userVO = userFeignClient.getUserVO(user);
        solutionVO.setUserVO(userVO);

        return ResultUtils.success(solutionVO);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param solutionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SolutionVO>> listSolutionVOByPage(@RequestBody SolutionQueryRequest solutionQueryRequest, HttpServletRequest request) {
        long current = solutionQueryRequest.getCurrent();
        long size = solutionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Solution> solutionPage = solutionService.page(new Page<>(current, size), solutionService.getQueryWrapper(solutionQueryRequest));
        return ResultUtils.success(solutionService.getSolutionVOPage(solutionPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param solutionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<SolutionVO>> listMySolutionVOByPage(@RequestBody SolutionQueryRequest solutionQueryRequest, HttpServletRequest request) {
        if (solutionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);
        solutionQueryRequest.setUserId(loginUser.getId());
        long current = solutionQueryRequest.getCurrent();
        long size = solutionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Solution> solutionPage = solutionService.page(new Page<>(current, size), solutionService.getQueryWrapper(solutionQueryRequest));
        // 关联查询题目信息
        Page<SolutionVO> solutionVOPage = solutionService.getSolutionVOPage(solutionPage, request);
        List<SolutionVO> records = solutionVOPage.getRecords();
        records.forEach(solutionVO -> {
            QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
            questionQueryWrapper.select("title");
            questionQueryWrapper.eq("id", solutionVO.getQuestionId());
            Question question = questionService.getOne(questionQueryWrapper);
            solutionVO.setQuestionVO(QuestionVO.objToVo(question));
        });
        solutionVOPage.setRecords(records);
        return ResultUtils.success(solutionVOPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param solutionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/favorite/page/vo")
    public BaseResponse<Page<SolutionVO>> listFavoriteSolutionVOByPage(@RequestBody SolutionQueryRequest solutionQueryRequest, HttpServletRequest request) {
        if (solutionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);
        solutionQueryRequest.setUserId(loginUser.getId());
        long current = solutionQueryRequest.getCurrent();
        long size = solutionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        List<SolutionVO> currentFavorites = solutionService.getCurrentFavorites(solutionQueryRequest, loginUser);
        Long total = redisTemplate.opsForZSet().zCard(RedisConstant.USER_FAVORITE_SOLUTION + loginUser.getId());
        Page<SolutionVO> solutionVOPage = new Page<>(current, size, total);
        solutionVOPage.setRecords(currentFavorites);

        return ResultUtils.success(solutionVOPage);
    }

    // endregion

    /**
     * 增加浏览量
     *
     * @param solutionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/view_count/add")
    public BaseResponse<Boolean> updateViewCount(@RequestBody SolutionQueryRequest solutionQueryRequest, HttpServletRequest request) {
        if (solutionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long increment = redisTemplate.opsForValue().increment(RedisConstant.SOLUTION_VIEW_COUNT + solutionQueryRequest.getId());
        return ResultUtils.success(increment == 1);
    }

    /**
     * 点赞
     *
     * @param solutionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/like")
    public BaseResponse<Boolean> like(@RequestBody SolutionQueryRequest solutionQueryRequest, HttpServletRequest request) {
        if (solutionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);

        return ResultUtils.success(solutionService.like(solutionQueryRequest, loginUser));
    }

    /**
     * 取消点赞
     *
     * @param solutionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/unlike")
    public BaseResponse<Boolean> unlike(@RequestBody SolutionQueryRequest solutionQueryRequest, HttpServletRequest request) {
        if (solutionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);

        return ResultUtils.success(solutionService.unlike(solutionQueryRequest, loginUser));
    }

    /**
     * 当前用户是否点赞过该题解
     *
     * @param solutionId
     * @param request
     * @return
     */
    @GetMapping("/upvote_count/my/{solutionId}")
    public BaseResponse<Boolean> isLiked(@PathVariable("solutionId") Long solutionId, HttpServletRequest request) {
        if (solutionId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);

        return ResultUtils.success(solutionService.isLiked(solutionId, loginUser));
    }

    /**
     * 收藏
     *
     * @param solutionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/favorite")
    public BaseResponse<Boolean> addFavorite(@RequestBody SolutionQueryRequest solutionQueryRequest, HttpServletRequest request) {
        if (solutionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);

        return ResultUtils.success(solutionService.addFavorite(solutionQueryRequest, loginUser));
    }

    /**
     * 取消收藏
     *
     * @param solutionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/unFavorite")
    public BaseResponse<Boolean> unFavorite(@RequestBody SolutionQueryRequest solutionQueryRequest, HttpServletRequest request) {
        if (solutionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);

        return ResultUtils.success(solutionService.unFavorite(solutionQueryRequest, loginUser));
    }

    /**
     * 当前用户是否收藏过该题解
     *
     * @param solutionId
     * @param request
     * @return
     */
    @GetMapping("/favorite_count/my/{solutionId}")
    public BaseResponse<Boolean> isFavorite(@PathVariable("solutionId") Long solutionId, HttpServletRequest request) {
        if (solutionId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);

        return ResultUtils.success(solutionService.isFavorite(solutionId, loginUser));
    }

    /**
     * 更新题解
     *
     * @param solutionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateSolution(@RequestBody SolutionUpdateRequest solutionUpdateRequest) {
        if (solutionUpdateRequest == null || solutionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Solution solution = new Solution();
        BeanUtils.copyProperties(solutionUpdateRequest, solution);
        // 参数校验
        solutionService.validSolution(solution, false);
        long id = solutionUpdateRequest.getId();
        // 判断是否存在
        Solution oldSolution = solutionService.getById(id);
        ThrowUtils.throwIf(oldSolution == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = solutionService.updateById(solution);
        return ResultUtils.success(result);
    }
}
