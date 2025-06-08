package dev.tizu.headmate.editor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.tizu.headmate.headmate.HeadmateStore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class EditorListener implements Listener {

    @EventHandler
    public void onPlayerInput(PlayerInputEvent event) {
        var player = event.getPlayer();
        if (!Editor.isEditing(player))
            return;

        var input = event.getInput();
        Editor.handleInputMove(player, input);
        Editor.handleInputControl(player, input);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        if (!Editor.isEditing(player))
            return;
        Editor.stopEditing(player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Editor.stopEditing(event.getBlock());
    }

}
