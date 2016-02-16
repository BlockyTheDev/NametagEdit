package com.nametagedit.plugin;

import com.nametagedit.plugin.api.INametagApi;
import com.nametagedit.plugin.api.NametagAPI;
import com.nametagedit.plugin.hooks.HookGroupManager;
import com.nametagedit.plugin.hooks.HookPermissionsEX;
import com.nametagedit.plugin.hooks.HookZPermissions;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;

@Getter
public class NametagEdit extends JavaPlugin {

    private static INametagApi api;

    private NametagHandler handler;
    private NametagManager manager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        handler = new NametagHandler(this);
        manager = new NametagManager();

        if (getConfig().getBoolean("MetricsEnabled")) {
            try {
                new Metrics(this).start();
            } catch (IOException e) {
                getLogger().severe("Couldn't start Metrics!");
            }
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        if (checkShouldRegister(pluginManager, "zPermissions")) {
            pluginManager.registerEvents(new HookZPermissions(handler), this);
        } else if (checkShouldRegister(pluginManager, "PermissionsEx")) {
            pluginManager.registerEvents(new HookPermissionsEX(handler), this);
        } else if (checkShouldRegister(pluginManager, "GroupManager")) {
            pluginManager.registerEvents(new HookGroupManager(handler), this);
        }

        getCommand("ne").setExecutor(new NametagCommand(handler));

        if (api == null) {
            api = new NametagAPI(manager);
        }
    }

    @Override
    public void onDisable() {
        manager.reset();
        handler.getAbstractConfig().shutdown();
    }

    public static INametagApi getApi() {
        return api;
    }

    private boolean checkShouldRegister(PluginManager pluginManager, String plugin) {
        if (pluginManager.getPlugin(plugin) == null) {
            return false;
        }
        getLogger().info("Found " + plugin + "! Hooking in.");
        return true;
    }

}