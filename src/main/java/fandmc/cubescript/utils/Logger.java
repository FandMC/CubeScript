package fandmc.cubescript.utils;

import org.bukkit.Bukkit;

public class Logger {

    private static final String PREFIX = "[CubeScript] ";

    public static void logInfo(String message) {
        Bukkit.getLogger().info(PREFIX + message);
    }

    public static void logWarning(String message) {
        Bukkit.getLogger().warning(PREFIX + message);
    }

    public static void logError(int lineNumber, String lineContent, Exception e) {
        Bukkit.getLogger().warning(PREFIX + "解析错误 (行 " + lineNumber + "): " + lineContent);
        Bukkit.getLogger().warning(PREFIX + "错误信息: " + e.getMessage());
    }
}
