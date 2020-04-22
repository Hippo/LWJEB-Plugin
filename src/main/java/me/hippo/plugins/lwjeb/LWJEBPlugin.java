package me.hippo.plugins.lwjeb;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * @author Hippo
 * @version 1.0.0, 4/22/20
 * @since 1.0.0
 */
public final class LWJEBPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "LWJEB Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "LWJEB Plugin disabled!");
    }
}
