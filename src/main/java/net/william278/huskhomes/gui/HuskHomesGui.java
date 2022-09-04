package net.william278.huskhomes.gui;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class HuskHomesGui extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register the event listener
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        // Log to console
        getLogger().log(Level.INFO, "Successfully enabled HuskHomes v" + getDescription().getVersion());
    }

}
