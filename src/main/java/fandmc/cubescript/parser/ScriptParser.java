package fandmc.cubescript.parser;

import fandmc.cubescript.utils.FileManager;
import fandmc.cubescript.utils.VariableManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Consumer;

public class ScriptParser {

    private final ForCommandParser forParser;
    private final PrintCommandParser printParser;
    private final FilesCommandParser filesParser;
    private final VarCommandParser varParser;
    private final JavaPlugin plugin;
    private final VariableManager varManager = new VariableManager();

    private final FileManager fileManager;
    private CommandSender scriptSender; // 用于 getPlayerName
    public void setScriptSender(CommandSender sender) {
        this.scriptSender = sender;
    }

    private List<String> allLines;
    private int currentIndex = 0;
    private boolean running = false;

    public ScriptParser(FileManager fm, JavaPlugin plugin) {
        this.varParser = new VarCommandParser(varManager);
        this.plugin = plugin;
        this.fileManager = fm;          // 从外面注入
        this.printParser = new PrintCommandParser(varManager);
        this.filesParser = new FilesCommandParser(fm, varManager);
        this.forParser = new ForCommandParser(this, plugin);
    }

    public void startScript(List<String> lines) {
        this.allLines = lines;
        this.currentIndex = 0;
        this.running = true;
        runNextLine();
    }

    private void runNextLine() {
        if (!running || currentIndex >= allLines.size()) {
            running = false;
            return;
        }
        String line = allLines.get(currentIndex++);
        parseLine(line);
    }

    public void parseLine(String line) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) {
            runNextLine();
            return;
        }

        if (line.startsWith("var ")) {
            // 调用 varParser
            varParser.parseVarCommand(line, this::runNextLine);
            return;
        }

        // for
        if (line.startsWith("for ")) {
            forParser.parseForLine(line, this::runNextLine, currentIndex-1, allLines);
            return;
        }

        // break; 顶层无效
        if (line.equalsIgnoreCase("break;")) {
            Bukkit.getLogger().info("[ScriptParser] 顶层 break; 无效");
            runNextLine();
            return;
        }

        // timeout
        if (line.startsWith("timeout(")) {
            parseTimeout(line);
            return;
        }

        // mc[...] 原版命令
        if (line.startsWith("mc[")) {
            parseMcCommand(line);
            return;
        }

        // print / files
        if (line.startsWith("print.")) {
            String expanded = expandVariables(line);
            printParser.parsePrintCommand(expanded);
            runNextLine();
            return;
        }
        if (line.startsWith("files.")) {
            String expanded = expandVariables(line);
            filesParser.parseFilesCommand(expanded);
            runNextLine();
            return;
        }

        // 未识别
        Bukkit.getLogger().info("[ScriptParser] 未识别命令: " + line);
        runNextLine();
    }

    private void parseTimeout(String line) {
        int start = line.indexOf("(");
        int end = line.indexOf(")");
        if (start==-1 || end==-1|| end<start) {
            runNextLine();
            return;
        }
        try {
            double sec = Double.parseDouble(line.substring(start+1, end).trim());
            if (sec<0) {
                runNextLine();
                return;
            }
            long ticks = (long)(sec*20);
            Bukkit.getScheduler().runTaskLater(plugin, this::runNextLine, ticks);
        } catch(Exception e){
            runNextLine();
        }
    }

    /**
     * 原版命令: mc[kill @e[type=!player]]
     * 也可用变量: mc[kill getPlayerName], mc[kill A]
     */
    private void parseMcCommand(String line) {
        int lb = line.indexOf("[");
        int rb = line.lastIndexOf("]");
        if (lb == -1 || rb == -1 || rb < lb) {
            runNextLine();
            return;
        }

        String cmdInside = line.substring(lb + 1, rb).trim();
        cmdInside = expandVariables(cmdInside);

        // 将命令调度到主线程执行
        String finalCmdInside = cmdInside;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmdInside);
            runNextLine(); // 执行完命令后继续下一行解析
        });
    }


    /**
     * 替换变量(含内置变量getPlayerName)
     * 简易实现: 若整段匹配某个变量名，就替换. 若整段==getPlayerName，就用 scriptSender.getName()
     */
    public String expandVariables(String input) {
        // 先切分空格? 还是整段?
        // 简易写法: 若空格分多段, 里面也想替换? 需求可能更复杂.
        // 这里先做“若整段恰好等于一个变量名，就替换”，或等于 getPlayerName 就替换
        // 如果要更灵活, 需要更复杂的解析/占位符.

        // 1) 若等于 getPlayerName
        if (input.equalsIgnoreCase("getPlayerName")) {
            return scriptSender!=null ? scriptSender.getName() : "UnknownPlayer";
        }
        // 2) 若 varManager 有同名变量
        if (varManager.hasVariable(input)) {
            VariableManager.VarData v = varManager.getVariable(input);
            return v.value; // 直接返回存储值
        }
        // 3) 如果包含空格/多个token, 可以逐个token替换(可选)
        // 这里演示: split by space, token by token
        String[] tokens = input.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<tokens.length;i++){
            String t = tokens[i];
            // if t == "getPlayerName" => replace
            if (t.equalsIgnoreCase("getPlayerName")) {
                sb.append(scriptSender!=null ? scriptSender.getName():"UnknownPlayer");
            } else if (varManager.hasVariable(t)) {
                sb.append(varManager.getVariable(t).value);
            } else {
                sb.append(t);
            }
            if(i<tokens.length-1){
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String stripQuotes(String s) {
        s=s.trim();
        if (s.startsWith("\"")&& s.endsWith("\"")&&s.length()>=2){
            return s.substring(1, s.length()-1);
        }
        return s;
    }
    public void parseLineAsync(String line, Runnable callback) {
        // 异步运行解析逻辑
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            parseLine(line); // 调用同步解析逻辑
            // 回到主线程执行回调
            Bukkit.getScheduler().runTask(plugin, callback);
        });
    }
}
