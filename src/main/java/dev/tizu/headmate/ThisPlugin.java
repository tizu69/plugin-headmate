package dev.tizu.headmate;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import dev.tizu.headmate.backend.HeadmateListener;
import dev.tizu.headmate.ui.Menu;

public class ThisPlugin extends JavaPlugin {
    public static ThisPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(new HeadmateListener(), this);
        Bukkit.getPluginManager().registerEvents(new Menu.MenuListener(), this);
    }
}