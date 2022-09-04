package net.william278.huskhomes.gui;

import net.william278.huskhomes.event.HomeListEvent;
import net.william278.huskhomes.event.WarpListEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class EventListener implements Listener {

    private final HuskHomesGui plugin;

    protected EventListener(@NotNull HuskHomesGui plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHomeListView(@NotNull HomeListEvent event) {
        event.setCancelled(true);
        SavedPositionMenu.create(plugin, event.getHomes(),
                        event.getIsPublicHomeList() ? "Public Homes" : event.getOnlineUser().username + "'s Homes")
                .show(event.getOnlineUser());
    }

    @EventHandler
    public void onWarpListView(@NotNull WarpListEvent event) {
        event.setCancelled(true);
        SavedPositionMenu.create(plugin, event.getWarps(), "Warps")
                .show(event.getOnlineUser());
    }

}
