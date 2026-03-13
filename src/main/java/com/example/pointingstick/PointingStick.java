package com.example.pointingstick;

import com.example.pointingstick.commands.PointingStickCommand;
import com.example.pointingstick.listeners.PingListener;
import com.example.pointingstick.listeners.StickItemListener;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class PointingStick extends JavaPlugin {

    private static PointingStick instance;
    public static NamespacedKey OWNER_KEY;
    public static NamespacedKey COLOR_KEY;
    public static NamespacedKey SOUND_KEY;
    public static NamespacedKey IS_STICK_KEY;

    @Override
    public void onEnable() {
        instance = this;
        OWNER_KEY = new NamespacedKey(this, "owner_uuid");
        COLOR_KEY = new NamespacedKey(this, "ping_color");
        SOUND_KEY = new NamespacedKey(this, "ping_sound");
        IS_STICK_KEY = new NamespacedKey(this, "is_pointing_stick");
        
        // Register commands
        PointingStickCommand cmd = new PointingStickCommand();
        getCommand("pointingstick").setExecutor(cmd);
        getCommand("pointingstick").setTabCompleter(cmd);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PingListener(this), this);
        getServer().getPluginManager().registerEvents(new StickItemListener(), this);

        getLogger().info("PointingStick has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PointingStick has been disabled!");
    }

    public static PointingStick getInstance() {
        return instance;
    }
}
