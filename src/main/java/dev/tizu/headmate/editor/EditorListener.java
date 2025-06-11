package dev.tizu.headmate.editor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerJoinEvent;

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
    public void onPlayerJoin(PlayerJoinEvent event) {
        Editor.stopEditing(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Editor.stopEditing(event.getBlock());
    }

}
