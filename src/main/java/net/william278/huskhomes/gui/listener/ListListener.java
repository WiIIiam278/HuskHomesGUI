package net.william278.huskhomes.gui.listener;

import net.william278.huskhomes.event.HomeListEvent;
import net.william278.huskhomes.event.WarpListEvent;
import net.william278.huskhomes.gui.HuskHomesGui;
import net.william278.huskhomes.gui.menu.ListMenu;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.user.OnlineUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ListListener implements Listener {
    private final HuskHomesGui plugin;

    public ListListener(@NotNull HuskHomesGui plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHomeListView(@NotNull HomeListEvent event) {
        if (!(event.getListViewer() instanceof OnlineUser onlineUser)) {
            return;
        }

        event.setCancelled(true);
        final ListMenu<Home> menu;
        if (event.getIsPublicHomeList()) {
            menu = ListMenu.publicHomes(plugin, event.getHomes());
        } else {
            menu = ListMenu.homes(plugin, event.getHomes(),
                    event.getHomes().stream()
                            .findFirst()
                            .map(Home::getOwner)
                            .orElse(onlineUser));
        }
        menu.show(onlineUser);
    }

    @EventHandler
    public void onWarpListView(@NotNull WarpListEvent event) {
        if (!(event.getListViewer() instanceof OnlineUser onlineUser)) {
            return;
        }

        event.setCancelled(true);
        ListMenu.warps(plugin, event.getWarps()).show(onlineUser);
    }

}