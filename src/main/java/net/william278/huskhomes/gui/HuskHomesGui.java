package net.william278.huskhomes.gui;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class HuskHomesGui extends JavaPlugin {
    private static HuskHomesGui plugin;

    @Override
    public void onEnable() {
        plugin = this;
        plugin.saveDefaultConfig();
        plugin.getConfig();

        // 注册指令
        plugin.getCommand("huskhomesgui").setExecutor(new Command());

        // Register the event listener
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        // Log to console
        getLogger().log(Level.INFO, "Successfully enabled HuskHomes v" + getDescription().getVersion());
    }


    public static HuskHomesGui getInstance() {
        return plugin;
    }

}
