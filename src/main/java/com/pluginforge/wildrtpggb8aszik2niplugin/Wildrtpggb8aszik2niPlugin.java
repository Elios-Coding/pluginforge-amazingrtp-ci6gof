package com.pluginforge.wildrtpggb8aszik2niplugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class Wildrtpggb8aszik2niPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        WildrtpCommand command = new WildrtpCommand(this);
        if (getCommand("wildrtp") != null) {
            getCommand("wildrtp").setExecutor(command);
            getCommand("wildrtp").setTabCompleter(command);
        }
        getServer().getPluginManager().registerEvents(command, this);
        getLogger().info("Wildrtpggb8aszik2niPlugin enabled.");
    }
}
