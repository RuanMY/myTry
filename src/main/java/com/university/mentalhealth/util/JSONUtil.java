package com.university.mentalhealth.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONUtil {
    private static final Logger logger = Logger.getLogger(JSONUtil.class.getName());

    /**
     * 简单的JSON解析器 - 解析测评题目的JSON选项
     */
    public static List<Map<String, Object>> parseOptions(String jsonOptions) {
        List<Map<String, Object>> options = new ArrayList<>();

        try {
            // 简单的JSON解析逻辑，适用于固定格式的选项数组
            if (jsonOptions == null || jsonOptions.trim().isEmpty()) {
                return options;
            }

            // 移除空格和换行
            String cleanJson = jsonOptions.replaceAll("\\s+", "");

            // 匹配每个选项对象
            Pattern pattern = Pattern.compile("\\{\"text\":\"([^\"]+)\",\"value\":(\\d+)\\}");
            Matcher matcher = pattern.matcher(cleanJson);

            while (matcher.find()) {
                Map<String, Object> option = new HashMap<>();
                option.put("text", matcher.group(1));
                option.put("value", Integer.parseInt(matcher.group(2)));
                options.add(option);
            }

            logger.info("成功解析 " + options.size() + " 个选项");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "解析JSON选项失败: " + jsonOptions, e);
            // 返回默认选项作为后备
            return createDefaultOptions();
        }

        return options;
    }

    /**
     * 创建默认选项（后备方案）
     */
    private static List<Map<String, Object>> createDefaultOptions() {
        List<Map<String, Object>> defaultOptions = new ArrayList<>();

        String[] texts = {"完全没有", "有几天", "一半以上时间", "几乎每天"};

        for (int i = 0; i < texts.length; i++) {
            Map<String, Object> option = new HashMap<>();
            option.put("text", texts[i]);
            option.put("value", i);
            defaultOptions.add(option);
        }

        return defaultOptions;
    }

    /**
     * 将选项列表转换为JSON字符串
     */
    public static String toJson(List<Map<String, Object>> options) {
        if (options == null || options.isEmpty()) {
            return "[]";
        }

        StringBuilder jsonBuilder = new StringBuilder("[");
        for (int i = 0; i < options.size(); i++) {
            Map<String, Object> option = options.get(i);
            jsonBuilder.append("{\"text\":\"")
                    .append(option.get("text"))
                    .append("\",\"value\":")
                    .append(option.get("value"))
                    .append("}");

            if (i < options.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");

        return jsonBuilder.toString();
    }

    /**
     * 生成标准的测评选项JSON
     */
    public static String generateStandardOptions() {
        List<Map<String, Object>> options = new ArrayList<>();

        String[] texts = {"完全没有", "有几天", "一半以上时间", "几乎每天"};
        int[] values = {0, 1, 2, 3};

        for (int i = 0; i < texts.length; i++) {
            Map<String, Object> option = new HashMap<>();
            option.put("text", texts[i]);
            option.put("value", values[i]);
            options.add(option);
        }

        return toJson(options);
    }
}