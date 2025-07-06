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

import dev.tizu.headmate.ThisPlugin;
import dev.tizu.headmate.headmate.HeadmateStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Editor {
    private static Map<UUID, EditorInstance> playerEditings = new HashMap<>();

    private static final NamespacedKey EDITING_KEY = new NamespacedKey(ThisPlugin.instance, "editing");

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
        var editorInst = playerEditings.get(player.getUniqueId());
        if (editorInst == null)
            return;

        var headInst = HeadmateStore.get(editorInst.head);
        switch (editorInst.mode) {
            case MOVE:
                var yaw = Math.toRadians(player.getYaw());
                if (input.isForward())
                    headInst.move((float) -Math.sin(yaw), 0, (float) Math.cos(yaw), true);
                if (input.isBackward())
                    headInst.move((float) Math.sin(yaw), 0, (float) -Math.cos(yaw), true);
                if (input.isLeft())
                    headInst.move((float) Math.cos(yaw), 0, (float) Math.sin(yaw), true);
                if (input.isRight())
                    headInst.move((float) -Math.cos(yaw), 0, (float) -Math.sin(yaw), true);
                if (input.isJump())
                    headInst.move(0, 1, 0, true);
                if (input.isSprint())
                    headInst.move(0, -1, 0, true);
                break;
            case TRANSFORM:
                if (input.isForward())
                    headInst.rotateUp();
                if (input.isBackward())
                    headInst.rotateDown();
                if (input.isLeft())
                    headInst.rotateLeft();
                if (input.isRight())
                    headInst.rotateRight();
                if (input.isJump())
                    headInst.scaleUp();
                if (input.isSprint())
                    headInst.scaleDown();
                break;
        }
        player.setFlying(false);

        HeadmateStore.set(editorInst.head, headInst);
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
        }, 20);
    }

    public static void showHowTo(Player player) {
        var instance = playerEditings.get(player.getUniqueId());
        if (instance == null)
            return;
        if (instance.mode == EditorMode.MOVE)
            player.sendActionBar(Component.text("Move, Jump, Sprint to move, Sneak to save",
                    NamedTextColor.GRAY));
        else if (instance.mode == EditorMode.TRANSFORM)
            player.sendActionBar(Component.text("Move to rotate, Jump/Sprint to scale",
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
