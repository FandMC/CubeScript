package fandmc.cubescript.commands;

import fandmc.cubescript.utils.FileManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class CjsTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("put", "files", "load", "unload"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("put")
                    || args[0].equalsIgnoreCase("load")
                    || args[0].equalsIgnoreCase("unload"))
            {
                suggestions.addAll(FileManager.listAllFiles());
            } else if (args[0].equalsIgnoreCase("files")) {
                suggestions.addAll(Arrays.asList("write", "changename", "delete", "create"));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("files")) {
                if (args[1].equalsIgnoreCase("changename")
                        || args[1].equalsIgnoreCase("delete"))
                {
                    // 文件名补全
                    suggestions.addAll(FileManager.listAllFiles());
                }
            }
        }

        return filterSuggestions(suggestions, args[args.length - 1]);
    }

    /**
     * 根据输入前缀进行过滤
     */
    private List<String> filterSuggestions(List<String> suggestions, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return suggestions;
        }
        List<String> filtered = new ArrayList<>();
        for (String s : suggestions) {
            if (s.toLowerCase().startsWith(prefix.toLowerCase())) {
                filtered.add(s);
            }
        }
        return filtered;
    }
}
