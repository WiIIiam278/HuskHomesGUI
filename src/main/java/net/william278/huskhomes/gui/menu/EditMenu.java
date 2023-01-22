package net.william278.huskhomes.gui.menu;

import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import net.wesjd.anvilgui.AnvilGUI;
import net.william278.huskhomes.gui.HuskHomesGui;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.position.Warp;
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

    @NotNull
    private static String[] getEditMenuLayout() {
        return new String[]{
                "aa     ab",
                "aundip ra",
                "aa     aa"
        };
    }

    private EditMenu(@NotNull HuskHomesGui plugin, @NotNull T position, @NotNull ListMenu<T> parentMenu) {
        super(plugin, plugin.getLocales().getLocale(
                        position instanceof Home ? "home_editor_title" : "warp_editor_title", position.meta.name),
                getEditMenuLayout());
        this.type = position instanceof Home ? Type.HOME : Type.WARP;
        this.position = position;
        this.parentMenu = parentMenu;
    }

    public static EditMenu<Home> home(@NotNull HuskHomesGui plugin, @NotNull Home home,
                                      @NotNull ListMenu<Home> parentMenu) {
        return new EditMenu<>(plugin, home, parentMenu);
    }

    public static EditMenu<Warp> warp(@NotNull HuskHomesGui plugin, @NotNull Warp warp,
                                      @NotNull ListMenu<Warp> parentMenu) {
        return new EditMenu<>(plugin, warp, parentMenu);
    }

    @Override
    protected Consumer<InventoryGui> buildMenu() {
        return (menu) -> {
            final ItemStack positionIcon = new ItemStack(getPositionMaterial(position)
                    .orElse(plugin.getSettings().getDefaultIcon()));
            menu.setCloseAction(i -> false);

            // Filler icons
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
                            player.performCommand(switch (type) {
                                case HOME, PUBLIC_HOME ->
                                        "huskhomes:edithome " + ((Home) position).owner.username + "." + position.meta.name + " relocate";
                                case WARP -> "huskhomes:editwarp " + position.meta.name + " relocate";
                            });
                        }
                        return true;
                    },
                    plugin.getLocales().getLocale("edit_location_button")));

            // Editing name (Via anvil)
            menu.addElement(new StaticGuiElement('n',
                    new ItemStack(plugin.getSettings().getEditorEditNameButtonIcon()),
                    (click) -> {
                        if (click.getWhoClicked() instanceof Player player) {
                            this.close(api.adaptUser(player));
                            new AnvilGUI.Builder()
                                    .title(plugin.getLocales().getLocale("edit_name_title", position.meta.name))
                                    .itemLeft(new ItemStack(positionIcon))
                                    .text(position.meta.name)
                                    .onClose(playerInAnvil -> this.show(api.adaptUser(player)))
                                    .onComplete((completion) -> {
                                        if (completion.getText() != null) {
                                            if (position instanceof Home home) {
                                                player.performCommand("huskhomes:edithome " + home.owner.username + "." + position.meta.name + " rename " + completion.getText());
                                            } else if (position instanceof Warp) {
                                                player.performCommand("huskhomes:editwarp " + position.meta.name + " rename " + completion.getText());
                                            }
                                            // Update in the menu again (fixes https://github.com/ApliNi/HuskHomesGUI/issues/5)
                                            setPositionMaterial(position, positionIcon.getType());
                                        }
                                        position.meta.description = completion.getText();
                                        this.show(api.adaptUser(player));
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
                                    .title(plugin.getLocales().getLocale("edit_description_title", position.meta.name))
                                    .itemLeft(new ItemStack(positionIcon))
                                    .text(position.meta.description)
                                    .onClose(playerInAnvil -> this.show(api.adaptUser(player)))
                                    .onComplete((completion) -> {
                                        if (completion.getText() != null) {
                                            if (position instanceof Home) {
                                                player.performCommand("huskhomes:edithome " + ((Home) position).owner.username + "." + position.meta.name + " description " + completion.getText());
                                            } else if (position instanceof Warp) {
                                                player.performCommand("huskhomes:editwarp " + position.meta.name + " description " + completion.getText());
                                            }
                                            // Update in the menu again (fixes https://github.com/ApliNi/HuskHomesGUI/issues/5)
                                            setPositionMaterial(position, positionIcon.getType());
                                        }
                                        position.meta.description = completion.getText();
                                        this.show(api.adaptUser(player));
                                        return List.of();
                                    })
                                    .plugin(plugin)
                                    .open(player);
                        }
                        return true;
                    },
                    plugin.getLocales().getLocale("edit_description_button")));

            // Editing home privacy
            if (position instanceof Home home) {
                menu.addElement(new StaticGuiElement('p',
                        new ItemStack(plugin.getSettings().getEditorEditPrivacyButtonIcon()),
                        (click) -> {
                            if (click.getWhoClicked() instanceof Player player) {
                                player.performCommand("huskhomes:edithome " + home.owner.username + "." + position.meta.name + " privacy");
                            }
                            return true;
                        },
                        plugin.getLocales().getLocale("edit_privacy_button")));
            }

            // Deleting
            menu.addElement(new StaticGuiElement('r',
                    new ItemStack(plugin.getSettings().getEditorDeleteButtonIcon()),
                    (click) -> {
                        if (click.getWhoClicked() instanceof Player player) {
                            this.close(api.adaptUser(player));
                            if (position instanceof Home home) {
                                player.performCommand("huskhomes:delhome " + home.owner.username + "." + position.meta.name);
                            } else if (position instanceof Warp warp) {
                                player.performCommand("huskhomes:delwarp " + warp.meta.name);
                            }
                        }
                        return true;
                    },
                    plugin.getLocales().getLocale("delete_button")));

            // Controls display
            menu.addElement(new StaticGuiElement('i',
                    new ItemStack(Material.OAK_SIGN),
                    plugin.getLocales().getLocale("item_info_name", position.meta.name),
                    plugin.getLocales().getLocale("item_info_description", position.meta.description),
                    plugin.getLocales().getLocale("item_info_world", position.world.name),
                    plugin.getLocales().getLocale("item_info_server", position.server.name),
                    plugin.getLocales().getLocale("item_info_coordinates",
                            Integer.toString((int) Math.floor(position.x)),
                            Integer.toString((int) Math.floor(position.y)),
                            Integer.toString((int) Math.floor(position.z))),
                    position instanceof Home home ? plugin.getLocales()
                            .getLocale("home_owner_name", home.owner.username) : ""
            ));
        };
    }

}
