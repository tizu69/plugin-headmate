package dev.tizu.headmate.editor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;

public class EditorListener implements Listener {

    @EventHandler
    public void onPlayerInput(PlayerInputEvent event) {
        var player = event.getPlayer();
        if (!Editor.isEditing(player))
            return;

        var input = event.getInput();
        Editor.autoMove(player);

        if (input.isSneak())
            Editor.stopEditing(player);
    }

}
