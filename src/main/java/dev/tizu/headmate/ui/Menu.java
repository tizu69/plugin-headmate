package dev.tizu.headmate.ui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface Menu extends InventoryHolder {
    void render();

    void onInventoryClick(InventoryClickEvent event);
}
