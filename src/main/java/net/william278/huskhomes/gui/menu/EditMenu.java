package net.william278.huskhomes.gui.menu;

import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import net.wesjd.anvilgui.AnvilGUI;
import net.william278.huskhomes.gui.HuskHomesGui;
import net.william278.huskhomes.user.OnlineUser;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
import net.william278.huskhomes.util.ValidationException;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * A menu for editing a saved position
 */
public class EditMenu<T extends SavedPosition> extends Menu {

    private final T position;
    private final Type type;
    private final ListMenu<T> parentMenu;
    private final int pageNumber;

    @NotNull
    private static String[] getEditMenuLayout() {
        return new String[]{
                "aa     ab",
                "aundip ra",
                "aa     aa"
        };
    }

    private EditMenu(@NotNull HuskHomesGui plugin, @NotNull T position, @NotNull ListMenu<T> parentMenu, int pageNumber) {
        super(plugin, plugin.getLocales().getLocale(
                        position instanceof Home ? "home_editor_title" : "warp_editor_title", position.getName()),
                getEditMenuLayout());
        this.type = position instanceof Home ? Type.HOME : Type.WARP;
        this.position = position;
        this.parentMenu = parentMenu;
        this.pageNumber = pageNumber;
    }

    public static EditMenu<Home> home(@NotNull HuskHomesGui plugin, @NotNull Home home,
                                      @NotNull ListMenu<Home> parentMenu,
                                      int pageNumber) {
        return new EditMenu<>(plugin, home, parentMenu, pageNumber);
    }

    public static EditMenu<Warp> warp(@NotNull HuskHomesGui plugin, @NotNull Warp warp,
                                      @NotNull ListMenu<Warp> parentMenu,
                                      int pageNumber) {
        return new EditMenu<>(plugin, warp, parentMenu, pageNumber);
    }

