package fandmc.cubescript.scripting;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.stream.Collectors;

public class ConditionEvaluator {

    /**
     * 解析并评估 if(...) 中的表达式
     * 支持:
     *   1) setall(true) => 针对所有在线玩家
     *   2) Player.hp
     *   3) Player.gamemode
     *   4) Player.name
     *   5) Player.team
     *   6) 以及通用比较符号(==, !=, >, <, >=, <=)等
     */
    public static boolean evaluate(String condition, CommandSender sender) {
        // 先做变量替换
        condition = VariableParser.replaceVariables(condition).trim();

        // 若包含最外层括号，比如 "(Player.name == "Steve")"，可再去掉
        if (condition.startsWith("(") && condition.endsWith(")")) {
            condition = condition.substring(1, condition.length() - 1).trim();
        }

        Bukkit.getLogger().info("[CubeScript Debug] Evaluating condition: " + condition);

        // 如果写了 setall(true)，表示要对“所有在线玩家”都检查该条件
        // 并且只有全部满足时才返回 true
        // 先检测有没有 setall(true)，如果有，就把它替换掉，并将 forAllPlayers = true
        boolean forAllPlayers = false;
        if (condition.contains("setall(true)")) {
            forAllPlayers = true;
            // 去掉 setall(true) 文字，以免干扰后续解析
            condition = condition.replace("setall(true)", "").trim();

            // 如果原本是 "setall(true)" 并且没有别的内容，就相当于只有 setall(true)
            // 这里可以直接认为没有具体条件可检，就看你要怎么处理
            if (condition.isEmpty()) {
                // 如果你想没有其他条件就直接返回 true，也行。先演示返回 false。
                Bukkit.getLogger().info("[CubeScript Debug] 'setall(true)' found but no actual condition left.");
                return false;
            }
        }

        // 如果 forAllPlayers = true，则遍历所有在线玩家检查 condition
        // 若全部通过 => true；有一个不通过 => false
        if (forAllPlayers) {
            List<Player> allPlayers = Bukkit.getOnlinePlayers().stream().collect(Collectors.toList());
            if (allPlayers.isEmpty()) {
                // 没人在线？看你需求，也许返回 false 或 true 都行
                return false;
            }

            // 对每个玩家都要检查 "Player.xxx" 的条件是否满足
            for (Player p : allPlayers) {
                // 如果有一个玩家不满足 => 整体 false
                if (!evaluatePlayerCondition(condition, p)) {
                    return false;
                }
            }
            return true; // 所有人都满足
        } else {
            // 否则仅针对当前 sender（若是 Player 才能读取 Player.xxx）
            // 如果不是玩家，默认只能返回 false（除非你想加别的处理）
            if (sender instanceof Player) {
                return evaluatePlayerCondition(condition, (Player) sender);
            } else {
                // 不是玩家 => condition 里若有 Player.xxx 通常就无法判定
                Bukkit.getLogger().warning("[CubeScript Debug] Condition references Player.xxx but sender is not a Player.");
                return false;
            }
        }
    }

    /**
     * 仅针对某个具体 Player 来评估 condition，如 "Player.hp > 10"、"Player.team == 蓝队" 等
     */
    private static boolean evaluatePlayerCondition(String condition, Player player) {
        // 支持 Player.hp / Player.gamemode / Player.name / Player.team
        if (condition.startsWith("Player.")) {
            if (condition.startsWith("Player.hp")) {
                double hp = player.getHealth();
                // 提取 "hp" 后面的比较操作，比如 " >= 10"
                String sub = condition.substring("Player.hp".length());
                return compareDouble(hp, sub);
            }
            else if (condition.startsWith("Player.gamemode")) {
                // e.g. "Player.gamemode == SURVIVAL"
                String gm = player.getGameMode().name();
                String sub = condition.substring("Player.gamemode".length()).trim();
                return compareString(gm, sub);
            }
            else if (condition.startsWith("Player.name")) {
                // e.g. "Player.name == "Notch""
                String pName = player.getName();
                String sub = condition.substring("Player.name".length()).trim();
                return compareString(pName, sub);
            }
            else if (condition.startsWith("Player.team")) {
                // 这里演示用 Scoreboard Team 来获取队伍
                // 你也可以用其他插件API / Placeholder / 变量
                Team team = player.getScoreboard().getEntryTeam(player.getName());
                String teamName = (team == null ? "" : team.getName());
                String sub = condition.substring("Player.team".length()).trim();
                return compareString(teamName, sub);
            }
        }

        // 如果不是以 Player.xxx 开头，还可以加别的布尔逻辑(如 true == true、myBool == true 等)
        // 这里略。若没有别的逻辑 => 不支持
        Bukkit.getLogger().warning("[CubeScript Debug] Unknown or unsupported condition: " + condition);
        return false;
    }

    /**
     * 简单数字比较：支持 >=, <=, >, <, ==, != 后紧跟一个数字
     */
    private static boolean compareDouble(double actual, String condition) {
        condition = condition.trim();
        try {
            if (condition.startsWith(">=")) {
                double val = Double.parseDouble(condition.substring(2).trim());
                return actual >= val;
            } else if (condition.startsWith("<=")) {
                double val = Double.parseDouble(condition.substring(2).trim());
                return actual <= val;
            } else if (condition.startsWith(">")) {
                double val = Double.parseDouble(condition.substring(1).trim());
                return actual > val;
            } else if (condition.startsWith("<")) {
                double val = Double.parseDouble(condition.substring(1).trim());
                return actual < val;
            } else if (condition.startsWith("==")) {
                double val = Double.parseDouble(condition.substring(2).trim());
                return actual == val;
            } else if (condition.startsWith("!=")) {
                double val = Double.parseDouble(condition.substring(2).trim());
                return actual != val;
            }
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("[CubeScript Debug] Error parsing number: " + condition + ", " + e.getMessage());
        }
        Bukkit.getLogger().warning("[CubeScript Debug] Unsupported comparison operation: " + condition);
        return false;
    }

    /**
     * 简单字符串比较，只支持 "==" 和 "!="，以及等号两侧可带或不带引号
     */
    private static boolean compareString(String actual, String condition) {
        condition = condition.trim();
        if (condition.startsWith("==")) {
            String expected = condition.substring(2).trim().replace("\"", "");
            return actual.equals(expected);
        }
        else if (condition.startsWith("!=")) {
            String notExpected = condition.substring(2).trim().replace("\"", "");
            return !actual.equals(notExpected);
        }
        Bukkit.getLogger().warning("[CubeScript Debug] Unsupported string operation: " + condition);
        return false;
    }
}
