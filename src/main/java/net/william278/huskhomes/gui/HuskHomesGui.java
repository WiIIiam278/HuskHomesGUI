package net.william278.huskhomes.gui;

import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.william278.desertwell.Version;
import net.william278.huskhomes.gui.command.HuskHomesGuiCommand;
import net.william278.huskhomes.gui.config.Locales;
import net.william278.huskhomes.gui.config.Settings;
import net.william278.huskhomes.gui.listener.ListListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Level;

public class HuskHomesGui extends JavaPlugin implements HuskHomesGuiPlugin {
    private BukkitAudiences adventure;
    private Settings settings;
    private Locales locales;

    public void onEnable() {
        // Load audiences
        this.adventure = BukkitAudiences.create(this);

        // Load settings and locales
        this.reloadConfigFiles();

        // Register event listener and command
        getServer().getPluginManager().registerEvents(new ListListener(this), this);
        Objects.requireNonNull(getCommand("huskhomesgui")).setExecutor(new HuskHomesGuiCommand(this));

        // Log to console
        getLogger().log(Level.INFO, "Successfully enabled HuskHomes v" + getDescription().getVersion());
    }

    public void reloadConfigFiles() {
        this.settings = loadSettings();
        this.locales = loadLocales();
    }

    @Override
    @NotNull
    public AudienceProvider getAudiences() {
        return adventure;
    }

    @Override
    @NotNull
    public Version getPluginVersion() {
        return Version.fromString(getDescription().getVersion());
    }

    @Override
    @NotNull
    public Settings getSettings() {
        return settings;
    }

    @Override
    @NotNull
    public Locales getLocales() {
        return locales;
    }
}
