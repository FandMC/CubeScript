package fandmc.cubescript.scripting;

import org.bukkit.command.CommandSender;

public class CmCommandParser {

    /**
     * 解析 cm: 块中收集到的行（用 - 开头）
     */
    public static void parse(String blockContent, CommandSender sender) {
        String[] lines = blockContent.split("\n");
        for (String line : lines) {
            String cmdLine = line.trim();
            if (!cmdLine.startsWith("- ")) {
                continue;
            }
            // 去掉 "- "
            cmdLine = cmdLine.substring(2).trim();

            // 尝试让自定义命令处理器处理。如果返回 false，说明没匹配到任何自定义命令
            if (!CustomCommandsHandler.handleCustomCmLine(cmdLine, sender)) {
                // 没匹配上 => 让 ScriptParser 再次解析（可能是 if(...) / cm: / minecraft.command: 等写法）
                ScriptParser.parseAndExecute(cmdLine, sender);
            }
        }
    }

}
