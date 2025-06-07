package dev.tizu.headmate;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.profile.PlayerProfile;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;;

public class ThisPlugin extends JavaPlugin implements Listener {
    public static ThisPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        var player = event.getPlayer();
        var head = event.getItemInHand();
        var block = event.getBlockAgainst();
        if (head.getType() != Material.PLAYER_HEAD)
            return;

        var isHeadmate = HeadmateStore.has(block);
        var addNewHead = head.getData(DataComponentTypes.PROFILE) != null;
        if (!isHeadmate && block.getType() != Material.PLAYER_HEAD
                && block.getType() != Material.PLAYER_WALL_HEAD)
            return;

        if (!player.isSneaking()) {
            player.sendActionBar(Component.text(
                    "Hold shift while placing to merge heads!", NamedTextColor.GRAY));
            return;
        }

        if (!isHeadmate) {
            HeadmateStore.create(block);
            event.getPlayer().spawnParticle(Particle.ENCHANT, block.getLocation()
                    .add(0.5, 0.5, 0.5), 1000);
            event.setCancelled(true);
            if (addNewHead)
                player.sendActionBar(Component.text(
                        "Shift-click again to add the head you're holding!", NamedTextColor.GRAY));
            return;
        }

        if (addNewHead) {
            HeadmateStore.add(block, head.getData(DataComponentTypes.PROFILE));
            if (player.getGameMode() == GameMode.SURVIVAL)
                player.getInventory().removeItem(head);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var player = event.getPlayer();
        var block = event.getBlock();
        if (!HeadmateStore.has(block))
            return;

        if (!player.isSneaking()) {
            player.sendActionBar(Component.text(
                    "Hold shift to remove merged heads!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        HeadmateStore.removeAll(block);
    }
}