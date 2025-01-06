package fandmc.cubescript.scripting;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class MinecraftCommandParser {

    /**
     * 解析 minecraft.command: 块中的行
     */
    public static void parse(String blockContent, CommandSender sender) {
        String[] lines = blockContent.split("\n");
        for (String line : lines) {
            String command = line.trim();
            if (!command.startsWith("- ")) {
                continue;
            }
            // 去掉 "- "
            command = command.substring(2).trim();
            // 变量替换
            command = VariableParser.replaceVariables(command);

            boolean success = Bukkit.dispatchCommand(sender, command);
            if (!success) {
                Bukkit.getLogger().warning("[CubeScript] 命令执行失败: " + command);
            }
        }
    }
}
