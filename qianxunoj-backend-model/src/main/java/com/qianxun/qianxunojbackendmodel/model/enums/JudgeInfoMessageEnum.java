package com.qianxun.qianxunojbackendmodel.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判题信息消息枚举
 */
public enum JudgeInfoMessageEnum {

    ACCEPTED("Accepted", "Accepted"),
    FINISHED("Finished", "Accepted"),
    RUNNING("Running", "Processing"),
    PENDING("Pending", "In Queue"),
    SYSTEM_ERROR("System Error", "Internal Error"),
    WRONG_ANSWER("Wrong Answer", "Wrong Answer"),
    COMPILE_ERROR("Compile Error", "Compilation Error"),
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded", "Runtime Error (NZEC)"),
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded", "Time Limit Exceeded"),
    PRESENTATION_ERROR("Presentation Error", "展示错误"),
    OUTPUT_LIMIT_EXCEEDED("Output Limit Exceeded", "输出溢出"),
    DANGEROUS_OPERATION("Dangerous Operation", "危险操作"),
    RUNTIME_ERROR("Runtime Error", "运行错误");

    private final String text;

    private final String value;

    JudgeInfoMessageEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 是否为运行或者队列状态
     *
     * @param status
     * @return
     */
    public static Boolean isRunningOrProcessing(String status) {
        return PENDING.getValue().equals(status) || RUNNING.getValue().equals(status);
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static JudgeInfoMessageEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (JudgeInfoMessageEnum anEnum : JudgeInfoMessageEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
