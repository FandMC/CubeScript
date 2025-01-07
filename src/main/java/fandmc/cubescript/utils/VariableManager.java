package fandmc.cubescript.utils;

import java.util.HashMap;
import java.util.Map;

public class VariableManager {

    // 存储变量: 变量名 -> (类型, 值)
    private final Map<String, VarData> vars = new HashMap<>();

    // 内置变量(如 getPlayerName)可以在这里动态获取
    // 或在 "expandVariables" 里判断

    public void setVariable(String name, String type, String value) {
        // 这里简单保存为字符串, 也可以真解析 int/boolean
        vars.put(name, new VarData(type, value));
    }

    public VarData getVariable(String name) {
        return vars.get(name);
    }

    public boolean hasVariable(String name) {
        return vars.containsKey(name);
    }

    public static class VarData {
        public final String type;
        public final String value;
        public VarData(String t, String v) {
            this.type = t;
            this.value = v;
        }
    }
}
