package com.qianxun.qianxunojbackendquestionservice.task;

import com.qianxun.qianxunojbackendcommon.constant.RedisConstant;
import com.qianxun.qianxunojbackendmodel.model.entity.Solution;
import com.qianxun.qianxunojbackendquestionservice.service.SolutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.List;

@Configuration
@EnableScheduling
public class DataSyncTask {

    private static final Logger log = LoggerFactory.getLogger(DataSyncTask.class);
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private SolutionService solutionService;


    @Scheduled(cron = "0 0 0 * * ?") // 每天0点同步
    //@Scheduled(cron = "0 * * * * ?")
    public void syncLikesAndViewCounts() {
        log.info("定时任务启动");
        List<Solution> list = solutionService.list();
        for (Solution solution : list) {
            // 同步点赞量
            String likes = redisTemplate.opsForValue().get(RedisConstant.SOLUTION_LIKE_COUNT + solution.getId());
            if (likes != null) {
                solution.setUpvoteCount(Long.parseLong(likes));
            }
            // 定时同步浏览量
            String viewCount = redisTemplate.opsForValue().get(RedisConstant.SOLUTION_VIEW_COUNT + solution.getId());
            if (viewCount != null) {
                solution.setViewCount(Long.parseLong(viewCount));
            }
            // 定时同步收藏量
            String favoriteCount = redisTemplate.opsForValue().get(RedisConstant.SOLUTION_FAVORITE_COUNT + solution.getId());
            if (favoriteCount != null) {
                solution.setFavoriteCount(Long.parseLong(favoriteCount));
            }
            System.out.println("更新单条：" + likes + "->" + viewCount + "->" + favoriteCount);
            solutionService.updateById(solution);
        }
    }
}