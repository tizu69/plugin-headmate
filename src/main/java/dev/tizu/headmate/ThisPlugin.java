package dev.tizu.headmate;

import dev.tizu.headmate.editor.EditorListener;
import dev.tizu.headmate.headmate.HeadmateMigrators;
import dev.tizu.headmate.menu.Menu;
import dev.tizu.headmate.wand.WandListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ThisPlugin extends JavaPlugin {

	private static ThisPlugin instance;

	public static ThisPlugin i() {
		return instance;
	}

	@Override
	public void onEnable() {
		saveResource("config.yml", /* replace */ false);
		saveDefaultConfig();

		instance = this;
		Bukkit.getPluginManager().registerEvents(new EditorListener(), this);
		Bukkit.getPluginManager().registerEvents(
			new HeadmateMigrators.Listeners(),
			this
		);
		Bukkit.getPluginManager().registerEvents(new Menu.MenuListener(), this);
		Bukkit.getPluginManager().registerEvents(new WandListener(), this);
	}
}
