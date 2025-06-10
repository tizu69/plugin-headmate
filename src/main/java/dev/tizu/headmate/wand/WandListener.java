package dev.tizu.headmate.wand;

import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import dev.tizu.headmate.editor.Editor;
import dev.tizu.headmate.headmate.HeadmateStore;
import dev.tizu.headmate.menu.MenuList;
import dev.tizu.headmate.util.Locator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WandListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.BREEZE_ROD
                || event.getAction() == Action.PHYSICAL)
            return;

        event.setCancelled(true);

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                if (player.isSneaking() && HeadmateStore.has(event.getClickedBlock())) {
                    handleListClick(event);
                    return;
                }
            case RIGHT_CLICK_AIR:
                if (player.isSneaking()) {
                    // the above falls through - if you're not looking at a block & sneaking OR
                    // the block you're looking at has no merged heads, this fires! very neat.
                    player.sendActionBar(Component.text(
                            "No merged heads here, click with head in offhand to create one!",
                            NamedTextColor.RED));
                    return;
                }
                handleRayClick(event);
                break;

            case LEFT_CLICK_BLOCK:
                HeadmateStore.changeHitbox(player, event.getClickedBlock());
        }
    }

    private void handleListClick(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var block = event.getClickedBlock();
        player.openInventory(new MenuList(block).getInventory());
    }

    private void handleRayClick(PlayerInteractEvent event) {
        var player = event.getPlayer();

        var considerations = player.getNearbyEntities(10, 10, 10).stream()
                .filter(e -> e instanceof ItemDisplay).map(e -> (ItemDisplay) e).toList();

        var head = Locator.lookingAt(player.getEyeLocation(), considerations);
        if (head == null) {
            player.sendActionBar(
                    Component.text("Could not find head, shift-click to select manually", NamedTextColor.RED));
            return;
        }

        var block = head.getWorld().getBlockAt(head.getLocation());
        Editor.startEditing(player, block, head);
    }

}
