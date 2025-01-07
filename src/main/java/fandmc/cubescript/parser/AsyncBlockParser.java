package fandmc.cubescript.parser;

import java.util.List;
import java.util.function.Consumer;

public class AsyncBlockParser {
    private final List<String> lines;
    private final ScriptParser parentParser;
    private final Consumer<Boolean> onFinish; // true=broken

    private int index=0;
    private boolean broken=false;

    public AsyncBlockParser(List<String> lines, ScriptParser parent, Consumer<Boolean> finish){
        this.lines=lines;
        this.parentParser=parent;
        this.onFinish=finish;
    }

    public void start(){
        runNext();
    }

    private void runNext(){
        if (index>=lines.size()){
            onFinish.accept(broken);
            return;
        }
        String line = lines.get(index).trim();
        index++;
        if (line.isEmpty()|| line.startsWith("//")|| line.startsWith("#")){
            runNext();
            return;
        }
        if (line.equalsIgnoreCase("break;")){
            broken=true;
            onFinish.accept(true);
            return;
        }
        // 用异步 parseLine
        parentParser.parseLineAsync(line, this::runNext);
    }
}
