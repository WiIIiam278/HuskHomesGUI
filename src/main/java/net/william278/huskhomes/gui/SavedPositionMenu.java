package net.william278.huskhomes.gui;

import de.themoep.inventorygui.*;
import de.themoep.minedown.adventure.MineDown;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.wesjd.anvilgui.AnvilGUI;
import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.util.Permission;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.william278.huskhomes.gui.Util.*;

/**
 * A menu for displaying a list of saved positions
 */
public class SavedPositionMenu {

//    private static final String[] MENU_LAYOUT = {
//            "ppppppppp",
//            "ppppppppp",
//            "ppppppppp",
//            "bl  i  ne",
//    };

    // 传送点列表GUI // 自定义箱子尺寸
    private static final String[] MENU_LAYOUT = Arrays.copyOfRange(
            new String[] {
                    "ppppppppp",
                    "ppppppppp",
                    "ppppppppp",
                    "ppppppppp",
                    "ppppppppp",
                    "bl  i  ne"},
            6 - getIntFromConfig("menu.size"), // 2 ~ 6, 为1时只显示操作栏
            6);

    // 编辑传送点GUI
    private static final String[] EDIT_MENU_LAYOUT = new String[] {
            "aa     ab",
            "aundip ra",
            "aa     aa"};


    private static final String TAG_KEY = "huskhomesgui:icon";
    private final InventoryGui menu;
    private InventoryGui edit_menu;
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

    private SavedPositionMenu(@NotNull HuskHomesGui plugin,
                              @NotNull HuskHomesAPI huskHomesAPI,
                              @NotNull List<? extends SavedPosition> positionList,
                              @NotNull MenuType menuType,
                              @NotNull String title) {

        this.menu = new InventoryGui(plugin, title, MENU_LAYOUT);

        this.menuType = menuType;
        this.huskHomesAPI = huskHomesAPI;

        // Add filler items
        this.menu.setFiller(new ItemStack(menuType.fillerMaterial, 1));

        // Add pagination handling
        this.menu.addElement(getPositionGroup(plugin, positionList));
        this.menu.addElement(new GuiPageElement('b',
                new ItemStack(getItemFromConfig("menu.pagination.FIRST.item")),
                GuiPageElement.PageAction.FIRST,
                getLegacyText(getMessageFromConfig("menu.pagination.FIRST.title"))));
        this.menu.addElement(new GuiPageElement('l',
                new ItemStack(getItemFromConfig("menu.pagination.PREVIOUS.item")),
                GuiPageElement.PageAction.PREVIOUS,
                getLegacyText(getMessageFromConfig("menu.pagination.PREVIOUS.title"))));
        this.menu.addElement(new GuiPageElement('n',
                new ItemStack(getItemFromConfig("menu.pagination.NEXT.item")),
                GuiPageElement.PageAction.NEXT,
                getLegacyText(getMessageFromConfig("menu.pagination.NEXT.title"))));
        this.menu.addElement(new GuiPageElement('e',
                new ItemStack(getItemFromConfig("menu.pagination.LAST.item")),
                GuiPageElement.PageAction.LAST,
                getLegacyText(getMessageFromConfig("menu.pagination.LAST.title"))));
        // 信息显示
        if(getBooleanFromConfig("menu.pagination.INFO.enable")){
            this.menu.addElement(new StaticGuiElement('i',
                    new ItemStack(getItemFromConfig("menu.pagination.INFO.item")),
                    getLegacyText(getMessageFromConfig("menu.pagination.INFO.title")),
                    getLegacyText(getMessageFromConfig("menu.pagination.INFO.message.A")),
                    getLegacyText(getMessageFromConfig("menu.pagination.INFO.message.B")),
                    getLegacyText(getMessageFromConfig("menu.pagination.INFO.message.C"))));
        }

    }

    // 遍历传送点, 并添加按钮
    @NotNull
    private GuiElementGroup getPositionGroup(@NotNull HuskHomesGui plugin,
                                             @NotNull List<? extends SavedPosition> positions) {
        final GuiElementGroup group = new GuiElementGroup('p');
        for (SavedPosition position : positions) {
            group.addElement(getPositionButton(plugin, position));
        }
        return group;
    }

