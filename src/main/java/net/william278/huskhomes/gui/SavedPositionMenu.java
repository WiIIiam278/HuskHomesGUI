package net.william278.huskhomes.gui;

import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import net.william278.huskhomes.api.HuskHomesAPI;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.position.SavedPosition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    protected static SavedPositionMenu create(@NotNull HuskHomesGui plugin,
                                           @NotNull List<? extends SavedPosition> savedPositions,
                                           @NotNull String title) {
        return new SavedPositionMenu(plugin, HuskHomesAPI.getInstance(), savedPositions, title);
    }

    protected void show(@NotNull OnlineUser onlineUser) {
        menu.show(huskHomesAPI.getPlayer(onlineUser));
    }

    private SavedPositionMenu(@NotNull HuskHomesGui plugin, @NotNull HuskHomesAPI huskHomesAPI,
                              @NotNull List<? extends SavedPosition> positionList, @NotNull String title) {
        this.menu = new InventoryGui(plugin, title, MENU_LAYOUT);
        this.huskHomesAPI = huskHomesAPI;

        // Add filler items
        this.menu.setFiller(new ItemStack(FILLER_MATERIAL, 1));

        // Add pagination handling
        this.menu.addElement(getPositionGroup(positionList));
        this.menu.addElement(new GuiPageElement('b', new ItemStack(Material.EGG),
                GuiPageElement.PageAction.FIRST,
                "Go to first page (current: %page%)"));
        this.menu.addElement(new GuiPageElement('l', new ItemStack(Material.ARROW),
                GuiPageElement.PageAction.PREVIOUS,
                "Go to previous page (%prevpage%)"));
        this.menu.addElement(new GuiPageElement('n', new ItemStack(Material.SPECTRAL_ARROW),
                GuiPageElement.PageAction.NEXT,
                "Go to next page (%nextpage%)"));
        this.menu.addElement(new GuiPageElement('e', new ItemStack(Material.EGG),
                GuiPageElement.PageAction.LAST,
                "Go to last page (%pages%)"));

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
        return new StaticGuiElement('e', new ItemStack(Material.IRON_NUGGET),
                click -> {
                    if (click.getWhoClicked() instanceof Player player) {
                        final OnlineUser onlineUser = huskHomesAPI.adaptUser(player);
                        if (click.getType() == ClickType.LEFT) {
                            player.closeInventory();
                            huskHomesAPI.teleportPlayer(onlineUser, position);
                        }
                    }
                    return true;
                },
                position.meta.name,
                "(Click to teleport)",
                "",
                position.meta.description);
    }

}
