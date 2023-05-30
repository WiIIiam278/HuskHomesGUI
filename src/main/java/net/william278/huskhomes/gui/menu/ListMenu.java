package net.william278.huskhomes.gui.menu;

import de.themoep.inventorygui.*;
import net.william278.huskhomes.gui.HuskHomesGui;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.teleport.TeleportationException;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.bukkit.Bukkit.getLogger;

/**
 * A menu for displaying a list of saved positions
 */
public class ListMenu<T extends SavedPosition> extends Menu {

    private static final String EDIT_HOME_PERMISSION = "huskhomes.command.edithome";
    private static final String EDIT_HOME_OTHER_PERMISSION = "huskhomes.command.edithome.other";
    private static final String EDIT_WARP_PERMISSION = "huskhomes.command.editwarp";
    private final List<T> positions;
    private final Type type;
    private final int pageNumber = 1;

    @NotNull
    public static ListMenu<Home> homes(@NotNull HuskHomesGui plugin, @NotNull List<Home> homes, @NotNull User owner) {
        return new ListMenu<>(plugin, homes, Type.HOME,
                plugin.getLocales().getLocale("homes_menu_title", owner.getUsername()));
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
                                // teleport
                                this.close(user);
                                this.destroy();

                                try {
                                    api.teleportBuilder(user)
                                            .target(position)
                                            .toTimedTeleport()
                                            .execute();
                                } catch (TeleportationException ignored) {
                                }
                                return true;
                            }

                            // Update the icon with the item on the cursor
                            if (!player.hasPermission(EDIT_HOME_PERMISSION)
                                    && !player.hasPermission(EDIT_HOME_OTHER_PERMISSION)) {
                                return true;
                            }
                            setPositionMaterial(position, newItem.getType());
//                            player.sendMessage(plugin.getLocales().getLocale("updated_icon", position.getName()));
                            click.getGui().draw();
                        }

                        case RIGHT, DROP -> { // DROP: geyser player throw item
                            switch (type) {
                                case WARP -> {
                                    if (!player.hasPermission(EDIT_WARP_PERMISSION)) {
                                        return true;
                                    }
                                }
                                case PUBLIC_HOME, HOME -> {
                                    if (position instanceof Home home) {
                                        if (!player.hasPermission(EDIT_HOME_PERMISSION)) {
                                            return true;
                                        }
                                        if (!player.getUniqueId().equals(home.getOwner().getUuid())
                                                && !player.hasPermission(EDIT_HOME_OTHER_PERMISSION)) {
                                            return true;
                                        }
                                    }
                                }
                            }
                            if (position instanceof Home home) {
                                EditMenu.home(plugin, home, (ListMenu<Home>) this, getPageNumber(user)).show(user);
                            } else if (position instanceof Warp warp) {
                                EditMenu.warp(plugin, warp, (ListMenu<Warp>) this, getPageNumber(user)).show(user);
                            }
                        }
                    }
                }
                return true;
            },

            // home name
            plugin.getLocales().getLocale("item_name", position.getName()),

            // description
            (!position.getMeta().getDescription().isBlank() ?
                    plugin.getLocales().getLocale("item_description", position.getMeta().getDescription())
                    : plugin.getLocales().getLocale("item_description_blank")),

            // player name
            (position instanceof Home home ?
                    type == Type.PUBLIC_HOME ?
                            plugin.getLocales().getLocale("home_owner_name", home.getOwner().getUsername())
                            : ""
                    : ""),

            // item_controls
            (plugin.getSettings().menu_displayOperationHelpInItem() ?
                    plugin.getLocales().getLocale("item_controls")
                    : "")
        ));
    }

}