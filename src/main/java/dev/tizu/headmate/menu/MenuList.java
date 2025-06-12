package dev.tizu.headmate.menu;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import dev.tizu.headmate.ThisPlugin;
import dev.tizu.headmate.editor.Editor;
import dev.tizu.headmate.headmate.HeadmateStore;
import dev.tizu.headmate.util.Config;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MenuList implements Menu {

    private final Inventory inv;
    private final Block block;

    public MenuList(Block block) {
        this.inv = ThisPlugin.instance.getServer().createInventory(this,
                (int) Math.ceil(Config.maxHeads() / 9f) * 9, Component.text("Merged Heads"));
        this.block = block;
        render();
    }

    @Override
    public Inventory getInventory() {
        return this.inv;
    }

    public void render() {
        this.inv.clear();
        var headmates = HeadmateStore.getHeads(block);
        for (int i = 0; i < headmates.length; i++) {
            if (headmates[i] == null) {
                var stack = ItemStack.of(Material.SKELETON_SKULL);
                stack.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Externally deleted head",
                        NamedTextColor.RED));
                stack.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
                        Component.text("Shift-right-click to remove", NamedTextColor.RED))));
                this.inv.setItem(i, stack);
                continue;
            }

            var stack = headmates[i].getItemStack();
            stack.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
                    Component.text("Click to edit", NamedTextColor.BLUE),
                    Component.text("Shift-right-click to remove", NamedTextColor.RED))));
            this.inv.setItem(i, stack);
        }
    }

    public void onInventoryClick(InventoryClickEvent event) {
        var inventory = event.getClickedInventory();
        var player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        var headmates = HeadmateStore.getHeads(block);
        var slot = event.getSlot();
        if (slot >= inventory.getSize())
            return;
        if (slot >= headmates.length)
            return;

        if (headmates[slot] == null) {
            if (event.isShiftClick() && event.isRightClick()) {
                HeadmateStore.removeInvalid(block);
                this.close();
            }
            return;
        }

        var uuid = headmates[slot].getUniqueId();
        if (event.isShiftClick() && event.isRightClick()) {
            HeadmateStore.remove(block, uuid);
            if (headmates.length == 1)
                this.close();
            else
                render();
        } else if (event.isLeftClick()) {
            this.close();
            Editor.startEditing(player, block, headmates[slot]);
        } else if (event.isRightClick()) {
            if (player.getGameMode() != GameMode.CREATIVE)
                return;
            var entity = headmates[slot];
            var item = entity.getItemStack();
            player.getInventory().addItem(item);
            render();
        }
    }

    private void close() {
        Bukkit.getScheduler().runTaskLater(ThisPlugin.instance, (task) -> {
            if (this.inv != null)
                this.inv.close();
        }, 1);
    }

}