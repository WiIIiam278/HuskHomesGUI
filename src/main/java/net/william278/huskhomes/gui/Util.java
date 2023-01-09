package net.william278.huskhomes.gui;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class Util {

    // 获取配置
    public static FileConfiguration fromConfig() {
        return HuskHomesGui.getInstance().getConfig();
    }

    // 获取布尔值
    public static Boolean getBooleanFromConfig(String path) {
        return fromConfig().getBoolean(path);
    }

    // 获取消息
    public static String getMessageFromConfig(String path) {
        return fromConfig().getString(path);
    }

    // 获取物品
    public static Material getItemFromConfig(String path) {
        return Material.valueOf(fromConfig().getString(path));
    }

}
