package fandmc.cubescript;

import fandmc.cubescript.commands.CjsCommand;
import fandmc.cubescript.commands.CjsTabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("--------CubeScript--------");
        getLogger().info("启动中......");
        Objects.requireNonNull(getCommand("cubescript")).setExecutor(new CjsCommand(this));
        Objects.requireNonNull(getCommand("cubescript")).setTabCompleter(new CjsTabCompleter());

        getLogger().info("CubeScript启动成功");
    }

    @Override
    public void onDisable() {
        getLogger().info("--------CubeScript--------");
        getLogger().info("正在禁用中");
    }

    public static Main getInstance() {
        return instance;
    }
}
