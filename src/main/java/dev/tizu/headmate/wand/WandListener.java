package dev.tizu.headmate.wand;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;

import dev.tizu.headmate.ThisPlugin;
import dev.tizu.headmate.editor.Editor;
import dev.tizu.headmate.headmate.HeadmateStore;
import dev.tizu.headmate.menu.MenuList;
import dev.tizu.headmate.util.Config;
import dev.tizu.headmate.util.Locator;
import io.papermc.paper.datacomponent.DataComponentTypes;
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
        if (!player.hasPermission("headmate.wand.use"))
            return;

        // HACK: this would otherwise get calld twice, once for off and once for the
        // main hand. oops!
        if (event.getHand() != EquipmentSlot.HAND)
            return;

        var rateLimitKey = new NamespacedKey(ThisPlugin.instance, "last-wand-click");
        var pdc = player.getPersistentDataContainer();
        if (pdc.has(rateLimitKey, PersistentDataType.LONG)) {
            var lastClick = pdc.get(rateLimitKey, PersistentDataType.LONG);
            var deltaClick = System.currentTimeMillis() - lastClick;
            if (deltaClick < 250 && deltaClick > 0)
                return;
        }
        pdc.set(rateLimitKey, PersistentDataType.LONG, System.currentTimeMillis());

        var block = event.getClickedBlock();
        var offhand = player.getInventory().getItemInOffHand();
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                switch (offhand.getType()) {
                    case PLAYER_HEAD:
                        handleCreation(event);
                        return;
                    case AIR:
                        if (player.isSneaking() && HeadmateStore.has(block)) {
                            handleListClick(event);
                            return;
                        }
                        break;
                    default:
                        player.sendActionBar(Component.text("That's not a head!", NamedTextColor.RED));
                        return;
                }
            case RIGHT_CLICK_AIR:
                switch (offhand.getType()) {
                    case PLAYER_HEAD:
                        player.sendActionBar(
                                Component.text("Face a block to create a merged head", NamedTextColor.RED));
                        return;
                    case AIR:
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
                    default:
                        player.sendActionBar(
                                Component.text("Hold head or nothing in offhand", NamedTextColor.RED));
                        return;
                }
                break;

            case LEFT_CLICK_BLOCK:
                switch (offhand.getType()) {
                    case LAVA_BUCKET:
                        if (!HeadmateStore.has(block)) {
                            player.sendActionBar(Component.text("Nothing to remove!", NamedTextColor.RED));
                            return;
                        }
                        HeadmateStore.removeAll(block);
                        return;
                    case AIR:
                        if (!HeadmateStore.has(block)) {
                            player.sendActionBar(Component.text("Cannot change hitbox here", NamedTextColor.RED));
                            return;
                        }
                        if (!player.isSneaking()) {
                            HeadmateStore.changeHitbox(player, block);
                            break;
                        }
                    default:
                        player.sendActionBar(Component.text(
                                "Hold lava (to delete) or nothing (change hitbox) in offhand",
                                NamedTextColor.RED));
                        return;
                }
                break;

            default:
                return;
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        var player = event.getPlayer();
        var block = event.getBlock();

        if (player.getInventory().getItemInMainHand().getType() == Material.BREEZE_ROD)
            return;
        if (!HeadmateStore.has(block))
            return;

        Bukkit.getScheduler().runTaskLater(ThisPlugin.instance,
                () -> event.getBlock().setType(Material.STRUCTURE_VOID), 1);
        player.sendActionBar(Component.text(player.hasPermission("headmate.wand.use")
                ? "Use the wand to delete these heads!"
                : "You cannot delete these heads!", NamedTextColor.RED));
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
        spawnParticle(head, player);
    }

    private void handleCreation(PlayerInteractEvent event) {
        var player = event.getPlayer();
        var block = player.isSneaking() ? event.getClickedBlock()
                : event.getClickedBlock().getRelative(event.getBlockFace());

        if (HeadmateStore.getCount(block) >= Config.maxHeads()) {
            player.sendActionBar(Component.text("Too many heads!", NamedTextColor.RED));
            return;
        }

        if (block.getType() == Material.AIR)
            block.setType(Material.STRUCTURE_VOID);
        var profile = player.getInventory().getItemInOffHand().getData(DataComponentTypes.PROFILE);
        var entity = HeadmateStore.add(block, profile, player.getYaw());
        if (player.getGameMode() == GameMode.SURVIVAL)
            player.getInventory().getItemInOffHand().subtract();
        Editor.startEditing(player, block, entity);
        return;
    }

    private void spawnParticle(ItemDisplay head, Player player) {
        var center = Locator.centerOfHead(head);
        var count = (int) Math.max(Locator.diameterOfHead(head) * 50, 5);
        player.spawnParticle(Particle.EFFECT, new Location(head.getWorld(), center.x, center.y,
                center.z), count, 0.0, 0.0, 0.0, 5);
    }

}
