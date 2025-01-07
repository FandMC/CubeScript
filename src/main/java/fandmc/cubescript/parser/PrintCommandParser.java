package fandmc.cubescript.parser;

import fandmc.cubescript.utils.ColorUtil;
import fandmc.cubescript.utils.VariableManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 专门用于解析 print 命令
 *
 * 支持:
 *   1) print.console("内容");
 *   2) print.player.all("内容");
 *   3) print.player["玩家名"]("内容");
 */
public class PrintCommandParser {
    private final VariableManager varManager;

    public PrintCommandParser(VariableManager varManager) {
        this.varManager = varManager;
    }

    /**
     * 解析一行以 "print" 开头的命令
     * 例如:
     *   print.console("服务器要重启啦");
     *   print.player.all("欢迎来到服务器!");
     *   print.player["Steve"]("你好, Steve!");
     */
    public void parsePrintCommand(String line) {
        // 去掉 "print"
        String afterPrint = line.substring("print".length()).trim();
        // 可能是 ".console(\"...\");" 或 ".player.all(\"...\");" 等

        // 判断是否以 '.' 开头
        if (!afterPrint.startsWith(".")) {
            // 语法不符合 (必须是 print.console / print.player)
            return;
        }
        // 去掉点
        afterPrint = afterPrint.substring(1).trim();
        // 现在可能是 "console(\"...\");" 或 "player.all(\"...\");" 或 "player[\"...\"](\"...\");"

        if (afterPrint.startsWith("console")) {
            parseConsoleCommand(afterPrint);
        } else if (afterPrint.startsWith("player")) {
            parsePlayerCommand(afterPrint);
        } else {
            // 未来可扩展: print.xxx(...)
        }
    }

    /**
     * 解析 print.console("内容");
     */
    private void parseConsoleCommand(String afterPrint) {
        // 去掉 "console"
        String afterConsole = afterPrint.substring("console".length()).trim();
        // 应该形如: ("内容");
        if (!afterConsole.startsWith("(")) {
            return; // 语法错误: 缺少 (
        }
        int closeParenIndex = afterConsole.indexOf(")");
        if (closeParenIndex == -1) {
            return; // 缺少 )
        }
        // 取出括号内内容
        String insideParen = afterConsole.substring(1, closeParenIndex).trim();
        // 去掉双引号
        String message = stripQuotes(insideParen);

        // 打印到控制台
        printToConsole(message);
    }

    /**
     * 解析 print.player.all("内容"); 或 print.player["玩家名"]("内容");
     */
    private void parsePlayerCommand(String afterPrint) {
        // 去掉 "player"
        String afterPlayer = afterPrint.substring("player".length()).trim();
        // 可能是 ".all(\"...\");" 或 "[\"Steve\"](\"...\");"

        // 判断是 .all(...) 还是 ["玩家名"](...)
        if (afterPlayer.startsWith(".")) {
            // 解析 .all("内容");
            parsePlayerAllCommand(afterPlayer);
        } else if (afterPlayer.startsWith("[")) {
            // 解析 ["玩家名"]("内容");
            parsePlayerNameCommand(afterPlayer);
        } else {
            // 语法错误或尚未支持
        }
    }

    /**
     * 解析 print.player.all("内容");
     */
    private void parsePlayerAllCommand(String afterPlayer) {
        // 去掉 .
        afterPlayer = afterPlayer.substring(1).trim(); // "all(\"内容\");"
        if (!afterPlayer.startsWith("all")) {
            return; // 语法错误
        }
        String afterAll = afterPlayer.substring("all".length()).trim(); // ("内容");
        if (!afterAll.startsWith("(")) {
            return;
        }
        int closeParenIndex = afterAll.indexOf(")");
        if (closeParenIndex == -1) {
            return;
        }
        String insideParen = afterAll.substring(1, closeParenIndex).trim();
        String message = stripQuotes(insideParen);

        broadcastMessageToAll(message);
    }

    /**
     * 解析 print.player["Steve"]("内容");
     */
    private void parsePlayerNameCommand(String afterPlayer) {
        // 以 [ 开头 => ["Steve"]("内容");
        int closeBracketIndex = afterPlayer.indexOf("]");
        if (closeBracketIndex == -1) {
            return;
        }
        String insideBracket = afterPlayer.substring(1, closeBracketIndex).trim();
        String playerName = stripQuotes(insideBracket);

        String afterBracket = afterPlayer.substring(closeBracketIndex + 1).trim();
        if (!afterBracket.startsWith("(")) {
            return;
        }
        int closeParenIndex = afterBracket.indexOf(")");
        if (closeParenIndex == -1) {
            return;
        }
        String insideParen = afterBracket.substring(1, closeParenIndex).trim();
        String message = stripQuotes(insideParen);

        sendMessageToPlayer(playerName, message);
    }

    /**
     * 给控制台打印
     */
    private void printToConsole(String msg) {
        // 先做颜色替换
        String finalMsg = ColorUtil.colorize(msg);
        Bukkit.getLogger().info("[ScriptPrint] " + stripColorForConsole(finalMsg));
    }

    /**
     * 给所有在线玩家发送消息（带颜色）
     */
    private void broadcastMessageToAll(String msg) {
        String finalMsg = ColorUtil.colorize(msg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(finalMsg);
        }
        Bukkit.getLogger().info("[all] " + stripColorForConsole(finalMsg));
    }

    /**
     * 给指定玩家发送消息
     */
    private void sendMessageToPlayer(String playerName, String msg) {
        String finalMsg = ColorUtil.colorize(msg);
        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null && target.isOnline()) {
            target.sendMessage(finalMsg);
            Bukkit.getLogger().info("[" + playerName + "] " + stripColorForConsole(finalMsg));
        } else {
            Bukkit.getLogger().warning("玩家 " + playerName + " 不在线或不存在！");
        }
    }

    /**
     * 去掉字符串两端的双引号
     */
    private String stripQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /**
     * 去掉§颜色，用于控制台输出
     */
    private String stripColorForConsole(String input) {
        return input.replaceAll("§.", "");
    }
}
