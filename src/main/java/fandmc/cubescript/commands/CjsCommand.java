package fandmc.cubescript.commands;

import fandmc.cubescript.utils.FileManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CjsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§a[CJS] 用法: /cjs <put|files|load|unload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "put":
                handlePut(sender, args);
                break;

            case "files":
                handleFiles(sender, args);
                break;

            case "load":
                handleLoad(sender, args);
                break;

            case "unload":
                handleUnload(sender, args);
                break;

            default:
                sender.sendMessage("§c未知命令: /cjs <put|files|load|unload>");
        }

        return true;
    }

    private void handlePut(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c用法: /cjs put <文件名>");
            return;
        }
        String fileName = args[1];
        String content = FileManager.readFromFile(fileName);
        if (content != null) {
            sender.sendMessage("§a文件内容:\n" + content);
        } else {
            sender.sendMessage("§c无法读取文件 " + fileName + "，请检查文件是否存在。");
        }
    }

    private void handleFiles(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§c用法: /cjs files <write|changename|delete|create> <args...>");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "write":
                handleFilesWrite(sender, args);
                break;
            case "changename":
                handleFilesChangeName(sender, args);
                break;
            case "delete":
                handleFilesDelete(sender, args);
                break;
            case "create":
                handleFilesCreate(sender, args);
                break;
            default:
                sender.sendMessage("§c未知命令: /cjs files <write|changename|delete|create>");
        }
    }

    private void handleLoad(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c用法: /cjs load <文件名>");
            return;
        }
        String fileName = args[1];
        if (FileManager.loadScript(fileName, sender)) {
            sender.sendMessage("§a脚本 " + fileName + " 已加载并执行！");
        } else {
            sender.sendMessage("§c加载脚本失败，请检查文件是否存在或内容是否正确。");
        }
    }

    private void handleUnload(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§c用法: /cjs unload <文件名>");
            return;
        }
        String fileName = args[1];
        if (FileManager.unloadScript(fileName)) {
            sender.sendMessage("§a脚本 " + fileName + " 已卸载！");
        } else {
            sender.sendMessage("§c卸载脚本失败，可能未加载该脚本。");
        }
    }

    private void handleFilesWrite(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§c用法: /cjs files write <文件名> <内容...>");
            return;
        }
        String fileName = args[2];
        // 将 args[3] 开始的部分拼接成写入内容
        // 这里简单写法：跳过 /cjs(0) files(1) write(2) fileName(3) => offset = 4
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String content = sb.toString().trim();

        boolean success = FileManager.writeToFile(fileName, content);
        if (success) {
            sender.sendMessage("§a文件 " + fileName + " 写入成功！");
        } else {
            sender.sendMessage("§c文件 " + fileName + " 写入失败！");
        }
    }

    private void handleFilesChangeName(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§c用法: /cjs files changename <旧文件名> <新文件名>");
            return;
        }
        String oldName = args[2];
        String newName = args[3];

        boolean success = FileManager.changeFileName(oldName, newName);
        if (success) {
            sender.sendMessage("§a文件 " + oldName + " 已重命名为 " + newName + "！");
        } else {
            sender.sendMessage("§c文件重命名失败！");
        }
    }

    private void handleFilesDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§c用法: /cjs files delete <文件名>");
            return;
        }
        String fileName = args[2];
        boolean success = FileManager.deleteFile(fileName);
        if (success) {
            sender.sendMessage("§a文件 " + fileName + " 删除成功！");
        } else {
            sender.sendMessage("§c文件删除失败！");
        }
    }

    private void handleFilesCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§c用法: /cjs files create <文件名>");
            return;
        }
        String fileName = args[2];
        boolean success = FileManager.createFile(fileName);
        if (success) {
            sender.sendMessage("§a文件 " + fileName + " 创建成功！");
        } else {
            sender.sendMessage("§c文件创建失败！");
        }
    }
}
