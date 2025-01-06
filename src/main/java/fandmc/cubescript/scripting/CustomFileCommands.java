package fandmc.cubescript.scripting;

import fandmc.cubescript.utils.FileManager;
import org.bukkit.command.CommandSender;

public class CustomFileCommands {

    /**
     * 识别并执行 Files.xxx(...) 命令。
     * 若识别成功 => 返回 true，否则 false。
     */
    public static boolean handleFileCommand(String rawLine, CommandSender sender) {
        // 例:  Files.put("test.txt")
        //      Files.delete("test.txt")
        //      Files.write("test.txt","hello")
        //      Files.create("test.txt")

        // 1) Files.put("文件名")
        if (rawLine.startsWith("Files.put(") && rawLine.endsWith(")")) {
            String fileName = extractBetween(rawLine, "Files.put(", ")");
            if (fileName != null) {
                String content = FileManager.readFromFile(fileName);
                if (content == null) {
                    sender.sendMessage("§c文件 " + fileName + " 不存在或读取失败!");
                } else {
                    sender.sendMessage("§a[文件 " + fileName + " 内容]§r\n" + content);
                }
            }
            return true;
        }

        // 2) Files.delete("文件名")
        if (rawLine.startsWith("Files.delete(") && rawLine.endsWith(")")) {
            String fileName = extractBetween(rawLine, "Files.delete(", ")");
            if (fileName != null) {
                boolean success = FileManager.deleteFile(fileName);
                if (success) {
                    sender.sendMessage("§a文件 " + fileName + " 删除成功!");
                } else {
                    sender.sendMessage("§c文件 " + fileName + " 删除失败或不存在!");
                }
            }
            return true;
        }

        // 3) Files.create("文件名")
        if (rawLine.startsWith("Files.create(") && rawLine.endsWith(")")) {
            String fileName = extractBetween(rawLine, "Files.create(", ")");
            if (fileName != null) {
                boolean success = FileManager.createFile(fileName);
                if (success) {
                    sender.sendMessage("§a文件 " + fileName + " 创建成功!");
                } else {
                    sender.sendMessage("§c文件 " + fileName + " 创建失败(可能已存在)!");
                }
            }
            return true;
        }

        // 4) Files.write("文件名","内容")
        if (rawLine.startsWith("Files.write(") && rawLine.endsWith(")")) {
            String inside = extractBetween(rawLine, "Files.write(", ")");
            if (inside != null) {
                // 例如 inside == "test.txt","Hello World"
                String[] parts = splitFirstTwoArgs(inside, ',');
                if (parts.length == 2) {
                    String fileName = stripQuotes(parts[0].trim());
                    String fileContent = stripQuotes(parts[1].trim());
                    boolean success = FileManager.writeToFile(fileName, fileContent);
                    if (success) {
                        sender.sendMessage("§a文件 " + fileName + " 写入成功!");
                    } else {
                        sender.sendMessage("§c文件 " + fileName + " 写入失败!");
                    }
                } else {
                    sender.sendMessage("§cFiles.write(...) 格式错误! 示例: Files.write(\"filename\",\"content\")");
                }
            }
            return true;
        }

        // 如果都没匹配 => 返回 false
        return false;
    }

    /**
     * 从形如 Files.put("xxx") 中提取 "xxx"
     */
    private static String extractBetween(String line, String prefix, String suffix) {
        if (!line.startsWith(prefix) || !line.endsWith(suffix)) {
            return null;
        }
        return line.substring(prefix.length(), line.length() - suffix.length()).trim();
    }

    /**
     * 将 "aaa,bbb" 按第一个逗号进行分割 => ["aaa","bbb"]（若有更多逗号，也保留后面）
     */
    private static String[] splitFirstTwoArgs(String text, char delimiter) {
        int idx = text.indexOf(delimiter);
        if (idx < 0) {
            return new String[]{ text };
        }
        String arg1 = text.substring(0, idx);
        String arg2 = text.substring(idx + 1);
        return new String[]{ arg1, arg2 };
    }

    /**
     * 去掉首尾引号（如果存在）
     */
    private static String stripQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
