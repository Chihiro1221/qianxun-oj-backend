package com.qianxun.qianxunojbackendcommon.constant;

/**
 * redis常量
 */
public interface RedisConstant {

    /**
     * 题解浏览量
     */
    String SOLUTION_VIEW_COUNT = "view:solution:";

    /**
     * 题解点赞用户id集合
     */
    String SOLUTION_LIKE_USERS = "solution:likes:users:";

    /**
     * 题解点赞量
     */
    String SOLUTION_LIKE_COUNT = "like:solution:";

    /**
     * 题解收藏量
     */
    String SOLUTION_FAVORITE_COUNT = "favorite:solution:";


    /**
     * 用户收藏题解id集合
     */
    String USER_FAVORITE_SOLUTION = "solution:favorites:";

}
