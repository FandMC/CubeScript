package fandmc.cubescript.parser;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ForCommandParser {

    private final ScriptParser scriptParser;
    private final JavaPlugin plugin;

    public ForCommandParser(ScriptParser parser, JavaPlugin plugin) {
        this.scriptParser = parser;
        this.plugin = plugin;
    }

    public void parseForLine(String line, Runnable onComplete, int lineIndex, List<String> allLines) {
        // e.g. for range(3) {
        int bracePos = line.indexOf("{");
        if (bracePos==-1){
            onComplete.run();
            return;
        }
        String forHead = line.substring("for".length(), bracePos).trim(); // "range(3)" or "true"

        boolean isRange=false; int rangeCount=0; boolean isTrueLoop=false;
        if (forHead.startsWith("range(")) {
            int cp=forHead.indexOf(")");
            if (cp==-1){
                onComplete.run(); return;
            }
            String numStr = forHead.substring("range(".length(), cp).trim();
            try {
                // 若numStr是变量
                String expanded = scriptParser.expandVariables(numStr); // 如果里面是 "A" 之类
                rangeCount = Integer.parseInt(expanded);
                isRange=true;
            } catch(Exception e){
                onComplete.run();
                return;
            }
        }
        else if (forHead.equals("true")){
            isTrueLoop=true;
        }
        else {
            onComplete.run();
            return;
        }

        // 收集花括号 {...} 行
        List<String> blockLines = new ArrayList<>();
        int cursor=lineIndex+1;
        while(true){
            if (cursor>=allLines.size()){
                onComplete.run();
                return;
            }
            String nxt = allLines.get(cursor).trim();
            cursor++;
            if (nxt.equals("}")) {
                break;
            }
            blockLines.add(nxt);
        }

        // 让 scriptParser 跳到cursor
        // 即 for块跳过
        scriptParserSetIndex(cursor);

        // 执行循环
        if (isRange) {
            runRangeLoop(blockLines,rangeCount,onComplete);
        } else if (isTrueLoop){
            runTrueLoop(blockLines,onComplete);
        }
    }

    private void runRangeLoop(List<String> blockLines,int count, Runnable onComplete){
        runRangeRecursive(blockLines,0,count,onComplete);
    }

    private void runRangeRecursive(List<String> blockLines,int i,int max,Runnable onComplete){
        if (i>=max){
            onComplete.run();
            return;
        }
        // 执行 block
        runBlockAsync(blockLines, broken->{
            if (broken){
                onComplete.run();
            } else {
                runRangeRecursive(blockLines, i+1, max, onComplete);
            }
        });
    }

    private void runTrueLoop(List<String> blockLines,Runnable onComplete){
        runTrueRecursive(blockLines,0,onComplete);
    }

    private void runTrueRecursive(List<String> blockLines,int iter,Runnable onComplete){
        if (iter>10000){ // 安全限制
            onComplete.run();
            return;
        }
        runBlockAsync(blockLines, broken->{
            if (broken){
                onComplete.run();
            } else {
                runTrueRecursive(blockLines, iter+1, onComplete);
            }
        });
    }

    private void runBlockAsync(List<String> lines, java.util.function.Consumer<Boolean> callback){
        // 异步执行 block, 如果遇到 break; => broken=true
        AsyncBlockParser parser = new AsyncBlockParser(lines, scriptParser, callback);
        parser.start();
    }

    // 用反射或在 ScriptParser 提供 public 方法
    private void scriptParserSetIndex(int newIndex) {
        try {
            java.lang.reflect.Field f = scriptParser.getClass().getDeclaredField("currentIndex");
            f.setAccessible(true);
            f.set(scriptParser, newIndex);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
