package dev.tizu.headmate.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface Menu extends InventoryHolder {
    void render();

    void onInventoryClick(InventoryClickEvent event);

    public class MenuListener implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            var inventory = event.getClickedInventory();
            if (inventory == null || !(inventory.getHolder(false) instanceof Menu menu))
                return;
            menu.onInventoryClick(event);
        }

    }

}
