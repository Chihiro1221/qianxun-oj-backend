package com.qianxun.qianxunojbackendmodel.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.qianxun.qianxunojbackendmodel.model.entity.Solution;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 题解
 * @TableName solution
 */
@Data
public class SolutionVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 支持量
     */
    private Long upvoteCount;

    /**
     * 踩量
     */
    private Long downvoteCount;

    /**
     * 收藏量
     */
    private Long favoriteCount;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建用户信息
     */
    private UserVO userVO;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 对象转包装类
     *
     * @param solution
     * @return
     */
    public static SolutionVO objToVo(Solution solution) {
        if (solution == null) {
            return null;
        }
        SolutionVO solutionVO = new SolutionVO();
        BeanUtils.copyProperties(solution, solutionVO);
        solutionVO.setUpvoteCount(solutionVO.getUpvoteCount() - solutionVO.getDownvoteCount());
        return solutionVO;
    }


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}