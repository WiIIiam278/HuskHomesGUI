package net.william278.huskhomes.gui.menu;

import de.themoep.inventorygui.*;
import net.william278.huskhomes.gui.HuskHomesGui;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.util.Permission;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * A menu for displaying a list of saved positions
 */
public class ListMenu<T extends SavedPosition> extends Menu {

    private final List<T> positions;
    private final Type type;
    private final int pageNumber = 1;

    @NotNull
    public static ListMenu<Home> homes(@NotNull HuskHomesGui plugin, @NotNull List<Home> homes, @NotNull User owner) {
        return new ListMenu<>(plugin, homes, Type.HOME,
                plugin.getLocales().getLocale("homes_menu_title", owner.username));
    }

    @NotNull
    public static ListMenu<Home> publicHomes(@NotNull HuskHomesGui plugin, @NotNull List<Home> homes) {
        return new ListMenu<>(plugin, homes, Type.PUBLIC_HOME,
                plugin.getLocales().getLocale("public_homes_menu_title"));
    }

    @NotNull
    public static ListMenu<Warp> warps(@NotNull HuskHomesGui plugin, @NotNull List<Warp> warps) {
        return new ListMenu<>(plugin, warps, Type.WARP,
                plugin.getLocales().getLocale("warps_menu_title"));
    }

    private ListMenu(@NotNull HuskHomesGui plugin, @NotNull List<T> positions, @NotNull ListMenu.Type type, @NotNull String title) {
        super(plugin, title, getMenuLayout(plugin));
        this.positions = positions;
        this.type = type;
    }

    @NotNull
    private static String[] getMenuLayout(@NotNull HuskHomesGui plugin) {
        return Arrays.copyOfRange(new String[]{
                        "ppppppppp",
                        "ppppppppp",
                        "ppppppppp",
                        "ppppppppp",
                        "ppppppppp",
                        "bl  i  ne"},
                6 - plugin.getSettings().getMenuSize(), 6);
    }

    @Override
    protected Consumer<InventoryGui> buildMenu() {
        return (menu) -> {
            // Add filler items
            menu.setFiller(new ItemStack(type.getFillerMaterial(plugin.getSettings()), 1));

            // Add pagination handling
            menu.addElement(getPositionGroup(plugin, positions));
            menu.addElement(new GuiPageElement('b',
                    new ItemStack(plugin.getSettings().getPaginateFirstPage()),
                    GuiPageElement.PageAction.FIRST,
                    plugin.getLocales().getLocale("pagination_first_page")));
            menu.addElement(new GuiPageElement('l',
                    new ItemStack(plugin.getSettings().getPaginatePreviousPage()),
                    GuiPageElement.PageAction.PREVIOUS,
                    plugin.getLocales().getLocale("pagination_previous_page")));
            menu.addElement(new GuiPageElement('n',
                    new ItemStack(plugin.getSettings().getPaginateNextPage()),
                    GuiPageElement.PageAction.NEXT,
                    plugin.getLocales().getLocale("pagination_next_page")));
            menu.addElement(new GuiPageElement('e',
                    new ItemStack(plugin.getSettings().getPaginateLastPage()),
                    GuiPageElement.PageAction.LAST,
                    plugin.getLocales().getLocale("pagination_last_page")));
            menu.setPageNumber(pageNumber);

            // Add controls information
            if (plugin.getSettings().doShowMenuControls()) {
                menu.addElement(new StaticGuiElement('i',
                        new ItemStack(plugin.getSettings().getControlsIcon()),
                        plugin.getLocales().getLocale("menu_controls_title"),
                        plugin.getLocales().getLocale("menu_controls_details")));
            }
        };
    }

    // Get the GUI group of position select buttons
    @NotNull
    private GuiElementGroup getPositionGroup(@NotNull HuskHomesGui plugin, @NotNull List<T> positions) {
        final GuiElementGroup group = new GuiElementGroup('p');
        positions.forEach(position -> group.addElement(getPositionButton(plugin, position)));
        return group;
    }

    // Get a position select button for a SavedPosition
    @SuppressWarnings("unchecked")
    @NotNull
    private DynamicGuiElement getPositionButton(@NotNull HuskHomesGui plugin, @NotNull SavedPosition position) {
        return new DynamicGuiElement('e', (viewer) -> new StaticGuiElement('e',
                new ItemStack(getPositionMaterial(position).orElse(plugin.getSettings().getDefaultIcon())),
                (click) -> {
                    if (click.getWhoClicked() instanceof Player player) {
                        final OnlineUser user = api.adaptUser(player);
                        switch (click.getType()) {
                            case LEFT -> {
                                // Update the icon with the item on the cursor
                                final ItemStack newItem = player.getItemOnCursor();
                                if (newItem.getType() == Material.AIR) {
                                    this.close(user);
                                    this.destroy();
                                    api.teleportBuilder(user)
                                            .setTarget(position)
                                            .toTimedTeleport()
                                            .thenAccept(Teleport::execute);
                                    return true;
                                }

                                if (!player.hasPermission(Permission.COMMAND_EDIT_HOME.node)
                                        && !player.hasPermission(Permission.COMMAND_EDIT_HOME_OTHER.node)) {
                                    return true;
                                }
                                setPositionMaterial(position, newItem.getType()).thenRun(() -> player.sendMessage(plugin
                                        .getLocales().getLocale("updated_icon", position.meta.name)));
                                click.getGui().draw();
                            }

                            case RIGHT, DROP -> {
                                switch (type) {
                                    case WARP -> {
                                        if (!player.hasPermission(Permission.COMMAND_EDIT_WARP.node)) {
                                            return true;
                                        }
                                    }
                                    case PUBLIC_HOME, HOME -> {
                                        if (position instanceof Home home) {
                                            if (!player.hasPermission(Permission.COMMAND_EDIT_HOME.node)) {
                                                return true;
                                            }
                                            if (!player.getUniqueId().equals(home.owner.uuid)
                                                    && !player.hasPermission(Permission.COMMAND_EDIT_HOME_OTHER.node)) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                                if (position instanceof Home home) {
                                    EditMenu.home(plugin, home, (ListMenu<Home>) this).show(user);
                                } else if (position instanceof Warp warp) {
                                    EditMenu.warp(plugin, warp, (ListMenu<Warp>) this).show(user);
                                }
                            }
                        }
                    }
                    return true;
                },
                plugin.getLocales().getLocale("item_name", position.meta.name),
                (!position.meta.description.isBlank() ?
                        plugin.getLocales().getLocale("item_description", position.meta.description)
                        : plugin.getLocales().getLocale("item_description_blank")),
                (position instanceof Home home ?
                        plugin.getLocales().getLocale("home_owner_name", home.owner.username)
                        : ""),
                plugin.getLocales().getLocale("item_controls_left_click"),
                plugin.getLocales().getLocale("item_controls_right_click"),
                plugin.getLocales().getLocale("item_controls_shift_click")));
    }

}