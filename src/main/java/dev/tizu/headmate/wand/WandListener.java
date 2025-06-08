package dev.tizu.headmate.wand;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import dev.tizu.headmate.util.Locator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class WandListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        var player = event.getPlayer();
        if (player.getInventory().getItemInOffHand().getType() != Material.BREEZE_ROD
                || event.getAction() == Action.PHYSICAL)
            return;

        event.setCancelled(true);

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
            handleRayClick(event);
    }

    private void handleRayClick(PlayerInteractEvent event) {
        var player = event.getPlayer();

        var considerations = player.getNearbyEntities(10, 10, 10).stream()
                .filter(e -> e instanceof ItemDisplay).map(e -> (ItemDisplay) e).toList();

        var head = Locator.lookingAt(player.getEyeLocation(), considerations);
        if (head == null) {
            player.sendActionBar(Component.text("no results", NamedTextColor.RED));
            return;
        }

        var vec = Locator.centerOfHead(head);
        var loc = new org.bukkit.Location(player.getWorld(), vec.x(), vec.y(), vec.z());
        player.spawnParticle(Particle.DUST, loc, 10, new Particle.DustOptions(Color.BLUE, 3f));
    }

}
