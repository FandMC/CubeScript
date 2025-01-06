package fandmc.cubescript.scripting;

import org.bukkit.command.CommandSender;

public class ScriptParser {

    /**
     * 解析并执行脚本内容
     */
    public static void parseAndExecute(String scriptContent, CommandSender sender) {
        String[] lines = scriptContent.split("\n");

        StringBuilder blockBuffer = new StringBuilder();
        boolean inCmBlock = false;
        boolean inMinecraftCommandBlock = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            // 1) 变量声明: 检测类似 "String xx = "val"" 或 "int xx = 123" 等
            if (isVariableDeclaration(line)) {
                // 直接交给 VariableParser 解析
                VariableParser.parse(line);
                continue;
            }

            // 2) 开始处理 cm: 块
            if (line.startsWith("cm:")) {
                // 如果之前正在收集某块，先把它提交
                if (inMinecraftCommandBlock) {
                    // 提交已收集的 Minecraft 命令块
                    MinecraftCommandParser.parse(blockBuffer.toString(), sender);
                    blockBuffer.setLength(0);
                    inMinecraftCommandBlock = false;
                }
                // 开启 cm: 块
                inCmBlock = true;
                blockBuffer.setLength(0);
                continue;
            }

            // 3) 开始处理 minecraft.command: 块
            if (line.startsWith("minecraft.command:")) {
                // 如果之前正在收集 cm: 块，先提交它
                if (inCmBlock) {
                    CmCommandParser.parse(blockBuffer.toString(), sender);
                    blockBuffer.setLength(0);
                    inCmBlock = false;
                }
                // 开启 minecraft.command: 块
                inMinecraftCommandBlock = true;
                blockBuffer.setLength(0);
                continue;
            }

            // 4) 如果正在 cm: 块内，并且当前行以 "- " 开头，则把它加入块缓存
            if (inCmBlock) {
                if (line.startsWith("- ")) {
                    blockBuffer.append(line).append("\n");
                    continue;
                } else {
                    // 当前行不再属于 cm: 块 => 提交 cm: 块
                    CmCommandParser.parse(blockBuffer.toString(), sender);
                    blockBuffer.setLength(0);
                    inCmBlock = false;
                    // 别忘了继续让当前行走正常逻辑（不能直接 continue）
                }
            }

            // 5) 如果正在 minecraft.command: 块内，并且当前行以 "- " 开头，则把它加入块缓存
            if (inMinecraftCommandBlock) {
                if (line.startsWith("- ")) {
                    blockBuffer.append(line).append("\n");
                    continue;
                } else {
                    // 当前行不再属于 minecraft.command: 块 => 提交
                    MinecraftCommandParser.parse(blockBuffer.toString(), sender);
                    blockBuffer.setLength(0);
                    inMinecraftCommandBlock = false;
                    // 同理不能直接 continue，要让当前行继续判断
                }
            }

            // 6) 检测 if / else if / else 块
            //    这里我们让 IfBlockParser 自己去处理接下来若干行，直到匹配到相应的 }
            if (line.startsWith("if (") && line.endsWith(") {")
                    || line.startsWith("else if (") && line.endsWith(") {")
                    || line.equals("else {"))
            {
                // 把后面的行都交给 IfBlockParser，让它找到匹配的大括号 } 后，再把解析位置返回
                i = IfBlockParser.parseIfElseBlock(lines, i, sender);
                // i 会变成 if 块或 else 块的末尾
                continue;
            }

            // 如果没有进入任何特殊块，就当作单行指令处理
            // （可扩展其他指令格式，如 "send.console(...)" 之类）
            handleSingleLine(line, sender);
        }

        // 循环结束后，如果还残留在某个块里，则要最后提交
        if (inCmBlock) {
            CmCommandParser.parse(blockBuffer.toString(), sender);
        }
        if (inMinecraftCommandBlock) {
            MinecraftCommandParser.parse(blockBuffer.toString(), sender);
        }
    }

    /**
     * 简易判断是否是变量声明
     */
    private static boolean isVariableDeclaration(String line) {
        // 很粗略：判断行首是不是 "String " / "int " / "boolean "
        return line.startsWith("String ")
                || line.startsWith("int ")
                || line.startsWith("boolean ");
    }

    /**
     * 处理不在 cm: 或 minecraft.command: 或 if 块中的其他行
     */
    private static void handleSingleLine(String line, CommandSender sender) {
        // 这里可以根据需求增加对 "send.console(...)" 或 "send.player(...)" 的检测
        // 也可以直接让它当 Minecraft 指令执行：
        line = VariableParser.replaceVariables(line);
        // 这里随便假设用 dispatchCommand
        boolean success = org.bukkit.Bukkit.dispatchCommand(sender, line);
        if (!success) {
            org.bukkit.Bukkit.getLogger().warning("[CubeScript] 命令执行失败: " + line);
        }
    }
}
