package dev.tizu.headmate.headmate;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import dev.tizu.headmate.ThisPlugin;
import dev.tizu.headmate.util.Transformers;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HeadmateStore {
    public static final int PROPOSED_MAX_HEADS = 27;

    public static boolean has(Block block) {
        var pdc = block.getChunk().getPersistentDataContainer();
        return pdc.has(getKey(block), PersistentDataType.LIST.strings());
    }

    public static ItemDisplay create(Block block) {
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD)
            throw new IllegalArgumentException("Invalid block type, got " + block.getType());

        var skull = (Skull) block.getState();
        var blockdata = skull.getBlockData();
        var profile = skull.getPlayerProfile();
        block.setType(Material.STRUCTURE_VOID);
        return HeadmateStore.add(block, profile != null ? ResolvableProfile.resolvableProfile(profile) : null,
                Transformers.getPos(blockdata), Transformers.getRot(blockdata));
    }

    public static ItemDisplay add(Block block, ResolvableProfile profile, Vector3f position, Quaternionf rotation) {
        var pdc = block.getChunk().getPersistentDataContainer();

        var list = pdc.get(getKey(block), PersistentDataType.LIST.strings());
        list = new ArrayList<>(list == null ? new ArrayList<>() : list);

        var world = block.getWorld();
        var loc = block.getLocation().clone().add(0.5, 0.5, 0.5);

        // transfer the player head to the item display
        var item = new ItemStack(Material.PLAYER_HEAD);
        if (profile != null)
            item.setData(DataComponentTypes.PROFILE, profile);

        var entity = world.spawnEntity(loc, EntityType.ITEM_DISPLAY, SpawnReason.CUSTOM, (e) -> {
            var id = (ItemDisplay) e;
            id.setItemStack(item);
            id.setTransformation(new Transformation(position, rotation,
                    new Vector3f(1, 1, 1), new Quaternionf()));
        });
        list.add(entity.getUniqueId().toString());
        pdc.set(getKey(block), PersistentDataType.LIST.strings(), list);
        return (ItemDisplay) entity;
    }

    public static ItemDisplay add(Block block, ResolvableProfile profile, float yaw) {
        return HeadmateStore.add(block, profile, new Vector3f(0f, 0f, 0f),
                Transformers.getRot(yaw));
    }

    public static void remove(Block block, UUID uuid) {
        var pdc = block.getChunk().getPersistentDataContainer();
        var list = pdc.get(getKey(block), PersistentDataType.LIST.strings());
        list.remove(uuid.toString());

        var entity = block.getWorld().getEntity(uuid);
        if (entity != null) {
            entity.remove();
            var de = (ItemDisplay) entity;
            block.getWorld().spawnEntity(de.getLocation(), EntityType.ITEM,
                    SpawnReason.NATURAL, (e) -> {
                        var item = (Item) e;
                        item.setItemStack(de.getItemStack());
                    });
        }

        pdc.set(getKey(block), PersistentDataType.LIST.strings(), list);
        if (list.isEmpty()) {
            // yes, this will retry removing all heads, but worth it for only one cleanup
            // task
            removeAll(block);
            return;
        }
    }

    public static void removeAll(Block block) {
        var pdc = block.getChunk().getPersistentDataContainer();
        var list = pdc.get(getKey(block), PersistentDataType.LIST.strings());
        for (var uuid : list) {
            var entity = block.getWorld().getEntity(UUID.fromString(uuid));
            if (entity != null) {
                entity.remove();
                var de = (ItemDisplay) entity;
                block.getWorld().spawnEntity(de.getLocation(), EntityType.ITEM,
                        SpawnReason.NATURAL, (e) -> {
                            var item = (Item) e;
                            item.setItemStack(de.getItemStack());
                        });
            }
        }
        pdc.remove(getKey(block));
        // TODO: drop the heads
        block.setType(Material.AIR);
    }

    public static ItemDisplay[] getHeads(Block block) {
        var pdc = block.getChunk().getPersistentDataContainer();
        var list = pdc.get(getKey(block), PersistentDataType.LIST.strings());
        var heads = new ItemDisplay[list.size()];
        for (int i = 0; i < heads.length; i++) {
            var uuid = UUID.fromString(list.get(i));
            heads[i] = (ItemDisplay) block.getWorld().getEntity(uuid);
        }
        return heads;
    }

    public static int getCount(Block block) {
        var pdc = block.getChunk().getPersistentDataContainer();
        var list = pdc.get(getKey(block), PersistentDataType.LIST.strings());
        return list == null ? 0 : list.size();
    }

    public static void changeHitbox(Player player, Block block) {
        if (!HeadmateStore.has(block))
            return;
        // change hitbox. block -> drop & void, void -> barrier, barrier -> void
        switch (block.getType()) {
            case STRUCTURE_VOID:
                block.setType(Material.BARRIER);
                player.sendActionBar(Component.text("-> Solid block", NamedTextColor.RED));
                break;
            case BARRIER:
                block.setType(Material.STRUCTURE_VOID);
                player.sendActionBar(Component.text("-> Pass-through", NamedTextColor.RED));
                break;
            default:
                block.breakNaturally();
                block.setType(Material.STRUCTURE_VOID);
                player.sendActionBar(Component.text("-> Pass-through (dropped)",
                        NamedTextColor.RED));
                break;
        }
    }

    private static NamespacedKey getKey(Block block) {
        return new NamespacedKey(ThisPlugin.instance,
                "hm-" + block.getX() + "-" + block.getY() + "-" + block.getZ());
    }
}
