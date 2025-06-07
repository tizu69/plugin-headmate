package dev.tizu.headmate;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;

public class HeadmateStore {
    public static boolean has(Block block) {
        if (block.getType() != Material.BARRIER)
            return false;
        var pdc = block.getChunk().getPersistentDataContainer();
        return pdc.has(getKey(block), PersistentDataType.LIST.strings());
    }

    public static void create(Block block) {
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD)
            throw new IllegalArgumentException("Invalid block type, got " + block.getType());

        var skull = (Skull) block.getState();
        block.setType(Material.BARRIER);
        HeadmateStore.add(block, skull);
    }

    public static void add(Block block, Skull skull) {
        var pdc = block.getChunk().getPersistentDataContainer();
        var list = pdc.get(getKey(block), PersistentDataType.LIST.strings());
        if (list == null)
            list = new ArrayList<>();

        var world = block.getWorld();
        var loc = block.getLocation();

        // transfer the player head to the item display
        var item = new ItemStack(Material.PLAYER_HEAD);
        if (skull.hasOwner())
            item.setData(DataComponentTypes.PROFILE,
                    ResolvableProfile.resolvableProfile(skull.getPlayerProfile()));

        var entity = world.spawnEntity(loc, EntityType.ITEM_DISPLAY, SpawnReason.CUSTOM, (e) -> {
            var id = (ItemDisplay) e;
            id.setItemStack(item);
            id.setTransformation(new Transformation(new Vector3f(0.5f, 0.5f, 0.5f),
                    new AxisAngle4f(0, 0, 0, 0), new Vector3f(1, 1, 1),
                    Maths.fromEuler(0, 45, 0)));
        });
        list.add(entity.getUniqueId().toString());
        pdc.set(getKey(block), PersistentDataType.LIST.strings(), list);
    }

    public static void removeAll(Block block) {
        var pdc = block.getChunk().getPersistentDataContainer();
        var list = pdc.get(getKey(block), PersistentDataType.LIST.strings());
        for (var uuid : list) {
            var entity = block.getWorld().getEntity(UUID.fromString(uuid));
            if (entity != null)
                entity.remove();
        }
        pdc.remove(getKey(block));
        // TODO: drop the heads
        block.setType(Material.AIR);
    }

    private static NamespacedKey getKey(Block block) {
        return new NamespacedKey(ThisPlugin.instance,
                "hm-" + block.getX() + "-" + block.getY() + "-" + block.getZ());
    }
}
