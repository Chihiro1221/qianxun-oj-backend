package com.qianxun.qianxunojbackendcommon.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;

import java.util.Map;

/**
 * judge0服务base64工具类
 */
public class Base64Utils {
    public static Map<String, Object> translateBase64Map(Object questionSubmitRequest) {
        // 将对象值进行base64编码
        Map<String, Object> beanMap = BeanUtil.beanToMap(questionSubmitRequest);
        beanMap.entrySet().stream().forEach(entry -> {
            Object value = entry.getValue();
            if (value != null && value instanceof String) {
                entry.setValue(Base64.encode((String) value));
            }
        });
        return beanMap;
    }

}
