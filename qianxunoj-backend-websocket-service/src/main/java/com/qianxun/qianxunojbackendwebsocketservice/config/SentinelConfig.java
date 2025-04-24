package com.qianxun.qianxunojbackendwebsocketservice.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import java.util.Collections;

@Configuration
public class SentinelConfig {

    @PostConstruct  // Spring Bean 初始化后自动执行
    public void initSentinelRules() {
        // 创建规则
        FlowRule rule = new FlowRule();
        rule.setResource("ws_problem_run_code");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS); // 按 QPS 限流
        rule.setCount(100); // 每秒最多 100 次请求
        
        // 加载规则（覆盖旧规则）
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }
}