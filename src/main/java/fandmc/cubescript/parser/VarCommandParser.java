package fandmc.cubescript.parser;

import fandmc.cubescript.utils.VariableManager;
import org.bukkit.Bukkit;

/**
 * 专门负责解析 var 命令, 如:
 *   var int A = 1;
 *   var String B = "hello";
 *   var boolean C = true;
 */
public class VarCommandParser {

    private final VariableManager varManager;

    public VarCommandParser(VariableManager varManager) {
        this.varManager = varManager;
    }

    /**
     * 解析一行 var 声明, 完成后调用 onComplete.run()
     * 例如:
     *   var int A = 1;
     */
    public void parseVarCommand(String line, Runnable onComplete) {
        // "var int A = 1;"
        // 去掉"var "
        String afterVar = line.substring("var".length()).trim(); // e.g. int A = 1;
        String[] parts = afterVar.split("\\s+");
        if (parts.length < 3) {
            Bukkit.getLogger().warning("[VarCommandParser] var语法错误: " + line);
            onComplete.run();
            return;
        }
        // parts[0] = int/String/boolean, parts[1] = A, parts[2..] = = 1;
        String varType = parts[0];
        String varName = parts[1];

        // 找 '='
        int eqIndex = afterVar.indexOf("=");
        if (eqIndex == -1) {
            Bukkit.getLogger().warning("[VarCommandParser] var语法错误,缺少= : " + line);
            onComplete.run();
            return;
        }
        String valuePart = afterVar.substring(eqIndex + 1).trim();
        // 去掉末尾分号
        if (valuePart.endsWith(";")) {
            valuePart = valuePart.substring(0, valuePart.length() - 1).trim();
        }
        // 如果类型是 String，则去掉可能的引号
        if (varType.equalsIgnoreCase("String")) {
            valuePart = stripQuotes(valuePart);
        }

        // 存储
        varManager.setVariable(varName, varType, valuePart);
        Bukkit.getLogger().info("[VarCommandParser] 变量声明: " + varName + " = " + valuePart);

        // 执行下一行
        onComplete.run();
    }

    private String stripQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
