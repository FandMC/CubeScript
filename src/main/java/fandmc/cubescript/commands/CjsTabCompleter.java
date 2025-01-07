package fandmc.cubescript.commands;

import fandmc.cubescript.utils.FileManager;
import org.bukkit.command.Command;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CjsTabCompleter implements TabCompleter {

    private final FileManager fileManager;

    public CjsTabCompleter() {
        this.fileManager = new FileManager();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // 第一层补全: put, files, load, unload
            if ("put".startsWith(args[0].toLowerCase())) {
                suggestions.add("put");
            }
            if ("files".startsWith(args[0].toLowerCase())) {
                suggestions.add("files");
            }
            if ("load".startsWith(args[0].toLowerCase())) {
                suggestions.add("load");
            }
            if ("unload".startsWith(args[0].toLowerCase())) {
                suggestions.add("unload");
            }
        } else {
            // /cubescript put <filename> -> 可补全所有文件
            if (args[0].equalsIgnoreCase("put") && args.length == 2) {
                for (String fileName : fileManager.listFiles()) {
                    if (fileName.toLowerCase().startsWith(args[1].toLowerCase())) {
                        suggestions.add(fileName);
                    }
                }
            }
            // /cubescript files ...
            else if (args[0].equalsIgnoreCase("files")) {
                if (args.length == 2) {
                    // write, delete, create, list
                    if ("write".startsWith(args[1].toLowerCase())) {
                        suggestions.add("write");
                    }
                    if ("delete".startsWith(args[1].toLowerCase())) {
                        suggestions.add("delete");
                    }
                    if ("create".startsWith(args[1].toLowerCase())) {
                        suggestions.add("create");
                    }
                    if ("list".startsWith(args[1].toLowerCase())) {
                        suggestions.add("list");
                    }
                }
                // /cubescript files delete/write <fileName> ...
                else if (args.length == 3 && (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("write"))) {
                    for (String fileName : fileManager.listFiles()) {
                        if (fileName.toLowerCase().startsWith(args[2].toLowerCase())) {
                            suggestions.add(fileName);
                        }
                    }
                }
            }
            // /cubescript load <scriptName.cjs>
            else if (args[0].equalsIgnoreCase("load") && args.length == 2) {
                // 只列出 .cjs 文件
                for (String fileName : fileManager.listFiles()) {
                    if (fileName.toLowerCase().endsWith(".cjs")) {
                        if (fileName.toLowerCase().startsWith(args[1].toLowerCase())) {
                            suggestions.add(fileName);
                        }
                    }
                }
            }
            // /cubescript unload <scriptName.cjs>
            else if (args[0].equalsIgnoreCase("unload") && args.length == 2) {
                // 同样只列出 .cjs 文件
                for (String fileName : fileManager.listFiles()) {
                    if (fileName.toLowerCase().endsWith(".cjs")) {
                        if (fileName.toLowerCase().startsWith(args[1].toLowerCase())) {
                            suggestions.add(fileName);
                        }
                    }
                }
            }
        }

        return suggestions;
    }
}