    // 创建一个传送点按钮
    private DynamicGuiElement getPositionButton(@NotNull HuskHomesGui plugin,
                                                @NotNull SavedPosition position) {
//        ItemStack position_item = new ItemStack(getPositionMaterial(position).orElse(getItemFromConfig("menu.item.default-item")));

//        return new StaticGuiElement('e',
//                position_item,
//                // 点击传送点物品时
//                click -> {
//                    if (click.getWhoClicked() instanceof Player player) {
//                        final OnlineUser onlineUser = huskHomesAPI.adaptUser(player);
//                        switch (click.getType()) {
//                            case LEFT -> { // 左键传送
//                                // 如果玩家手上有物品, 就运行修改图标, 否则点击传送
//                                ItemStack newItem = player.getItemOnCursor();
//                                if(newItem.getType() != Material.AIR){
//                                    setPositionMaterial(position, newItem.getType())
//                                            .thenRun(() -> player.sendMessage(getLegacyText(getMessageFromConfig("chat.updated-icon"))
//                                                    .replaceAll("%1%", position.meta.name)));
//
//                                }else{
//                                    menu.close(true);
//                                    huskHomesAPI.teleportPlayer(onlineUser, position, true);
//                                }
//                            }
//
//                            case RIGHT -> { // 右键编辑
//                                // 如果玩家没有warp权限, 则不打开编辑页面
//                                if (Objects.requireNonNull(menuType) == MenuType.WARP) {
//                                    if (!player.hasPermission(Permission.COMMAND_EDIT_WARP.node)) {
//                                        return true;
//                                    }
//                                }
//                                getEditGui(plugin, position, position_item, menuType).show(player);
//                            }
//
//                            case SHIFT_LEFT -> { // 设置物品
//                                if (canEditPosition(position, player) && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
//                                    menu.close(true);
//                                    setPositionMaterial(position, player.getInventory().getItemInMainHand().getType())
//                                            .thenRun(() -> player.sendMessage(getLegacyText(getMessageFromConfig("chat.updated-icon"))
//                                                    .replaceAll("%1%", position.meta.name)));
//                                }
//                            }
//                        }
//                    }
//                    return true;
//                },
//                getLegacyText(getMessageFromConfig("menu.item.name").replace("%1%", position.meta.name)),
//                getLegacyText(getMessageFromConfig("menu.item.description").replace("%1%", position.meta.description.isBlank()
//                        ? huskHomesAPI.getRawLocale("menu.item_no_description").orElse(getMessageFromConfig("menu.item.description-var1-no"))
//                        : position.meta.description)),
//                getMessageFromConfig("menu.item.space"),
//                getLegacyText(getMessageFromConfig("menu.item.Left")),
//                getLegacyText(getMessageFromConfig("menu.item.Right")),
//                getLegacyText(getMessageFromConfig("menu.item.Shift")));

        return new DynamicGuiElement('e', (viewer) -> {
                return new StaticGuiElement('e',
                        new ItemStack(getPositionMaterial(position).orElse(getItemFromConfig("menu.item.default-item"))),
                        click -> {
                            if (click.getWhoClicked() instanceof Player player) {
                                final OnlineUser onlineUser = huskHomesAPI.adaptUser(player);
                                switch (click.getType()) {
                                    case LEFT -> { // 左键传送
                                        // 如果玩家手上有物品, 就运行修改图标, 否则点击传送
                                        ItemStack newItem = player.getItemOnCursor();
                                        if(newItem.getType() != Material.AIR){
                                            setPositionMaterial(position, newItem.getType())
                                                    .thenRun(() -> player.sendMessage(getLegacyText(getMessageFromConfig("chat.updated-icon"))
                                                            .replaceAll("%1%", position.meta.name)));
                                            click.getGui().draw();
                                        }else{
                                            menu.close(true);
                                            huskHomesAPI.teleportPlayer(onlineUser, position, true);
                                        }
                                    }

                                    case RIGHT -> { // 右键编辑
                                        // 如果玩家没有warp权限, 则不打开编辑页面
                                        if (Objects.requireNonNull(menuType) == MenuType.WARP) {
                                            if (!player.hasPermission(Permission.COMMAND_EDIT_WARP.node)) {
                                                return true;
                                            }
                                        }
                                        getEditGui(plugin, position, menuType).show(player);
                                    }

                                    case SHIFT_LEFT -> { // 设置物品
                                        if (canEditPosition(position, player) && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                            menu.close(true);
                                            setPositionMaterial(position, player.getInventory().getItemInMainHand().getType())
                                                    .thenRun(() -> player.sendMessage(getLegacyText(getMessageFromConfig("chat.updated-icon"))
                                                            .replaceAll("%1%", position.meta.name)));
                                        }
                                    }
                                }
                            }
                            return true;
                        },
                        getLegacyText(getMessageFromConfig("menu.item.name").replace("%1%", position.meta.name)),
                        getLegacyText(getMessageFromConfig("menu.item.description").replace("%1%", position.meta.description.isBlank()
                                ? huskHomesAPI.getRawLocale("menu.item_no_description").orElse(getMessageFromConfig("menu.item.description-var1-no"))
                                : position.meta.description)),
                        getMessageFromConfig("menu.item.space"),
                        getLegacyText(getMessageFromConfig("menu.item.Left")),
                        getLegacyText(getMessageFromConfig("menu.item.Right")),
                        getLegacyText(getMessageFromConfig("menu.item.Shift")));
        });
    }

