package net.william278.huskhomes.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.util.Permission;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static net.william278.huskhomes.gui.Util.*;

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
    private static final String TAG_KEY = "huskhomesgui:icon";
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
        this.menu.setFiller(new ItemStack(menuType.fillerMaterial, 1));

        // Add pagination handling
        this.menu.addElement(getPositionGroup(positionList));
        this.menu.addElement(new GuiPageElement('b',
                new ItemStack(getItemFromConfig("message.pagination.FIRST-item")),
                GuiPageElement.PageAction.FIRST,
                getLegacyText( getMessageFromConfig("message.pagination.FIRST") )));
        this.menu.addElement(new GuiPageElement('l',
                new ItemStack(getItemFromConfig("message.pagination.PREVIOUS-item")),
                GuiPageElement.PageAction.PREVIOUS,
                getLegacyText( getMessageFromConfig("message.pagination.PREVIOUS") )));
        this.menu.addElement(new GuiPageElement('n',
                new ItemStack(getItemFromConfig("message.pagination.NEXT-item")),
                GuiPageElement.PageAction.NEXT,
                getLegacyText( getMessageFromConfig("message.pagination.NEXT") )));
        this.menu.addElement(new GuiPageElement('e',
                new ItemStack(getItemFromConfig("message.pagination.LAST-item")),
                GuiPageElement.PageAction.LAST,
                getLegacyText( getMessageFromConfig("message.pagination.LAST") )));
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
        return new StaticGuiElement('e',
                new ItemStack(getPositionMaterial(position).orElse(getItemFromConfig("message.item.default-item"))),
                click -> {
                    if (click.getWhoClicked() instanceof Player player) {
                        final OnlineUser onlineUser = huskHomesAPI.adaptUser(player);
                        switch (click.getType()) {
                            case LEFT -> {
                                menu.close(true);
                                huskHomesAPI.teleportPlayer(onlineUser, position, true);
                            }
                            case RIGHT -> {
                                menu.close(true);
                                player.performCommand(switch (menuType) {
                                    case HOME, PUBLIC_HOME ->
                                            "huskhomes:edithome " + ((Home) position).owner.username + "." + position.meta.name;
                                    case WARP -> "huskhomes:editwarp " + position.meta.name;
                                });
                            }
                            case SHIFT_LEFT -> {
                                if (canEditPosition(position, player) && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                    menu.close(true);
                                    setPositionMaterial(position, player.getInventory().getItemInMainHand().getType())
                                            .thenRun(() -> player.sendMessage(getLegacyText( getMessageFromConfig("message.chat.updated-icon") )
                                                    .replaceAll("%1%", position.meta.name)));
                                }
                            }
                        }
                    }
                    return true;
                },
                getLegacyText(getMessageFromConfig("message.item.name").replace("%1%", position.meta.name)),
                getLegacyText(getMessageFromConfig("message.item.description").replace("%1%", position.meta.description.isBlank()
                        ? huskHomesAPI.getRawLocale("item_no_description").orElse(getMessageFromConfig("message.item.description-var1-no"))
                        : position.meta.description)),
                getMessageFromConfig("message.item.space"),
                getLegacyText(getMessageFromConfig("message.item.Left")),
                getLegacyText(getMessageFromConfig("message.item.Right")),
                getLegacyText(getMessageFromConfig("message.item.Shift")));
    }

    /**
     * Get the material to use for a saved position by icon tag
     *
     * @param position The saved position
     * @return The material to use if found
     */
    private Optional<Material> getPositionMaterial(@NotNull SavedPosition position) {
        if (position.meta.tags.containsKey(TAG_KEY)) {
            return Optional.ofNullable(Material.matchMaterial(position.meta.tags.get(TAG_KEY)));
        }
        return Optional.empty();
    }

    /**
     * Set the material to use for a {@link SavedPosition} and update it in the database
     *
     * @param position The saved position
     * @param material The {@link Material} to use
     * @return A future that completes when the saved position has been updated
     */
    private CompletableFuture<SavedPositionManager.SaveResult> setPositionMaterial(@NotNull SavedPosition position,
                                                                                   @NotNull Material material) {
        final PositionMeta meta = position.meta;
        meta.tags.put(TAG_KEY, material.getKey().toString());
        if (menuType == MenuType.WARP) {
            final Warp warp = (Warp) position;
            return huskHomesAPI.updateWarpMeta(warp, meta);
        } else {
            final Home home = (Home) position;
            return huskHomesAPI.updateHomeMeta(home, meta);
        }
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
                System.out.println("没有权限");
                // getLocale("error_no_permission").ifPresent(player::sendMessage);
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
    @SuppressWarnings("SameParameterValue")
    @NotNull
    private Optional<String> getLocale(@NotNull String localeKey, String... replacements) {
        return huskHomesAPI.getLocale(localeKey, replacements)
                .map(mineDown -> LegacyComponentSerializer.builder()
                        .build().serialize(mineDown.toComponent()));
    }

    /**
     * Convert MineDown formatted text into legacy text
     *
     * @param mineDown The MineDown formatted text
     * @return The legacy text
     */
    @NotNull
    private String getLegacyText(@NotNull String mineDown) {
        return LegacyComponentSerializer.builder()
                .build().serialize(new MineDown(mineDown).toComponent());
    }

    /**
     * Represents different types of {@link SavedPosition} that a {@link SavedPositionMenu} can display
     */
    protected enum MenuType {
        HOME(getItemFromConfig("message.menu-type.HOME-item")),
        PUBLIC_HOME(getItemFromConfig("message.menu-type.PUBLIC_HOME-item")),
        WARP(getItemFromConfig("message.menu-type.WARP-item"));

        private final Material fillerMaterial;

        MenuType(@NotNull Material fillerMaterial) {
            this.fillerMaterial = fillerMaterial;
        }
    }

}