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
import org.joml.Vector3d;
import org.joml.Vector3f;

import dev.tizu.headmate.ThisPlugin;
import dev.tizu.headmate.util.Transformers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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

        var instance = new EditorInstance(head, previousSlowness, previousJump, EditorMode.MOVE, -1);
        playerEditings.put(player.getUniqueId(), instance);
        showHowTo(player);
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

    private static final float SCALE_INCREMENT = 1f / 4f;

    public static void handleInputMove(Player player, Input input) {
        var inst = playerEditings.get(player.getUniqueId());
        if (inst == null)
            return;

        var trans = inst.head.getTransformation();
        var movementOffset = new Vector3d();
        var rotation = trans.getLeftRotation();
        var scale = 0f;

        switch (inst.mode) {
            case MOVE:
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
                break;
            case TRANSFORM:
                if (input.isForward())
                    scale += Editor.SCALE_INCREMENT;
                if (input.isBackward())
                    scale -= Editor.SCALE_INCREMENT;
                if (input.isLeft())
                    rotation = Transformers.getRot(Transformers.getRotIndex(rotation) - 1);
                if (input.isRight())
                    rotation = Transformers.getRot(Transformers.getRotIndex(rotation) + 1);
                break;
        }
        player.setFlying(false);

        float previousScale = trans.getScale().x;
        float newScale = Math.min(Math.max(previousScale + scale, SCALE_INCREMENT), 2.5f);
        float scaleOffset = (newScale - previousScale) / 2f;

        var transPos = trans.getTranslation();
        transPos.add(Transformers.turnIntoGenericOffset(movementOffset, 0.0625f))
                .add(0, scaleOffset, 0).min(new Vector3f(1)).max(new Vector3f(-1));
        inst.head.setTransformation(new Transformation(transPos, rotation,
                new Vector3f(newScale), trans.getRightRotation()));
    }

    public static void handleInputControl(Player player, Input input) {
        var inst = playerEditings.get(player.getUniqueId());
        if (inst == null)
            return;

        if (!input.isSneak() && inst.shiftDown > 0) {
            var newInst = inst.mode(inst.mode.next()).shiftDown(-1);
            playerEditings.put(player.getUniqueId(), newInst);
            showHowTo(player);
            return;
        }
        if (!input.isSneak() || inst.shiftDown > 0)
            return;

        var sneakTime = player.getTicksLived();
        playerEditings.put(player.getUniqueId(), inst.shiftDown(sneakTime));

        player.sendActionBar(Component.text("Hold to save, release to change mode", NamedTextColor.YELLOW));
        player.getServer().getScheduler().runTaskLater(ThisPlugin.instance, (task) -> {
            var current = playerEditings.get(player.getUniqueId());
            if (current == null || current.shiftDown != sneakTime)
                return;

            Editor.stopEditing(player);
            player.sendActionBar(Component.text("Saving...", NamedTextColor.GREEN));
        }, 2 * 20);
    }

    public static void showHowTo(Player player) {
        var instance = playerEditings.get(player.getUniqueId());
        if (instance == null)
            return;
        if (instance.mode == EditorMode.MOVE)
            player.sendActionBar(Component.text("Move, Jump, Sprint to move, Sneak to save",
                    NamedTextColor.GRAY));
        else if (instance.mode == EditorMode.TRANSFORM)
            player.sendActionBar(Component.text("Left/Right to rotate, Forward/Backward to scale",
                    NamedTextColor.GRAY));
    }

    public record EditorInstance(ItemDisplay head, PotionEffect prePotionSlowness,
            PotionEffect prePotionJump, EditorMode mode, int shiftDown) {
        public EditorInstance shiftDown(int to) {
            return new EditorInstance(head(), prePotionSlowness(), prePotionJump(), mode(), to);
        }

        public EditorInstance mode(EditorMode mode) {
            return new EditorInstance(head(), prePotionSlowness(), prePotionJump(), mode, shiftDown());
        }
    }

    public enum EditorMode {
        MOVE, TRANSFORM;

        public EditorMode next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }
}
