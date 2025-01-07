package fandmc.cubescript.utils;

import fandmc.cubescript.Main;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private final File baseFolder;

    public FileManager() {
        // 在插件 data folder 下创建一个 scripts 目录
        this.baseFolder = new File(Main.getInstance().getDataFolder(), "scripts");
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
        }
    }

    /**
     * 列出当前 scripts 文件夹下的所有文件名称
     */
    public List<String> listFiles() {
        List<String> result = new ArrayList<>();
        File[] files = baseFolder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    result.add(f.getName());
                }
            }
        }
        return result;
    }

    /**
     * 创建一个新文件
     */
    public boolean createFile(String fileName) {
        try {
            File file = new File(baseFolder, fileName);
            if (file.exists()) {
                return false; // 已经存在了
            }
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除一个文件
     */
    public boolean deleteFile(String fileName) {
        File file = new File(baseFolder, fileName);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 写入文件(覆盖写入)
     * 支持 \n 换行符，可以在游戏内命令把文字传进来，然后写入到文件里
     */
    public boolean writeFile(String fileName, String content) {
        File file = new File(baseFolder, fileName);
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {
            // 把 \n 替换成真正的换行
            String realContent = content.replace("\\n", System.lineSeparator());
            writer.write(realContent);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 读取并返回文件内容
     */
    public List<String> readFile(String fileName) {
        List<String> lines = new ArrayList<>();
        File file = new File(baseFolder, fileName);
        if (!file.exists() || !file.isFile()) {
            return lines;
        }
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
