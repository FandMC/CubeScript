package fandmc.cubescript.commands;

import fandmc.cubescript.parser.ScriptParser;
import fandmc.cubescript.utils.ColorUtil;
import fandmc.cubescript.utils.FileManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CjsCommand implements CommandExecutor {

    private final FileManager fileManager;
    private final JavaPlugin plugin;

    public CjsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.fileManager = new FileManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.YELLOW + "用法: " + ColorUtil.RESET
                    + "/cubescript <put / files / load / unload> ...");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "put":
                return handlePut(sender, args);
            case "files":
                return handleFiles(sender, args);
            case "load":
                return handleLoad(sender, args);
            case "unload":
                return handleUnload(sender, args);
            default:
                sender.sendMessage(ColorUtil.RED + "未知的子命令: " + subCommand);
                return true;
        }
    }

    /**
     * files子命令: /cubescript files <write / delete / create / list>
     */
    private boolean handleFiles(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.YELLOW + "用法: " + ColorUtil.RESET
                    + "/cubescript files <write / delete / create / list> ...");
            return true;
        }

        String sub = args[1].toLowerCase();
        switch (sub) {
            case "write":
                return handleFilesWrite(sender, args);
            case "delete":
                return handleFilesDelete(sender, args);
            case "create":
                return handleFilesCreate(sender, args);
            case "list":
                return handleFilesList(sender);
            default:
                sender.sendMessage(ColorUtil.RED + "未知的 files 子命令: " + sub);
                return true;
        }
    }

    /**
     * /cubescript files write <fileName> <content>
     */
    private boolean handleFilesWrite(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ColorUtil.RED + "用法: /cubescript files write <fileName> <content>");
            sender.sendMessage(ColorUtil.YELLOW + "如果需要换行，请使用 \\n 替代。");
            return true;
        }
        String fileName = args[2];

        // 拼接写入内容
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String content = sb.toString().trim();

        boolean success = fileManager.writeFile(fileName, content);
        if (success) {
            sender.sendMessage(ColorUtil.GREEN + "写入文件成功: " + fileName);
        } else {
            sender.sendMessage(ColorUtil.RED + "写入文件失败: " + fileName);
        }
        return true;
    }

    private boolean handleFilesDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.RED + "用法: /cubescript files delete <fileName>");
            return true;
        }
        String fileName = args[2];
        boolean success = fileManager.deleteFile(fileName);
        if (success) {
            sender.sendMessage(ColorUtil.GREEN + "成功删除文件: " + fileName);
        } else {
            sender.sendMessage(ColorUtil.RED + "删除文件失败(文件不存在?): " + fileName);
        }
        return true;
    }

    private boolean handleFilesCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ColorUtil.RED + "用法: /cubescript files create <fileName>");
            return true;
        }
        String fileName = args[2];
        boolean success = fileManager.createFile(fileName);
        if (success) {
            sender.sendMessage(ColorUtil.GREEN + "已创建文件: " + fileName);
        } else {
            sender.sendMessage(ColorUtil.RED + "文件已存在或创建失败: " + fileName);
        }
        return true;
    }

    private boolean handleFilesList(CommandSender sender) {
        List<String> files = fileManager.listFiles();
        if (files.isEmpty()) {
            sender.sendMessage(ColorUtil.YELLOW + "目前没有任何文件。");
        } else {
            sender.sendMessage(ColorUtil.GREEN + "文件列表:" + ColorUtil.RESET);
            for (String f : files) {
                sender.sendMessage("- " + f);
            }
        }
        return true;
    }

    /**
     * /cubescript load <filename.cjs>
     * 从脚本文件读取内容 -> 使用ScriptParser解析
     */
    private boolean handleLoad(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.colorize("&c用法: /cubescript load <脚本文件.cjs>"));
            return true;
        }
        String scriptName = args[1];
        if (!scriptName.toLowerCase().endsWith(".cjs")) {
            sender.sendMessage(ColorUtil.colorize("&c仅支持加载后缀为 .cjs 的文件: " + scriptName));
            return true;
        }

        List<String> lines = fileManager.readFile(scriptName);
        if (lines.isEmpty()) {
            sender.sendMessage(ColorUtil.colorize("&c未找到脚本或脚本为空: " + scriptName));
            return true;
        }

        // 创建异步脚本解析器
        ScriptParser parser = new ScriptParser(fileManager, plugin);
        parser.startScript(lines);

        sender.sendMessage(ColorUtil.colorize("&a脚本 " + scriptName + " 已开始执行(异步), 请注意查看效果。"));
        return true;
    }

    private boolean handleUnload(CommandSender sender, String[] args) {
        // TODO: 这里可停止脚本执行(若要支持多脚本, 需脚本管理器)
        sender.sendMessage(ColorUtil.colorize("&a卸载脚本功能暂未实现。"));
        return true;
    }

    /**
     * /cubescript put <filename>
     * 测试: 查看脚本文件内容
     */
    private boolean handlePut(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtil.RED + "用法: /cubescript put <文件名>");
            return true;
        }
        String fileName = args[1];
        List<String> content = fileManager.readFile(fileName);
        if (content.isEmpty()) {
            sender.sendMessage(ColorUtil.RED + "无法读取或文件为空: " + fileName);
            return true;
        }
        sender.sendMessage(ColorUtil.GREEN + "文件 " + fileName + " 内容:");
        for (String line : content) {
            sender.sendMessage(line);
        }
        return true;
    }
}
