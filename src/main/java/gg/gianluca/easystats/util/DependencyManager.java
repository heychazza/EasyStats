package gg.gianluca.easystats.util;

import gg.gianluca.easystats.EasyStats;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class DependencyManager {
    private final EasyStats plugin;
    private boolean floodgateEnabled;
    private boolean placeholderAPIEnabled;

    public DependencyManager(EasyStats plugin) {
        this.plugin = plugin;
        checkDependencies();
    }

    private void checkDependencies() {
        // Check Floodgate
        Plugin floodgate = Bukkit.getPluginManager().getPlugin("floodgate");
        floodgateEnabled = floodgate != null && floodgate.isEnabled();
        if (floodgateEnabled) {
            plugin.getLogger().info("Floodgate found! Bedrock player detection enabled.");
        } else {
            plugin.getLogger().info("Floodgate not found. Bedrock player detection disabled.");
        }

        // Check PlaceholderAPI
        Plugin placeholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        placeholderAPIEnabled = placeholderAPI != null && placeholderAPI.isEnabled();
        if (placeholderAPIEnabled) {
            plugin.getLogger().info("PlaceholderAPI found! Placeholders enabled.");
        } else {
            plugin.getLogger().info("PlaceholderAPI not found. Placeholders disabled.");
        }
    }

    public boolean isFloodgateEnabled() {
        return floodgateEnabled;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public String getClientType(String uuid) {
        if (!floodgateEnabled) {
            return "java"; // Default to Java if Floodgate is not available
        }

        try {
            // Use reflection to avoid direct dependency on Floodgate API
            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object floodgateApi = floodgateApiClass.getMethod("getInstance").invoke(null);
            boolean isFloodgatePlayer = (boolean) floodgateApiClass.getMethod("isFloodgatePlayer", String.class)
                    .invoke(floodgateApi, uuid);

            return isFloodgatePlayer ? "bedrock" : "java";
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check player platform type: " + e.getMessage());
            return "java"; // Default to Java on error
        }
    }
} 