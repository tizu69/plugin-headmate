package dev.tizu.headmate.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Editor {
    private static Map<UUID, EditorInstance> playerEditings = new HashMap<>();

    public static void startEditing(Player player, ItemDisplay head) {
        if (Editor.isEditing(player))
            stopEditing(player);

        var previousSlowness = player.getPotionEffect(PotionEffectType.SLOWNESS);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION,
                5, false, false, false));

        var pos = player.getLocation().toVector().toVector3d();
        var facing = new Vector2f(player.getYaw(), player.getPitch());
        var instance = new EditorInstance(head, previousSlowness, pos, facing);
        playerEditings.put(player.getUniqueId(), instance);
    }

    public static void stopEditing(Player player) {
        var instance = playerEditings.get(player.getUniqueId());
        if (instance == null)
            return;

        player.removePotionEffect(PotionEffectType.SLOWNESS);
        if (instance.prePotion != null)
            player.addPotionEffect(instance.prePotion);

        playerEditings.remove(player.getUniqueId());
    }

    public static boolean isEditing(Player player) {
        return playerEditings.containsKey(player.getUniqueId());
    }

    public static void autoMove(Player player) {
        var inst = playerEditings.get(player.getUniqueId());
        if (inst == null)
            return;

        var loc = player.getLocation().clone();
        loc.set(inst.pos.x, inst.pos.y, inst.pos.z);
        if (loc.equals(player.getLocation()))
            return;
        player.teleport(loc);

        var trans = inst.head.getTransformation();
        inst.head.setTransformation(new Transformation(new Vector3f(),
                trans.getLeftRotation(), trans.getScale(), trans.getRightRotation()));
    }

    public record EditorInstance(ItemDisplay head, PotionEffect prePotion,
            Vector3d pos, Vector2f rot) {
    }
}
