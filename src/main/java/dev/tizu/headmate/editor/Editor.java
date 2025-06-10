package dev.tizu.headmate.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Input;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Vector3d;
import org.joml.Vector3f;

import dev.tizu.headmate.ThisPlugin;
import dev.tizu.headmate.headmate.HeadmateStore;
import dev.tizu.headmate.util.Transformers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Editor {
    private static Map<UUID, EditorInstance> playerEditings = new HashMap<>();

    private static final NamespacedKey EDITING_KEY = new NamespacedKey(ThisPlugin.instance, "editing");
    private static final float SCALE_INCREMENT = 1f / 4f;
    private static final float TRANSFORM_MAX = 1f;

    public static void startEditing(Player player, Block block, ItemDisplay head) {
        stopEditing(player);

        player.getAttribute(Attribute.MOVEMENT_SPEED)
                .addModifier(new AttributeModifier(EDITING_KEY, -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        player.getAttribute(Attribute.JUMP_STRENGTH)
                .addModifier(new AttributeModifier(EDITING_KEY, -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1));

        var canFly = player.getAllowFlight();
        player.setAllowFlight(false);

        var instance = new EditorInstance(block, head, EditorMode.MOVE, -1, canFly);
        playerEditings.put(player.getUniqueId(), instance);
        showHowTo(player);
    }

    public static void stopEditing(Player player) {
        player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(EDITING_KEY);
        player.getAttribute(Attribute.JUMP_STRENGTH).removeModifier(EDITING_KEY);

        var instance = playerEditings.get(player.getUniqueId());
        if (instance == null)
            return;

        if (instance.canFly)
            player.setAllowFlight(true);
        playerEditings.remove(player.getUniqueId());
    }

    public static void stopEditing(Block block) {
        for (var entry : playerEditings.entrySet())
            if (entry.getValue().block.getLocation().equals(block.getLocation()))
                stopEditing(ThisPlugin.instance.getServer().getPlayer(entry.getKey()));
    }

    public static boolean isEditing(Player player) {
        return playerEditings.containsKey(player.getUniqueId());
    }

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
                if (input.isSprint())
                    HeadmateStore.changeHitbox(player, inst.block);
                break;
        }
        player.setFlying(false);

        float previousScale = trans.getScale().x;
        float newScale = Math.min(Math.max(previousScale + scale, SCALE_INCREMENT), 2.5f);
        float scaleOffset = (newScale - previousScale) / 2f;

        var transPos = trans.getTranslation();
        transPos.add(Transformers.turnIntoGenericOffset(movementOffset, 0.0625f))
                .add(0, scaleOffset, 0).min(new Vector3f(TRANSFORM_MAX)).max(new Vector3f(-TRANSFORM_MAX));

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
            player.sendActionBar(Component.text("Saving... To edit again, shift-click with steve head.",
                    NamedTextColor.GREEN));
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
            player.sendActionBar(Component.text("Left/Right to rotate, Forward/Backward to scale, Sprint to hitbox",
                    NamedTextColor.GRAY));
    }

    public record EditorInstance(Block block, ItemDisplay head, EditorMode mode, int shiftDown,
            boolean canFly) {
        public EditorInstance shiftDown(int to) {
            return new EditorInstance(block(), head(), mode(), to, canFly());
        }

        public EditorInstance mode(EditorMode mode) {
            return new EditorInstance(block(), head(), mode, shiftDown(), canFly());
        }
    }

    public enum EditorMode {
        MOVE, TRANSFORM;

        public EditorMode next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }
}
