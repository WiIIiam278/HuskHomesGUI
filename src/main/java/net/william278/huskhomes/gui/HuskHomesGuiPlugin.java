package net.william278.huskhomes.gui;

import net.kyori.adventure.platform.AudienceProvider;
import net.william278.annotaml.Annotaml;
import net.william278.desertwell.Version;
import net.william278.huskhomes.gui.config.Locales;
import net.william278.huskhomes.gui.config.Settings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public interface HuskHomesGuiPlugin {

    @NotNull
    default Locales loadLocales() {
        try {
            return Annotaml.create(new File(getDataFolder(), "messages-" + loadSettings().getLanguage() + ".yml"),
                    Annotaml.create(Locales.class, getResource("locales/" + loadSettings().getLanguage() + ".yml")).get()).get();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | IOException e) {
            throw new IllegalStateException("Failed to load locales file", e);
        }
    }

    @NotNull
    default Settings loadSettings() {
        try {
            return Annotaml.create(new File(getDataFolder(), "config.yml"), Settings.class).get();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | IOException e) {
            throw new IllegalStateException("Failed to load config file", e);
        }
    }

    @NotNull
    AudienceProvider getAudiences();

    @NotNull
    Version getPluginVersion();

    @NotNull
    Settings getSettings();

    @NotNull
    Locales getLocales();

    File getDataFolder();

    InputStream getResource(@NotNull String fileName);

}
