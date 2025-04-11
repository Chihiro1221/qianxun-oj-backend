package com.yupi.yuojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuojbackendmodel.model.dto.solution.SolutionQueryRequest;
import com.yupi.yuojbackendmodel.model.entity.Solution;
import com.yupi.yuojbackendmodel.model.entity.User;
import com.yupi.yuojbackendmodel.model.vo.SolutionVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author heart
* @description 针对表【solution(题解)】的数据库操作Service
* @createDate 2025-04-09 10:01:37
*/
public interface SolutionService extends IService<Solution> {
    /**
     * 校验题目是否合法
     * @param solution
     * @param add
     */
    public void validSolution(Solution solution, boolean add);

    /**
     * 获取查询条件
     *
     * @param solutionQueryRequest
     * @return
     */
    QueryWrapper<Solution> getQueryWrapper(SolutionQueryRequest solutionQueryRequest);

    /**
     * 获取page包装类
     * @param solutionPage
     * @param request
     * @return
     */
    Page<SolutionVO> getSolutionVOPage(Page<Solution> solutionPage, HttpServletRequest request);


    /**
     * 点赞
     * @param solutionQueryRequest
     * @param loginUser
     * @return
     */
    Boolean like(SolutionQueryRequest solutionQueryRequest, User loginUser);

    /**
     * 取消点赞
     * @param solutionQueryRequest
     * @param loginUser
     * @return
     */
    Boolean unlike(SolutionQueryRequest solutionQueryRequest, User loginUser);

    /**
     * 是否点赞过该题解
     * @param solutionId
     * @param loginUser
     * @return
     */
    Boolean isLiked(Long solutionId, User loginUser);

    Boolean addFavorite(SolutionQueryRequest solutionQueryRequest, User loginUser);

    Boolean unFavorite(SolutionQueryRequest solutionQueryRequest, User loginUser);

    boolean isFavorite(Long solutionId, User loginUser);
}
