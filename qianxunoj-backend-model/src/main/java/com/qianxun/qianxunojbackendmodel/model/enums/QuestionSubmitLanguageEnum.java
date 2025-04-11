package com.qianxun.qianxunojbackendmodel.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目提交编程语言枚举
 */
public enum QuestionSubmitLanguageEnum {

    CPLUSPLUS(54, "cpp"),
    C(49, "c"),
    JAVA(62, "java"),
    PYTHON(70, "python"),
    PYTHON3(71, "python3"),
    JAVASCRIPT(63, "javascript"),
    TYPESCRIPT(74, "typescript"),
    RUBY(72, "ruby"),
    RUST(73, "rust"),
    GOLANG(60, "go");

    private final Integer id;

    private final String value;

    QuestionSubmitLanguageEnum(Integer id, String value) {
        this.id = id;
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
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static QuestionSubmitLanguageEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (QuestionSubmitLanguageEnum anEnum : QuestionSubmitLanguageEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public Integer getId() {
        return id;
    }
}