    @Override
    protected Consumer<InventoryGui> buildMenu() {
        return (menu) -> {
            final ItemStack positionIcon = new ItemStack(getPositionMaterial(position)
                    .orElse(plugin.getSettings().getDefaultIcon()));
            menu.setCloseAction(i -> false);

            // Filler background icons
            menu.addElement(new StaticGuiElement('a',
                    new ItemStack(switch (type) {
                        case HOME, PUBLIC_HOME -> plugin.getSettings().getHomeEditorFillerIcon();
                        case WARP -> plugin.getSettings().getWarpEditorFillerIcon();
                    }),
                    " "
            ));

            // Return to the parent list menu
            menu.addElement(new StaticGuiElement('b',
                    new ItemStack(plugin.getSettings().getEditorBackButtonIcon()),
                    (click) -> {
                        if (click.getWhoClicked() instanceof Player player) {
                            final OnlineUser user = api.adaptUser(player);
                            this.close(user);
                            parentMenu.show(user);
                            parentMenu.setPageNumber(user, pageNumber);
                            this.destroy();
                        }
                        return true;
                    },
                    plugin.getLocales().getLocale("back_button")));

            // Relocating
            menu.addElement(new StaticGuiElement('u',
                    new ItemStack(plugin.getSettings().getEditorEditLocationButtonIcon()),
                    (click) -> {
                        if (click.getWhoClicked() instanceof Player player) {
                            try {
                                if (position instanceof Home home) {
                                    api.relocateHome(home, api.adaptUser(player).getPosition());
                                } else if (position instanceof Warp warp) {
                                    api.relocateWarp(warp, api.adaptUser(player).getPosition());
                                }
                            } catch (ValidationException e) {
                                return true;
                            }
                        }
                        return true;
                    },
                    plugin.getLocales().getLocale("edit_location_button"),
                    plugin.getLocales().getLocale("edit_location_default_message",
                            Integer.toString((int) Math.floor(position.getX())),
                            Integer.toString((int) Math.floor(position.getY())),
                            Integer.toString((int) Math.floor(position.getZ())))));

            // Editing name (Via anvil)
            menu.addElement(new StaticGuiElement('n',
                    new ItemStack(plugin.getSettings().getEditorEditNameButtonIcon()),
                    (click) -> {
                        if (click.getWhoClicked() instanceof Player player) {
                            this.close(api.adaptUser(player));
                            new AnvilGUI.Builder()
                                    .title(plugin.getLocales().getLocale("edit_name_title", position.getName()))
                                    .itemLeft(new ItemStack(positionIcon))
                                    .text(position.getName())
                                    .onClose(playerInAnvil -> this.show(api.adaptUser(player)))
                                    .onComplete((completion) -> {
                                        if (completion.getText() != null) {
                                            try {
                                                if (position instanceof Home home) {
                                                    api.renameHome(home, completion.getText());
                                                } else if (position instanceof Warp warp) {
                                                    api.renameWarp(warp, completion.getText());
                                                }
                                            } catch (ValidationException e) {
                                                return List.of();
                                            }
                                        }
                                        position.getMeta().setName(completion.getText());

                                        // Refresh menu title
                                        this.close(api.adaptUser(player));
                                        this.destroy();
                                        new EditMenu<>(plugin, position, parentMenu, pageNumber).show(api.adaptUser(player));
                                        return List.of();
                                    })
                                    .plugin(plugin)
                                    .open(player);
                        }
                        return true;
                    },
                    plugin.getLocales().getLocale("edit_name_button")));

            // Editing description (Via anvil)
            menu.addElement(new StaticGuiElement('d',
                    new ItemStack(plugin.getSettings().getEditorEditDescriptionButtonIcon()),
                    (click) -> {
                        if (click.getWhoClicked() instanceof Player player) {
                            this.close(api.adaptUser(player));
                            new AnvilGUI.Builder()
                                    .title(plugin.getLocales().getLocale("edit_description_title", position.getName()))
                                    .itemLeft(new ItemStack(positionIcon))
                                    // description or default_description
                                    .text(!position.getMeta().getDescription().isBlank() ?
                                            position.getMeta().getDescription()
                                            : plugin.getLocales().getLocale("edit_description_default_input"))
                                    .onClose(playerInAnvil -> this.show(api.adaptUser(player)))
                                    .onComplete((completion) -> {
                                        if (completion.getText() != null) {
                                            try {
                                                if (position instanceof Home home) {
                                                    api.setHomeDescription(home, completion.getText());
                                                } else if (position instanceof Warp warp) {
                                                    api.setWarpDescription(warp, completion.getText());
                                                }
                                            } catch (ValidationException e) {
                                                return List.of();
                                            }
                                        }
                                        position.getMeta().setDescription(completion.getText());
                                        this.show(api.adaptUser(player));
                                        return List.of();
                                    })
                                    .plugin(plugin)
                                    .open(player);
                        }
                        return true;
                    },
                    plugin.getLocales().getLocale("edit_description_button"),

                    // description
                    (!position.getMeta().getDescription().isBlank() ?
                            plugin.getLocales().getLocale("edit_description_default_message", position.getMeta().getDescription())
                            : plugin.getLocales().getLocale("edit_description_default_message_blank"))));

            // Editing home privacy
            if (position instanceof Home home) {
                menu.addElement(new StaticGuiElement('p',
                        new ItemStack(plugin.getSettings().getEditorEditPrivacyButtonIcon()),
                        (click) -> {
                            if (click.getWhoClicked() instanceof Player player) {
                                try {
                                    api.setHomePrivacy(home, !home.isPublic());
                                    // Update the status display on the menu
                                    home.setPublic(!home.isPublic());
                                    this.show(api.adaptUser(player));
                                } catch (ValidationException e) {
                                    return true;
                                }
                            }
                            return true;
                        },
                        plugin.getLocales().getLocale("edit_privacy_button"),
                        // public or private
                        plugin.getLocales().getLocale("edit_privacy_message", (home.isPublic() ?
                                plugin.getLocales().getLocale("edit_privacy_message_public")
                                : plugin.getLocales().getLocale("edit_privacy_message_private")))));
            }

            // Deleting
            menu.addElement(new StaticGuiElement('r',
                    new ItemStack(plugin.getSettings().getEditorDeleteButtonIcon()),
                    (click) -> {
                        switch (click.getType()) {
                            case RIGHT, DROP -> { // DROP: geyser player throw item
                                if (click.getWhoClicked() instanceof Player player) {
                                    this.close(api.adaptUser(player));
                                    try {
                                        if (position instanceof Home home) {
                                            api.deleteHome(home);
                                            home.getMeta().setName(plugin.getLocales().getLocale("item_deleted_name", home.getName())); // update listMenu
                                        } else if (position instanceof Warp warp) {
                                            api.deleteWarp(warp);
                                            warp.getMeta().setName(plugin.getLocales().getLocale("item_deleted_name", warp.getName())); // update listMenu
                                        }
                                    } catch (ValidationException e) {
                                        return true;
                                    }

                                    // Return to the parent list menu
                                    final OnlineUser user = api.adaptUser(player);
                                    this.close(user);
                                    parentMenu.show(user);
                                    parentMenu.setPageNumber(user, pageNumber);
                                    this.destroy();
                                }
                            }
                        }
                        return true;
                    },
                    plugin.getLocales().getLocale("delete_button"),
                    plugin.getLocales().getLocale("delete_button_describe")
            ));

            // Controls display
            menu.addElement(new StaticGuiElement('i',
                    new ItemStack(Material.OAK_SIGN),
                    // name
                    plugin.getLocales().getLocale("item_info_name", position.getName()),
                    // description
                    (!position.getMeta().getDescription().isBlank() ?
                            plugin.getLocales().getLocale("item_info_description", position.getMeta().getDescription())
                            : plugin.getLocales().getLocale("item_info_description_blank")),
                    // world name
                    plugin.getLocales().getLocale("item_info_world", position.getWorld().getName()),
                    // server
                    plugin.getLocales().getLocale("item_info_server", position.getServer()),
                    // xyz
                    plugin.getLocales().getLocale("item_info_coordinates",
                            Integer.toString((int) Math.floor(position.getX())),
                            Integer.toString((int) Math.floor(position.getY())),
                            Integer.toString((int) Math.floor(position.getZ()))),
                    // by playerName
                    position instanceof Home home ? plugin.getLocales()
                            .getLocale("home_owner_name", home.getOwner().getUsername()) : ""
            ));
        };
    }

}
