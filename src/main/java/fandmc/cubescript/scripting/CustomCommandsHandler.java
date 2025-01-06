package fandmc.cubescript.scripting;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 用于统一识别和处理 "send.console(...)",
 * "send.player(...)", 以及把 "Files.xxx(...)" 转交给 CustomFileCommands
 */
public class CustomCommandsHandler {

    /**
     * 处理一行自定义命令。
     * 若识别并执行了该命令 => 返回 true，否则返回 false。
     */
    public static boolean handleCustomCmLine(String rawLine, CommandSender sender) {
        // 去掉末尾分号
        String line = rawLine.endsWith(";")
                ? rawLine.substring(0, rawLine.length() - 1).trim()
                : rawLine;

        // 做变量替换
        line = VariableParser.replaceVariables(line);

        // 1) send.console("...")
        if (line.startsWith("send.console(") && line.endsWith(")")) {
            String message = extractBetween(line, "send.console(", ")");
            if (message != null) {
                Bukkit.getLogger().info("[CubeScript Console] " + message);
            }
            return true;
        }

        // 2) send.player("...")
        if (line.startsWith("send.player(") && line.endsWith(")")) {
            String message = extractBetween(line, "send.player(", ")");
            if (message != null) {
                if (sender instanceof Player) {
                    ((Player) sender).sendMessage(message);
                } else {
                    Bukkit.getLogger().warning("[CubeScript] send.player(...) 命令只能由玩家执行。");
                }
            }
            return true;
        }

        // 3) Files.xxx(...) => 交给 CustomFileCommands 去处理
        if (line.startsWith("Files.")) {
            // 如果在里面成功匹配 => 返回 true
            boolean handled = CustomFileCommands.handleFileCommand(line, sender);
            return handled;
        }

        // 如果都不匹配 => 返回 false
        return false;
    }

    /**
     * 从形如  send.console("xxx")  中提取 "xxx"（不含引号）。
     */
    private static String extractBetween(String line, String prefix, String suffix) {
        if (!line.startsWith(prefix) || !line.endsWith(suffix)) {
            return null;
        }
        String raw = line.substring(prefix.length(), line.length() - suffix.length()).trim();
        // 如果带引号，就去掉最外层引号
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
            raw = raw.substring(1, raw.length() - 1);
        }
        return raw;
    }

}
