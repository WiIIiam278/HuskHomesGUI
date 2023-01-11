package net.william278.huskhomes.gui;
import net.william278.huskhomes.event.HomeListEvent;
import net.william278.huskhomes.event.WarpListEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import static net.william278.huskhomes.gui.Util.getMessageFromConfig;

public class EventListener implements Listener {

    private final HuskHomesGui plugin;
    protected EventListener(@NotNull HuskHomesGui plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onHomeListView(@NotNull HomeListEvent event) {
        event.setCancelled(true);
        SavedPositionMenu.create(plugin,
                event.getHomes(),
                event.getIsPublicHomeList() ? SavedPositionMenu.MenuType.PUBLIC_HOME : SavedPositionMenu.MenuType.HOME,
                event.getIsPublicHomeList() ? getMessageFromConfig("menu.title.PUBLIC_HOME")
                                            : getMessageFromConfig("menu.title.HOME").replace("%1%", event.getOnlineUser().username))
            .show(event.getOnlineUser());
    }

    @EventHandler
    public void onWarpListView(@NotNull WarpListEvent event) {
        event.setCancelled(true);
        SavedPositionMenu.create(plugin, event.getWarps(), SavedPositionMenu.MenuType.WARP, getMessageFromConfig("menu.title.WARP"))
                .show(event.getOnlineUser());
    }

}