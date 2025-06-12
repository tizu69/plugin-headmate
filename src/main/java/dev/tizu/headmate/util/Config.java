package dev.tizu.headmate.util;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import dev.tizu.headmate.ThisPlugin;

public class Config {
    public static float anchorDistanceLimit() {
        return (float) ThisPlugin.instance.getConfig().getDouble("anchorDistanceLimit");
    };

    public static float maxSideLength() {
        return (float) ThisPlugin.instance.getConfig().getDouble("maxSideLength");
    };

    public static float minSideLength() {
        return (float) ThisPlugin.instance.getConfig().getDouble("minSideLength");
    };

    public static int maxHeads() {
        return ThisPlugin.instance.getConfig().getInt("maxHeads");
    };

    public static void loadConfig() {
        /*
         * File file = new File(ThisPlugin.instance.getDataFolder(), "config.yml");
         * var config = YamlConfiguration.loadConfiguration(file);
         * 
         * config.addDefault("anchorDistanceLimit", anchorDistanceLimit);
         * config.addDefault("maxSideLength", maxSideLength);
         * config.addDefault("minSideLength", minSideLength);
         * config.addDefault("maxHeads", maxHeads);
         * Config.anchorDistanceLimit = (float) config.getDouble("anchorDistanceLimit");
         * Config.maxSideLength = (float) config.getDouble("maxSideLength");
         * Config.minSideLength = (float) config.getDouble("minSideLength");
         * Config.maxHeads = config.getInt("maxHeads");
         * 
         * try {
         * config.save(file);
         * } catch (IOException e) {
         * ThisPlugin.instance.getLogger().warning(e.getMessage());
         * }
         */
    }
}
