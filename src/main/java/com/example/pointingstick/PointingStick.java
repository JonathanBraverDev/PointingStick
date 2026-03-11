package com.example.pointingstick;

import com.example.pointingstick.commands.PointingStickCommand;
import com.example.pointingstick.listeners.PingListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PointingStick extends JavaPlugin {

    private static PointingStick instance;

    @Override
    public void onEnable() {
        instance = this;
        
        // Register commands
        getCommand("pointingstick").setExecutor(new PointingStickCommand());

        // Register listeners
        getServer().getPluginManager().registerEvents(new PingListener(this), this);

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