    // 输出编辑菜单
    private InventoryGui getEditGui(@NotNull HuskHomesGui plugin,
                                    SavedPosition position,
//                                    ItemStack item,
                                    MenuType menuType) {

        // inventorygui DOC: https://docs.phoenix616.dev/inventorygui/de/themoep/inventorygui/package-summary.html
        // AnvilGUI: https://github.com/WesJD/AnvilGUI
        // HuskHome Command: https://github.com/WiIIiam278/HuskHomes2/wiki/Commands

        // title
        switch (menuType) {
            case HOME, PUBLIC_HOME -> {
                this.edit_menu = new InventoryGui(plugin,
                        getMessageFromConfig("edit-menu.title.HOME").replace("%1%", position.meta.name),
                        EDIT_MENU_LAYOUT);
            }
            case WARP -> {
                this.edit_menu = new InventoryGui(plugin,
                        getMessageFromConfig("edit-menu.title.WARP").replace("%1%", position.meta.name),
                        EDIT_MENU_LAYOUT);
            }
        }

        ItemStack item = new ItemStack(getPositionMaterial(position).orElse(getItemFromConfig("menu.item.default-item")));

        // a = 背景
        // b = 返回
        // u = 更新位置
        // n = 更新名称
        // d = 更新描述
        // i = 显示信息
        // p = 开放 (phome)
        // r = 删除

        this.edit_menu.setCloseAction(i -> {
            return false;
        });

        // 背景
        this.edit_menu.addElement(new StaticGuiElement('a',
                new ItemStack(switch (menuType) {
                    case HOME, PUBLIC_HOME -> getItemFromConfig("edit-menu.theme-item.HOME");
                    case WARP -> getItemFromConfig("edit-menu.theme-item.WARP");
                }),
                " "  // 不显示物品默认的名称
        ));

        // 返回按钮
        this.edit_menu.addElement(new GuiBackElement('b',
                new ItemStack(getItemFromConfig("edit-menu.button.BACK.item")),
                true,   // 在没有可返回的GUI时关闭GUI
                getLegacyText(getMessageFromConfig("edit-menu.button.BACK.text"))));

        // 更新位置
        this.edit_menu.addElement(new StaticGuiElement('u',
                new ItemStack(getItemFromConfig("edit-menu.button.Update-location.item")),
                click -> {
                    if (click.getWhoClicked() instanceof Player player) {
                        player.performCommand(switch (menuType) {
                            case HOME, PUBLIC_HOME -> "huskhomes:edithome " + ((Home) position).owner.username +"."+ position.meta.name +" relocate";
                            case WARP -> "huskhomes:editwarp " + position.meta.name +" relocate";
                        });
                    }
                    return true;
                },
                getLegacyText(getMessageFromConfig("edit-menu.button.Update-location.text"))));

        // 更新名称
        this.edit_menu.addElement(new StaticGuiElement('n',
                new ItemStack(getItemFromConfig("edit-menu.button.Update-name.item")),
                click -> {
                    if (click.getWhoClicked() instanceof Player player) {
                        edit_menu.close(false); // false = 不清除历史记录?
                        new AnvilGUI.Builder()
                                .title(getMessageFromConfig("edit-menu.button.Update-name.anvil-menu.title").replace("%1%", position.meta.name))
                                .itemLeft(new ItemStack(item))
                                .text(position.meta.name)
                                .onClose(playerInAnvil -> {
                                    edit_menu.show(player);
                                })
                                // 点击输出位
                                .onComplete((completion) -> {
                                    if(completion.getText() != null){
                                        player.performCommand(switch (menuType) {
                                            case HOME, PUBLIC_HOME -> "huskhomes:edithome " + ((Home) position).owner.username +"."+ position.meta.name +" rename "+ completion.getText();
                                            case WARP -> "huskhomes:editwarp " + position.meta.name +" rename "+ completion.getText();
                                        });
                                    }
                                    AnvilGUI.ResponseAction.close();
//                                    edit_menu.show(player);
                                    position.meta.name = completion.getText();
                                    getEditGui(plugin, position, menuType).show(player);
                                    return List.of();
                                })

                                .plugin(plugin)
                                .open(player);
                    }
                    return true;
                },
                getLegacyText(getMessageFromConfig("edit-menu.button.Update-name.text"))));

        // 更新描述
        this.edit_menu.addElement(new StaticGuiElement('d',
                new ItemStack(getItemFromConfig("edit-menu.button.Update-description.item")),
                click -> {
                    if (click.getWhoClicked() instanceof Player player) {
                        edit_menu.close(false);
                        new AnvilGUI.Builder()
                                .title(getMessageFromConfig("edit-menu.button.Update-description.anvil-menu.title").replace("%1%", position.meta.name))
                                .itemLeft(new ItemStack(item))
                                .text(position.meta.description)
                                .onClose(playerInAnvil -> {
                                    edit_menu.show(player);
                                })
                                .onComplete((completion) -> {
                                    if(completion.getText() != null){
                                        player.performCommand(switch (menuType) {
                                            case HOME, PUBLIC_HOME -> "huskhomes:edithome " + ((Home) position).owner.username +"."+ position.meta.name +" description "+ completion.getText();
                                            case WARP -> "huskhomes:editwarp " + position.meta.name +" description "+ completion.getText();
                                        });
                                    }
                                    AnvilGUI.ResponseAction.close();
//                                    edit_menu.show(player);
                                    position.meta.description = completion.getText();
                                    getEditGui(plugin, position, menuType).show(player);
                                    return List.of();
                                })

                                .plugin(plugin)
                                .open(player);
                    }
                    return true;
                },
                getLegacyText(getMessageFromConfig("edit-menu.button.Update-description.text"))));

        // 显示信息
        this.edit_menu.addElement(new StaticGuiElement('i',
                new ItemStack(Material.OAK_SIGN),
                getLegacyText(getMessageFromConfig("edit-menu.button.INFO.name")
                        .replace("%1%", position.meta.name)),
                getLegacyText(getMessageFromConfig("edit-menu.button.INFO.description")
                        .replace("%1%", position.meta.description)),
                getLegacyText(getMessageFromConfig("edit-menu.button.INFO.world")
                        .replace("%1%", position.world.name)),
                getLegacyText(getMessageFromConfig("edit-menu.button.INFO.server")
                        .replace("%1%", position.server.name)),
                getLegacyText(getMessageFromConfig("edit-menu.button.INFO.coordinate")
                        .replace("%x%", String.format("%.1f", position.x))
                        .replace("%y%", String.format("%.1f", position.y))
                        .replace("%z%", String.format("%.1f", position.z)))
        ));

        // 切换开放 phome
        switch (menuType) {
            case HOME, PUBLIC_HOME -> {
                this.edit_menu.addElement(new StaticGuiElement('p',
                        new ItemStack(getItemFromConfig("edit-menu.button.Update-privacy.item")),
                        click -> {
                            if (click.getWhoClicked() instanceof Player player) {
//                      edit_menu.close(true);
                                player.performCommand("huskhomes:edithome " + ((Home) position).owner.username +"."+ position.meta.name +" privacy");
                            }
                            return true;
                        },
                        getLegacyText(getMessageFromConfig("edit-menu.button.Update-privacy.text"))));
            }
//            case WARP -> {}
        };


        // 删除 (使用右键
        this.edit_menu.addElement(new StaticGuiElement('r',
                new ItemStack(getItemFromConfig("edit-menu.button.del.item")),
                click -> {
                    if (click.getWhoClicked() instanceof Player player) {
                        // 右键
                        if (Objects.requireNonNull(click.getType()) == ClickType.RIGHT) {
                            edit_menu.close(true);
                            player.performCommand(switch (menuType) {
                                case HOME, PUBLIC_HOME -> "huskhomes:delhome " + ((Home) position).owner.username + "." + position.meta.name;
                                case WARP -> "huskhomes:delwarp " + position.meta.name;
                            });
                        }
                    }
                    return true;
                },
                getLegacyText(getMessageFromConfig("edit-menu.button.del.text")),
                getLegacyText(getMessageFromConfig("edit-menu.button.del.text-2"))));


        return this.edit_menu;
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
//                getLocale("error_no_permission").ifPresent(player::sendMessage);
                return false;
            }
            return true;
        }

        // Validate home permission checks
        if (menuType == MenuType.HOME || menuType == MenuType.PUBLIC_HOME) {
            final Home home = (Home) position;
            if (player.getUniqueId().equals(home.owner.uuid)) {
                if (!player.hasPermission(Permission.COMMAND_EDIT_HOME.node)) {
//                    getLocale("error_no_permission").ifPresent(player::sendMessage);
                    return false;
                }
            } else {
                if (!player.hasPermission(Permission.COMMAND_EDIT_HOME_OTHER.node)) {
//                    getLocale("error_no_permission").ifPresent(player::sendMessage);
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
        HOME(getItemFromConfig("menu.theme-item.HOME")),
        PUBLIC_HOME(getItemFromConfig("menu.theme-item.PUBLIC_HOME")),
        WARP(getItemFromConfig("menu.theme-item.WARP"));

        private final Material fillerMaterial;

        MenuType(@NotNull Material fillerMaterial) {
            this.fillerMaterial = fillerMaterial;
        }
    }

}