package fandmc.cubescript.scripting;

import org.bukkit.command.CommandSender;

/**
 * 负责从一行 if(...) { 的位置开始，收集 if / else if / else 中的大括号块。
 * 逻辑：
 * - 找到与 { } 配对的代码块，把其中行交给 ScriptParser.parseAndExecute(...) 执行
 * - 支持多段 else if(...) { } 与 else { }
 * - 处理完以后返回结束位置（行号），以便主循环能继续
 */
public class IfBlockParser {

    /**
     * 从 lines[curLine] 开始解析 if / else if / else 结构，返回最后处理的位置下标
     */
    public static int parseIfElseBlock(String[] lines, int curLine, CommandSender sender) {
        boolean conditionMet = false;    // 当前 if/else if 的条件是否满足
        boolean blockExecuted = false;   // 是否已经有一个块成功执行过

        for (int i = curLine; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            // 匹配 if(...) { / else if(...) { / else {
            if ((line.startsWith("if (") && line.endsWith(") {"))
                    || (line.startsWith("else if (") && line.endsWith(") {"))
                    || line.equals("else {"))
            {
                // if(...) 或 else if(...)
                if (line.startsWith("if (")) {
                    // 还没有任何块执行过，我们才去判断
                    if (!blockExecuted) {
                        String condition = extractCondition(line, "if");
                        conditionMet = ConditionEvaluator.evaluate(condition, sender);
                    } else {
                        conditionMet = false;
                    }
                }
                else if (line.startsWith("else if (")) {
                    if (!blockExecuted) {
                        String condition = extractCondition(line, "else if");
                        conditionMet = ConditionEvaluator.evaluate(condition, sender);
                    } else {
                        conditionMet = false;
                    }
                }
                // else {
                else {
                    // 如果还没有块执行过，则 else 就算满足
                    conditionMet = !blockExecuted;
                }

                // 解析花括号里的内容
                int blockStart = i + 1;
                int blockEnd = findBlockEnd(lines, blockStart);
                if (blockEnd < 0) {
                    throw new IllegalStateException("if/else 块缺少匹配的 '}'");
                }

                if (conditionMet) {
                    // 把块中的内容交给 ScriptParser 解析执行
                    String blockContent = buildBlockContent(lines, blockStart, blockEnd);
                    ScriptParser.parseAndExecute(blockContent, sender);
                    blockExecuted = true;
                }

                // i 跳到 blockEnd 之后
                i = blockEnd;

                // 如果后面还有 else if(...) 或 else { ... }，会在下一次循环继续判断
                // 所以不能直接 break，除非你想一次只支持一个 if-else

                // 继续走 for 循环
            }
            else if (line.startsWith("}")) {
                // 理论上不会走到这里，因为 findBlockEnd 会把 i 移动过去
                // 如果来了，就直接返回
                return i;
            }
            else {
                // 不是 if / else if / else 或者 }，说明已经走出该结构了
                // 表示整个 if-else 结构解析完成
                return i - 1;
            }
        }
        // 如果 for 正常结束，说明所有行都读完了
        return lines.length - 1;
    }

    /**
     * 提取 if(...) / else if(...) 中小括号内的内容
     */
    private static String extractCondition(String line, String prefix) {
        // e.g. "if (Player.name == "Steve") {"
        // 去掉前缀 + 空格，如 "if " => 然后取 "(" 和 ")"
        int start = line.indexOf('(');
        int end = line.lastIndexOf(')');
        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalArgumentException("if/else if 语句格式错误: " + line);
        }
        return line.substring(start + 1, end).trim();
    }

    /**
     * 找到与 { 对应的 }
     */
    private static int findBlockEnd(String[] lines, int startIndex) {
        int braceCount = 1; // 已经遇到一个 {
        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i];
            // 统计大括号配对：如果出现新的 { 就 +1，出现 } 就 -1
            if (line.contains("{")) {
                braceCount++;
            }
            if (line.contains("}")) {
                braceCount--;
                if (braceCount == 0) {
                    return i; // 返回匹配到 } 的行
                }
            }
        }
        return -1; // 未找到对应的 }
    }

    /**
     * 从 start 到 end（含）把行拼接起来作为 if/else 块内容
     */
    private static String buildBlockContent(String[] lines, int start, int end) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            sb.append(lines[i]).append("\n");
        }
        return sb.toString();
    }
}
