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
                            final OnlineUser onlineUser = api.adaptUser(player);
                            try {
                                if (position instanceof Home home) {
                                    player.performCommand("huskhomes:edithome " + ((Home) position).getOwner().getUsername() +"."+ position.getName() +" relocate");
//                                    api.relocateHome(home, onlineUser.getPosition());
                                } else if (position instanceof Warp warp) {
                                    player.performCommand("huskhomes:editwarp " + position.getName() +" relocate");
//                                    api.relocateWarp(warp, onlineUser.getPosition());
                                }
                            } catch (ValidationException e) {
                                return true;
                            }
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
                                    .title(plugin.getLocales().getLocale("edit_name_title", position.getName()))
                                    .itemLeft(new ItemStack(positionIcon))
                                    .text(position.getName())
                                    .onClose(playerInAnvil -> this.show(api.adaptUser(player)))
                                    .onComplete((completion) -> {
                                        if (completion.getText() != null) {
                                            try {
                                                if (position instanceof Home) {
                                                    player.performCommand("huskhomes:edithome " + ((Home) position).getOwner().getUsername() +"."+ position.getName() +" rename "+ completion.getText());
//                                                    api.renameHome(home, completion.getText());
                                                } else if (position instanceof Warp) {
                                                    player.performCommand("huskhomes:editwarp " + position.getName() +" description "+ completion.getText());
//                                                    api.renameWarp(warp, completion.getText());
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
                                    .text(position.getMeta().getDescription())
                                    .onClose(playerInAnvil -> this.show(api.adaptUser(player)))
                                    .onComplete((completion) -> {
                                        if (completion.getText() != null) {
                                            try {
                                                if (position instanceof Home) {
                                                    player.performCommand("huskhomes:edithome " + ((Home) position).getOwner().getUsername() +"."+ position.getName() +" description "+ completion.getText());
//                                                    api.setHomeDescription(home, completion.getText());
                                                } else if (position instanceof Warp) {
                                                    player.performCommand("huskhomes:editwarp " + position.getName() +" description "+ completion.getText());
//                                                    api.setWarpDescription(warp, completion.getText());
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
                    plugin.getLocales().getLocale("edit_description_button")));

            // Editing home privacy
            if (position instanceof Home home) {
                menu.addElement(new StaticGuiElement('p',
                        new ItemStack(plugin.getSettings().getEditorEditPrivacyButtonIcon()),
                        (click) -> {
                            if (click.getWhoClicked() instanceof Player player) {
                                try {
                                    String not_isPublic = home.isPublic()? "private" : "public";
                                    player.performCommand("huskhomes:edithome " + home.getOwner().getUsername() +"."+ home.getName() +" privacy "+ not_isPublic);
                                    home.setPublic(!home.isPublic()); // Change the data here, not the database // The player may click the button repeatedly
//                                    api.setHomePrivacy(home, !home.isPublic());
                                } catch (ValidationException e) {
                                    return true;
                                }
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
                            try {
                                if (position instanceof Home) {
                                    player.performCommand("huskhomes:delhome " + ((Home) position).getOwner().getUsername() + "." + position.getName());
//                                    api.deleteHome(home);
                                } else if (position instanceof Warp) {
                                    player.performCommand("huskhomes:delwarp " + position.getName());
//                                    api.deleteWarp(warp);
                                }
                            } catch (ValidationException e) {
                                return true;
                            }
                        }
                        return true;
                    },
                    plugin.getLocales().getLocale("delete_button")));

            // Controls display
            menu.addElement(new StaticGuiElement('i',
                    new ItemStack(Material.OAK_SIGN),
                    plugin.getLocales().getLocale("item_info_name", position.getName()),
                    plugin.getLocales().getLocale("item_info_description", position.getMeta().getDescription()),
                    plugin.getLocales().getLocale("item_info_world", position.getWorld().getName()),
                    plugin.getLocales().getLocale("item_info_server", position.getServer()),
                    plugin.getLocales().getLocale("item_info_coordinates",
                            Integer.toString((int) Math.floor(position.getX())),
                            Integer.toString((int) Math.floor(position.getY())),
                            Integer.toString((int) Math.floor(position.getZ()))),
                    position instanceof Home home ? plugin.getLocales()
                            .getLocale("home_owner_name", home.getOwner().getUsername()) : ""
            ));
        };
    }

}
