package fandmc.cubescript.scripting;

import java.util.HashMap;
import java.util.Map;

public class VariableParser {

    /**
     * 全局变量存储表
     */
    private static final Map<String, Object> variables = new HashMap<>();

    /**
     * 解析一行类似 “String varName = "Hello"” 的声明，并存储到全局变量表
     */
    public static void parse(String declaration) {
        // 示例: "String varName = "Hello""
        String[] parts = declaration.split(" ", 3);
        if (parts.length < 3) {
            throw new IllegalArgumentException("变量声明格式不正确: " + declaration);
        }

        String type = parts[0];
        String name = parts[1];
        // 后半部分再 split 一下获取真正的值
        String valuePart = parts[2];
        if (!valuePart.contains("=")) {
            throw new IllegalArgumentException("变量声明缺少等号: " + declaration);
        }
        // 假设最简规则: "varName = ...."
        String value = valuePart.split("=", 2)[1].trim();

        switch (type) {
            case "String":
                // 去掉首尾引号
                variables.put(name, stripQuotes(value));
                break;
            case "int":
                variables.put(name, Integer.parseInt(value));
                break;
            case "boolean":
                variables.put(name, Boolean.parseBoolean(value));
                break;
            default:
                throw new IllegalArgumentException("不支持的变量类型: " + type);
        }
    }

    /**
     * 将文本中的所有 {变量名} 替换为对应的值（这里简化为直接把原字符串替换）
     * 或者使用更安全的方式，例如先判断是否真正是变量名
     */
    public static String replaceVariables(String text) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue().toString();
            // 这里直接将出现的 key 都替换为 val
            text = text.replace(key, val);
        }
        return text;
    }

    /**
     * 帮助方法：去掉 String 两边的引号
     */
    private static String stripQuotes(String raw) {
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
            return raw.substring(1, raw.length() - 1);
        }
        return raw;
    }
}
