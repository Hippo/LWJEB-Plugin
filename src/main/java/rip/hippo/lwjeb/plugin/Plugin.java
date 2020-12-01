package rip.hippo.lwjeb.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * @author Hippo
 * @version 2.0.0, 12/1/20
 * @since 2.0.0
 */
public final class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getLogger().log(Level.INFO, "Using LWJEB Plugin by Hippo.");
        super.onEnable();
    }
}
