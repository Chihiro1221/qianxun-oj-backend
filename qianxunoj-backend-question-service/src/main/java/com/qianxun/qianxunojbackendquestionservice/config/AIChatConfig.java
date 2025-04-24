package com.qianxun.qianxunojbackendquestionservice.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring ai 配置
 */
@Configuration
public class AIChatConfig {

    //private static final String DEFAULT_PROMPT = "你是一个博学的、精通数据结构与算法的智能聊天助手，请根据以下题目描述回答用户的问题：\n{question_info}";
    private static final String DEFAULT_PROMPT = """
            你是一个严谨的编程题目助手，题目信息已经告诉你了，所以你必须遵守以下规则：
                1. 思考过程必须包裹在 <think>...</think> 标签中
                2. 最终答案写在标签外
                3. 按照给的题目信息回答用户的问题
               
                当前题目信息：
                {question_info}
            """;

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        return chatClientBuilder
                .defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new SimpleLoggerAdvisor()
                )
                .build();
    }
}