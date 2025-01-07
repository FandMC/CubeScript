package fandmc.cubescript.utils;

import java.util.HashMap;
import java.util.Map;

public class ColorUtil {
    // 自定义 <xxx> -> §? 映射
    private static final Map<String, String> COLOR_MAP = new HashMap<>();
    static {
        COLOR_MAP.put("<black>", "§0");
        COLOR_MAP.put("<dark_blue>", "§1");
        COLOR_MAP.put("<dark_green>", "§2");
        COLOR_MAP.put("<dark_aqua>", "§3");
        COLOR_MAP.put("<dark_red>", "§4");
        COLOR_MAP.put("<dark_purple>", "§5");
        COLOR_MAP.put("<gold>", "§6");
        COLOR_MAP.put("<gray>", "§7");
        COLOR_MAP.put("<dark_gray>", "§8");
        COLOR_MAP.put("<blue>", "§9");
        COLOR_MAP.put("<green>", "§a");
        COLOR_MAP.put("<aqua>", "§b");
        COLOR_MAP.put("<red>", "§c");
        COLOR_MAP.put("<light_purple>", "§d");
        COLOR_MAP.put("<yellow>", "§e");
        COLOR_MAP.put("<white>", "§f");
    }

    /**
     * 将字符串中的自定义颜色标记 (<red>, <gold>, etc.) 和
     * & 颜色/格式代码 (&c, &l, &r, etc.) 转换为 §(U+00A7) 形式
     */
    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        // 1) 替换 & -> § (针对 [0-9a-fk-or])
        //   正则中 ([0-9a-fk-orA-FK-OR]) 匹配到第1组, 用 §$1 替换
        message = message.replaceAll("&([0-9a-fk-orA-FK-OR])", "§$1");

        // 2) 替换 <xxx> -> §?
        for (Map.Entry<String, String> entry : COLOR_MAP.entrySet()) {
            String key = entry.getKey();   // <red>
            String val = entry.getValue(); // §c
            // 全部替换
            message = message.replace(key, val);
        }

        return message;
    }
    // 这里是常用颜色代码
    public static final String RESET = "§r";
    public static final String BLACK = "§0";
    public static final String DARK_BLUE = "§1";
    public static final String DARK_GREEN = "§2";
    public static final String DARK_AQUA = "§3";
    public static final String DARK_RED = "§4";
    public static final String DARK_PURPLE = "§5";
    public static final String GOLD = "§6";
    public static final String GRAY = "§7";
    public static final String DARK_GRAY = "§8";
    public static final String BLUE = "§9";
    public static final String GREEN = "§a";
    public static final String AQUA = "§b";
    public static final String RED = "§c";
    public static final String LIGHT_PURPLE = "§d";
    public static final String YELLOW = "§e";
    public static final String WHITE = "§f";

    // 你也可以加粗、下划线等修饰符
    public static final String BOLD = "§l";
    public static final String UNDERLINE = "§n";
    public static final String ITALIC = "§o";
    public static final String STRIKETHROUGH = "§m";
}
