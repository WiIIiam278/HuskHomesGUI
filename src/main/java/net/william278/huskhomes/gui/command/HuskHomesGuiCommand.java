package net.william278.huskhomes.gui.command;

import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.audience.Audience;
import net.william278.desertwell.AboutMenu;
import net.william278.huskhomes.gui.HuskHomesGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HuskHomesGuiCommand implements CommandExecutor, TabExecutor {
    private final HuskHomesGui plugin;
    private final AboutMenu aboutMenu;

    public HuskHomesGuiCommand(@NotNull HuskHomesGui plugin) {
        this.plugin = plugin;
        this.aboutMenu = AboutMenu.create("HuskHomesGUI")
                .withDescription("Show HuskHomes homes and warps in a simple to use menu")
                .withVersion(plugin.getPluginVersion())
                .addAttribution("Author",
                        AboutMenu.Credit.of("William278").withDescription("Click to visit website").withUrl("https://william278.net"))
                .addAttribution("Contributors",
                        AboutMenu.Credit.of("ApliNi").withDescription("Code"))
                .addButtons(
                        AboutMenu.Link.of("https://william278.net/docs/huskhomesgui").withText("About").withIcon("⛏"),
                        AboutMenu.Link.of("https://github.com/WiIIiam278/HuskHomes2/issues").withText("Issues").withIcon("❌").withColor("#ff9f0f"),
                        AboutMenu.Link.of("https://discord.gg/tVYhJfyDWG").withText("Discord").withIcon("⭐").withColor("#6773f5"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        final Audience audience;
        if (sender instanceof Player player) {
            audience = plugin.getAudiences().player(player.getUniqueId());
        } else {
            audience = plugin.getAudiences().console();
        }

        final String subCommand = args.length >= 1 ? args[0] : "";
        if (subCommand.equals("reload")) {
            plugin.reloadConfigFiles();
            audience.sendMessage(new MineDown("[[HuskHomesGUI]](#00fb9a bold) [Reloaded config files!](#00fb9a)")
                    .toComponent());
        } else {
            audience.sendMessage(aboutMenu.toMineDown().toComponent());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        return this.filter(List.of("reload", "about"), args);
    }

    @NotNull
    private List<String> filter(@NotNull List<String> list, @NotNull String[] args) {
        final List<String> filtered = new ArrayList<>();
        for (String s : list) {
            if (s.startsWith(args[args.length - 1])) {
                filtered.add(s);
            }
        }
        return filtered;
    }
}
