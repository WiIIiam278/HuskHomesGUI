package net.william278.huskhomes.gui.menu;

import de.themoep.inventorygui.InventoryGui;
import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.gui.HuskHomesGui;
import net.william278.huskhomes.gui.config.Settings;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.user.OnlineUser;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class Menu {
    private static final String TAG_KEY = "huskhomesgui:icon";

    protected final HuskHomesGui plugin;
    protected final HuskHomesAPI api;
    private final InventoryGui gui;

    protected Menu(@NotNull HuskHomesGui plugin, @NotNull String title, @NotNull String[] layout) {
        this.plugin = plugin;
        this.api = HuskHomesAPI.getInstance();
        this.gui = new InventoryGui(plugin, title, layout);
    }

    protected abstract Consumer<InventoryGui> buildMenu();

    public final void show(@NotNull OnlineUser user) {
        buildMenu().accept(gui);
        gui.show(api.getPlayer(user));
    }

    public final void setPageNumber(@NotNull OnlineUser user, int pageNumber) {
        gui.setPageNumber(api.getPlayer(user), pageNumber);
    }

    public final int getPageNumber(@NotNull OnlineUser user) {
        return gui.getPageNumber(api.getPlayer(user));
    }

    public final void close(@NotNull OnlineUser user) {
        gui.close(api.getPlayer(user));
    }

    public final void destroy() {
        gui.destroy();
    }

    /**
     * Get the material to use for a saved position by icon tag
     *
     * @param position The saved position
     * @return The material to use if found
     */
    protected Optional<Material> getPositionMaterial(@NotNull SavedPosition position) {
        final Map<String, String> tags = position.getMeta().getTags();
        if (tags.containsKey(TAG_KEY)) {
            return Optional.ofNullable(Material.matchMaterial(tags.get(TAG_KEY)));
        }
        return Optional.empty();
    }

    /**
     * Set the material to use for a {@link SavedPosition} and update it in the database
     *
     * @param position The saved position
     * @param material The {@link Material} to use
     */
    protected void setPositionMaterial(@NotNull SavedPosition position, @NotNull Material material) {
        final Map<String, String> tags = position.getMeta().getTags();
        tags.put(TAG_KEY, material.getKey().toString());

        if (position instanceof Warp warp) {
            api.setWarpMetaTags(warp, tags);
        } else if (position instanceof Home home) {
            api.setHomeMetaTags(home, tags);
        } else {
            throw new IllegalArgumentException("Position must be a warp or home");
        }
    }

    /**
     * Represents different types of {@link SavedPosition} that a {@link ListMenu} can display
     */
    protected enum Type {
        HOME,
        PUBLIC_HOME,
        WARP;

        public Material getFillerMaterial(@NotNull Settings settings) {
            return switch (this) {
                case HOME -> settings.getHomesFillerItem();
                case PUBLIC_HOME -> settings.getPublicHomesFillerItem();
                case WARP -> settings.getWarpsFillerItem();
            };
        }
    }

}
