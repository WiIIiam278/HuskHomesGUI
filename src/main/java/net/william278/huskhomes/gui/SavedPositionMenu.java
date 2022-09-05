package net.william278.huskhomes.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import net.md_5.bungee.api.chat.BaseComponent;
import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.libraries.minedown.MineDown;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.Home;
import net.william278.huskhomes.position.SavedPosition;
import net.william278.huskhomes.util.Permission;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * A menu for displaying a list of saved positions
 */
public class SavedPositionMenu {

    private static final String[] MENU_LAYOUT = {
            "ppppppppp",
            "ppppppppp",
            "ppppppppp",
            "bl     ne",
    };

    private static final Material FILLER_MATERIAL = Material.GRAY_STAINED_GLASS_PANE;
    private final InventoryGui menu;
    private final HuskHomesAPI huskHomesAPI;
    private final MenuType menuType;

    protected static SavedPositionMenu create(@NotNull HuskHomesGui plugin,
                                              @NotNull List<? extends SavedPosition> savedPositions,
                                              @NotNull MenuType type, @NotNull String title) {
        return new SavedPositionMenu(plugin, HuskHomesAPI.getInstance(), savedPositions, type, title);
    }

    protected void show(@NotNull OnlineUser onlineUser) {
        menu.show(huskHomesAPI.getPlayer(onlineUser));
    }

    private SavedPositionMenu(@NotNull HuskHomesGui plugin, @NotNull HuskHomesAPI huskHomesAPI,
                              @NotNull List<? extends SavedPosition> positionList,
                              @NotNull MenuType menuType, @NotNull String title) {
        this.menu = new InventoryGui(plugin, title, MENU_LAYOUT);
        this.menuType = menuType;
        this.huskHomesAPI = huskHomesAPI;

        // Add filler items
        this.menu.setFiller(new ItemStack(FILLER_MATERIAL, 1));

        // Add pagination handling
        this.menu.addElement(getPositionGroup(positionList));
        this.menu.addElement(new GuiPageElement('b', new ItemStack(Material.EGG),
                GuiPageElement.PageAction.FIRST,
                "[⏪ View first page (\\1\\)](#00fb9a)"));
        this.menu.addElement(new GuiPageElement('l', new ItemStack(Material.ARROW),
                GuiPageElement.PageAction.PREVIOUS,
                "[◀ View previous page \\(%prevpage%\\)](#00fb9a)"));
        this.menu.addElement(new GuiPageElement('n', new ItemStack(Material.SPECTRAL_ARROW),
                GuiPageElement.PageAction.NEXT,
                "[View next page \\(%nextpage%\\) ▶](#00fb9a)"));
        this.menu.addElement(new GuiPageElement('e', new ItemStack(Material.EGG),
                GuiPageElement.PageAction.LAST,
                "[View last page \\(%pages\\) ⏩](#00fb9a)"));
    }

    @NotNull
    private GuiElementGroup getPositionGroup(@NotNull List<? extends SavedPosition> positions) {
        final GuiElementGroup group = new GuiElementGroup('p');
        for (SavedPosition position : positions) {
            group.addElement(getPositionButton(position));
        }
        return group;
    }

    @NotNull
    private StaticGuiElement getPositionButton(@NotNull SavedPosition position) {
        return new StaticGuiElement('e', new ItemStack(getPositionMaterial(position).orElse(Material.STONE)),
                click -> {
                    if (click.getWhoClicked() instanceof Player player) {
                        final OnlineUser onlineUser = huskHomesAPI.adaptUser(player);
                        switch (click.getType()) {
                            case LEFT -> {
                                player.closeInventory();
                                huskHomesAPI.teleportPlayer(onlineUser, position, true);
                            }
                            case RIGHT -> {
                                player.closeInventory();
                                player.performCommand(switch (menuType) {
                                    case HOME, PUBLIC_HOME ->
                                            "huskhomes:edithome " + ((Home) position).owner.username + "." + position.meta.name;
                                    case WARP -> "huskhomes:editwarp " + position.meta.name;
                                });
                            }
                            case SHIFT_LEFT -> {
                                player.closeInventory();
                                if (canEditPosition(position, player)) {
                                    setPositionMaterial(position, player.getInventory().getItemInMainHand().getType());
                                }
                            }
                        }
                    }
                    return true;
                },
                getLegacyText("[" + position.meta.name + "](#00fb9a)"),
                getLegacyText("&7ℹ" + (position.meta.description.isBlank()
                        ? getLocale("item_no_description") : position.meta.description)));
    }

    /**
     * Get the material to use for a saved position by icon tag
     *
     * @param position The saved position
     * @return The material to use if found
     */
    private Optional<Material> getPositionMaterial(@NotNull SavedPosition position) {
        final String TAG_KEY = "huskhomesgui:icon";
        if (position.meta.tags.containsKey(TAG_KEY)) {
            return Optional.ofNullable(Material.getMaterial(position.meta.tags.get(TAG_KEY)));
        }
        return Optional.empty();
    }

    private void setPositionMaterial(@NotNull SavedPosition position, @NotNull Material material) {
        final String TAG_KEY = "huskhomesgui:icon";
        position.meta.tags.put(TAG_KEY, material.getKey().toString());
//todo
        //        huskHomesAPI.saveHome(position).thenAccept(success -> {
//
//        });
    }

    /**
     * Get if a {@link Player} can edit a {@link SavedPosition}
     *
     * @param position The saved position
     * @param player   The player
     * @return {@code true} if the player can edit the position
     */
    private boolean canEditPosition(@NotNull SavedPosition position, @NotNull Player player) {
        // Validate warp permission checks
        if (menuType == MenuType.WARP) {
            if (!player.hasPermission(Permission.COMMAND_EDIT_WARP.node)) {
                getLocale("error_no_permission").ifPresent(player::sendMessage);
                return false;
            }
            return true;
        }

        // Validate home permission checks
        if (menuType == MenuType.HOME || menuType == MenuType.PUBLIC_HOME) {
            final Home home = (Home) position;
            if (player.getUniqueId().equals(home.owner.uuid)) {
                if (!player.hasPermission(Permission.COMMAND_EDIT_HOME.node)) {
                    getLocale("error_no_permission").ifPresent(player::sendMessage);
                    return false;
                }
            } else {
                if (!player.hasPermission(Permission.COMMAND_EDIT_HOME_OTHER.node)) {
                    getLocale("error_no_permission").ifPresent(player::sendMessage);
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Gets a locale as legacy text from HuskHomes
     *
     * @param localeKey    The locale key
     * @param replacements The replacements to use
     * @return The legacy text
     */
    @NotNull
    private Optional<String> getLocale(@NotNull String localeKey, String... replacements) {
        return huskHomesAPI.getLocale(localeKey, replacements)
                .map(MineDown::toComponent)
                .map(BaseComponent::toLegacyText);
    }

    /**
     * Convert MineDown formatted text into legacy text
     *
     * @param mineDown The MineDown formatted text
     * @return The legacy text
     */
    @NotNull
    private String getLegacyText(@NotNull String mineDown) {
        return BaseComponent.toLegacyText(new MineDown(mineDown).toComponent());
    }

    protected enum MenuType {
        HOME,
        PUBLIC_HOME,
        WARP
    }

}
