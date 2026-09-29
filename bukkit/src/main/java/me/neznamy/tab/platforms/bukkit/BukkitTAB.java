package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.platform.FoliaPlatform;
import me.neznamy.tab.shared.ProjectVariables;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main class for Bukkit.
 */
public class BukkitTAB extends JavaPlugin {

    @Override
    public void onEnable() {
        boolean folia = ReflectionUtils.classExists("io.papermc.paper.threadedregions.RegionizedServer");
        try {
            TAB.create(folia ? new FoliaPlatform(this) : new BukkitPlatform(this));
        } catch (IllegalStateException e) {
            Bukkit.getConsoleSender().sendMessage("§c[TAB] ================================================================================");
            if (isSpecialJar()) {
                Bukkit.getConsoleSender().sendMessage("§c[TAB] The " + ProjectVariables.PLUGIN_VERSION + " release consists of 2 jars due to Java version problems.");
                Bukkit.getConsoleSender().sendMessage("§c[TAB] One jar is for Paper 1.21 - 1.21.4, the other jar is for everything else.");
                Bukkit.getConsoleSender().sendMessage("§c[TAB] You have installed the jar that explicitly says \"Paper 1.21 - 1.21.4\", which does not support your server version (" + Bukkit.getBukkitVersion() + ").");
                Bukkit.getConsoleSender().sendMessage("§c[TAB] Use the other jar in the release instead.");
            } else {
                Bukkit.getConsoleSender().sendMessage("§c[TAB] Your server version (" + Bukkit.getBukkitVersion() + ") is not supported.");
                Bukkit.getConsoleSender().sendMessage("§c[TAB] This jar only supports 1.21.x and 26.x");
                Bukkit.getConsoleSender().sendMessage("§c[TAB] If you just updated to a new Minecraft version, check for TAB updates.");
                Bukkit.getConsoleSender().sendMessage("§c[TAB] Thrown error message: " + e.getMessage());
            }
            Bukkit.getConsoleSender().sendMessage("§c[TAB] ================================================================================");
        }
    }

    private boolean isSpecialJar() {
        return !moduleExists("v1_21_R1") &&
                moduleExists("paper_1_21") && moduleExists("paper_1_21_2") && moduleExists("paper_1_21_4") &&
                !moduleExists("paper_1_21_9");
    }

    private boolean moduleExists(@NotNull String module) {
        return ReflectionUtils.classExists("me.neznamy.tab.platforms.bukkit." + module + ".NMSImplementationProvider");
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() == null) return;
        TAB.getInstance().unload();
    }
}