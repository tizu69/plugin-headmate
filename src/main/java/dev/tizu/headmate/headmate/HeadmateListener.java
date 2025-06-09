package dev.tizu.headmate.headmate;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import dev.tizu.headmate.ThisPlugin;
import dev.tizu.headmate.editor.Editor;
import dev.tizu.headmate.menu.MenuList;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HeadmateListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var player = event.getPlayer();
        var head = event.getItemInHand();
        var block = event.getBlockAgainst();
        if (head.getType() != Material.PLAYER_HEAD)
            return;

        var isHeadmate = HeadmateStore.has(block);
        var handHeadHasSkin = head.getData(DataComponentTypes.PROFILE) != null;
        if (!isHeadmate && block.getType() != Material.PLAYER_HEAD
                && block.getType() != Material.PLAYER_WALL_HEAD) {
            player.sendActionBar(Component.text(
                    "Shift-click with a player skull to start merging heads!", NamedTextColor.GRAY));
            return;
        }

        if (!player.isSneaking()) {
            player.sendActionBar(Component.text(
                    "Hold shift while placing to merge heads!", NamedTextColor.GRAY));
            return;
        }

        if (!isHeadmate) {
            var display = HeadmateStore.create(block);
            event.getPlayer().spawnParticle(Particle.ENCHANT, block.getLocation()
                    .add(0.5, 0.5, 0.5), 1000);
            event.setCancelled(true);
            Editor.startEditing(player, block, display);
            return;
        }

        if (handHeadHasSkin) {
            if (HeadmateStore.getCount(block) >= HeadmateStore.PROPOSED_MAX_HEADS) {
                player.sendActionBar(Component.text("Too many heads!", NamedTextColor.RED));
                return;
            }
            var entity = HeadmateStore.add(block, head.getData(DataComponentTypes.PROFILE), player.getYaw());
            if (player.getGameMode() == GameMode.SURVIVAL)
                event.getItemInHand().subtract();
            event.setCancelled(true);
            Editor.startEditing(player, block, entity);
            return;
        }

        event.setCancelled(true);
        player.openInventory(new MenuList(block).getInventory());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var player = event.getPlayer();
        var block = event.getBlock();
        if (!HeadmateStore.has(block))
            return;

        if (!player.isSneaking()) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(ThisPlugin.instance,
                    () -> event.getBlock().setType(Material.STRUCTURE_VOID), 1);
            player.sendActionBar(Component.text(
                    "Hold shift to remove merged heads!", NamedTextColor.RED));
            return;
        }

        HeadmateStore.removeAll(block);
    }

}
