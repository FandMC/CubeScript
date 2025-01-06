package fandmc.cubescript.utils;

import fandmc.cubescript.Main;
import fandmc.cubescript.scripting.ScriptParser;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class FileManager {

    private static final File scriptFolder = new File(Main.getInstance().getDataFolder(), "scripts");
    // 用于记录已加载脚本的内容（如果需要在脚本之间共享数据，可在此存储）
    private static final Map<String, String> loadedScripts = new HashMap<>();

    static {
        if (!scriptFolder.exists()) {
            scriptFolder.mkdirs();
        }
    }

    /**
     * 将给定字符串内容写入指定文件（覆盖写入）
     */
    public static boolean writeToFile(String fileName, String content) {
        try {
            // 将 "\n" 转换为真正的换行符（若用户输入的是转义字符）
            content = content.replace("\\n", "\n");

            File file = new File(scriptFolder, fileName);
            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write(content);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 从文件中读取全部内容
     */
    public static String readFromFile(String fileName) {
        File file = new File(scriptFolder, fileName);
        if (!file.exists()) {
            return null;
        }
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 加载并执行脚本文件
     */
    public static boolean loadScript(String fileName, CommandSender sender) {
        File file = new File(scriptFolder, fileName);
        if (!file.exists()) {
            if (sender != null) {
                sender.sendMessage("§c文件 " + fileName + " 不存在！");
            }
            return false;
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            loadedScripts.put(fileName, content);
            ScriptParser.parseAndExecute(content, sender);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            if (sender != null) {
                sender.sendMessage("§c加载脚本 " + fileName + " 时发生错误！");
            }
            return false;
        }
    }

    /**
     * 卸载脚本（本示例仅从 map 中删除，以示管理；实际可做更多清理动作）
     */
    public static boolean unloadScript(String fileName) {
        return loadedScripts.remove(fileName) != null;
    }

    /**
     * 创建空白脚本文件
     */
    public static boolean createFile(String fileName) {
        try {
            File file = new File(scriptFolder, fileName);
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 重命名脚本文件
     */
    public static boolean changeFileName(String oldName, String newName) {
        File oldFile = new File(scriptFolder, oldName);
        File newFile = new File(scriptFolder, newName);
        return oldFile.exists() && oldFile.renameTo(newFile);
    }

    /**
     * 删除脚本文件
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(scriptFolder, fileName);
        return file.exists() && file.delete();
    }

    /**
     * 列出所有脚本文件
     */
    public static List<String> listAllFiles() {
        List<String> fileNames = new ArrayList<>();
        if (scriptFolder.exists() && scriptFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(scriptFolder.listFiles())) {
                if (file.isFile()) {
                    fileNames.add(file.getName());
                }
            }
        }
        return fileNames;
    }
}
