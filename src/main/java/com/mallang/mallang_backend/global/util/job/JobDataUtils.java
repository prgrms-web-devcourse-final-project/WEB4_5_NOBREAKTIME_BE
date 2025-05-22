package com.mallang.mallang_backend.global.util.job;

import org.quartz.JobDataMap;

public class JobDataUtils {
    public static int getIntValue(JobDataMap dataMap, String key, int defaultValue) {
        Object value = dataMap.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}