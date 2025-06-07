package dev.tizu.headmate.editor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import net.kyori.adventure.text.Component;

public class EditorListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        var player = event.getPlayer();
        if (!Editor.isEditing(player))
            return;

        Editor.autoMove(player);

        player.sendActionBar(Component.text("youre moving lmao!!"));
        if (player.isSneaking())
            Editor.stopEditing(player);
    }

}
