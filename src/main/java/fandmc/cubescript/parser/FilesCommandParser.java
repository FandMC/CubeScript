package fandmc.cubescript.parser;


import fandmc.cubescript.utils.FileManager;
import fandmc.cubescript.utils.VariableManager;

public class FilesCommandParser {

    private final FileManager fileManager;
    private final VariableManager varManager;
    public FilesCommandParser(FileManager fm, VariableManager vm) {
        this.fileManager = fm;
        this.varManager = vm;
    }
    public FileManager getFileManager() {
        return fileManager;
    }
    /**
     * 解析一行以 "files" 开头的命令
     * 例如:
     *   files.create("test.cjs");
     *   files.delete("test.cjs");
     *   files.write("test.cjs","Hello,world!");
     */
    public void parseFilesCommand(String line) {
        // 去掉 "files"
        String afterFiles = line.substring("files".length()).trim();
        // 可能是 ".create(\"...\");" 或 ".delete(\"...\");" 或 ".write(\"...\",\"...\");"

        if (!afterFiles.startsWith(".")) {
            // 语法错误: 要求 files.xxx
            return;
        }
        // 去掉点
        afterFiles = afterFiles.substring(1).trim();
        // 现在可能是 "create(\"...\");" / "delete(\"...\");" / "write(\"...\")"

        if (afterFiles.startsWith("create")) {
            parseCreateCommand(afterFiles);
        } else if (afterFiles.startsWith("delete")) {
            parseDeleteCommand(afterFiles);
        } else if (afterFiles.startsWith("write")) {
            parseWriteCommand(afterFiles);
        } else {
            // 不支持的子命令
        }
    }

    /**
     * 解析 files.create("文件名");
     */
    private void parseCreateCommand(String afterFiles) {
        // 去掉 "create"
        String afterCreate = afterFiles.substring("create".length()).trim();
        // 应该形如: ("文件名");
        if (!afterCreate.startsWith("(")) {
            return;
        }
        int closeParenIndex = afterCreate.indexOf(")");
        if (closeParenIndex == -1) {
            return;
        }
        // 括号里的部分
        String inside = afterCreate.substring(1, closeParenIndex).trim();
        String fileName = stripQuotes(inside);
        // 调用 FileManager
        boolean result = fileManager.createFile(fileName);
        // 你可以做点日志输出或提示，这里仅做示例
        if (result) {
            //System.out.println("[Files] Created file: " + fileName);
        } else {
           // System.out.println("[Files] Failed to create file or it already exists: " + fileName);
        }
    }

    /**
     * 解析 files.delete("文件名");
     */
    private void parseDeleteCommand(String afterFiles) {
        // 去掉 "delete"
        String afterDelete = afterFiles.substring("delete".length()).trim();
        // ("文件名");
        if (!afterDelete.startsWith("(")) {
            return;
        }
        int closeParenIndex = afterDelete.indexOf(")");
        if (closeParenIndex == -1) {
            return;
        }
        String inside = afterDelete.substring(1, closeParenIndex).trim();
        String fileName = stripQuotes(inside);
        boolean result = fileManager.deleteFile(fileName);
        if (result) {
           // System.out.println("[Files] Deleted file: " + fileName);
        } else {
           // System.out.println("[Files] Could not delete file: " + fileName);
        }
    }

    /**
     * 解析 files.write("文件名","内容");
     */
    private void parseWriteCommand(String afterFiles) {
        // 去掉 "write"
        String afterWrite = afterFiles.substring("write".length()).trim();
        // ("文件名","内容");
        if (!afterWrite.startsWith("(")) {
            return;
        }
        int closeParenIndex = afterWrite.indexOf(")");
        if (closeParenIndex == -1) {
            return;
        }
        // 括号内  ->  "文件名","内容"
        String inside = afterWrite.substring(1, closeParenIndex).trim();

        // 这里需要拆分成2部分：文件名、内容
        // 简单做法：找到第一个逗号，然后左右分别 stripQuotes
        int commaIndex = findMainComma(inside);
        if (commaIndex == -1) {
            return; // 没有找到逗号 -> 语法错误
        }
        String fileNameRaw = inside.substring(0, commaIndex).trim();
        String contentRaw = inside.substring(commaIndex + 1).trim();
        String fileName = stripQuotes(fileNameRaw);
        String fileContent = stripQuotes(contentRaw);

        boolean result = fileManager.writeFile(fileName, fileContent);
        if (result) {
           // System.out.println("[Files] Wrote to file: " + fileName);
        } else {
           // System.out.println("[Files] Failed to write to file: " + fileName);
        }
    }

    /**
     * 找到最外层的逗号(如果字符串中没有嵌套引号或更复杂的情况，这里可直接 .indexOf(','))
     */
    private int findMainComma(String str) {
        // 假设只会出现 files.write("abc","xyz") 这种简单情况
        // 直接用 str.indexOf(',') 即可
        // 如果要处理更复杂(例如逗号出现在引号里)，需要更严格的解析
        return str.indexOf(',');
    }

    /**
     * 去掉字符串两端的双引号
     */
    private String stripQuotes(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
