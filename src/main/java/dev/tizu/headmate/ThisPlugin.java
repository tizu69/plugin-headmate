package dev.tizu.headmate;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import dev.tizu.headmate.editor.EditorListener;
import dev.tizu.headmate.menu.Menu;
import dev.tizu.headmate.wand.WandListener;

public class ThisPlugin extends JavaPlugin {
    public static ThisPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(new EditorListener(), this);
        Bukkit.getPluginManager().registerEvents(new Menu.MenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new WandListener(), this);
    }
}