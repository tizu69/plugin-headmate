package dev.tizu.headmate.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Input;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import dev.tizu.headmate.util.Transformers;
import net.kyori.adventure.text.Component;

public class Editor {
    private static Map<UUID, EditorInstance> playerEditings = new HashMap<>();

    public static void startEditing(Player player, ItemDisplay head) {
        if (Editor.isEditing(player))
            stopEditing(player);

        var previousSlowness = player.getPotionEffect(PotionEffectType.SLOWNESS);
        var previousJump = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                PotionEffect.INFINITE_DURATION, 6, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST,
                PotionEffect.INFINITE_DURATION, 127, false, false, false));

        var instance = new EditorInstance(head, previousSlowness, previousJump);
        playerEditings.put(player.getUniqueId(), instance);

        player.sendActionBar(Component.text("Press ").append(Component.keybind("key.sneak"))
                .append(Component.text(" to Finalize")));
    }

    public static void stopEditing(Player player) {
        var instance = playerEditings.get(player.getUniqueId());
        if (instance == null)
            return;

        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        if (instance.prePotionSlowness != null)
            player.addPotionEffect(instance.prePotionSlowness);
        if (instance.prePotionJump != null)
            player.addPotionEffect(instance.prePotionJump);

        playerEditings.remove(player.getUniqueId());
    }

    public static boolean isEditing(Player player) {
        return playerEditings.containsKey(player.getUniqueId());
    }

    public static void handleInputMove(Player player, Input input) {
        var inst = playerEditings.get(player.getUniqueId());
        if (inst == null)
            return;

        var movementOffset = new Vector3d();
        var yaw = Math.toRadians(player.getYaw());

        if (input.isForward())
            movementOffset.add(-Math.sin(yaw), 0, Math.cos(yaw));
        if (input.isBackward())
            movementOffset.add(Math.sin(yaw), 0, -Math.cos(yaw));
        if (input.isLeft())
            movementOffset.add(Math.cos(yaw), 0, Math.sin(yaw));
        if (input.isRight())
            movementOffset.add(-Math.cos(yaw), 0, -Math.sin(yaw));
        if (input.isJump())
            movementOffset.add(0, 0.2, 0);
        if (input.isSprint())
            movementOffset.add(0, -0.2, 0);
        player.setFlying(false);

        var trans = inst.head.getTransformation();
        var transPos = trans.getTranslation();
        transPos.add(Transformers.turnIntoGenericOffset(movementOffset, 0.0625f));
        transPos.min(new Vector3f(1));
        transPos.max(new Vector3f(-1));
        inst.head.setTransformation(new Transformation(transPos, trans.getLeftRotation(),
                trans.getScale(), trans.getRightRotation()));
    }

    public record EditorInstance(ItemDisplay head, PotionEffect prePotionSlowness,
            PotionEffect prePotionJump) {
    }
}
