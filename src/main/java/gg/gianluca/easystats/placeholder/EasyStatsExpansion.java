package gg.gianluca.easystats.placeholder;

import gg.gianluca.easystats.EasyStats;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import java.util.Map;

public class EasyStatsExpansion extends PlaceholderExpansion {

    private final EasyStats plugin;

    public EasyStatsExpansion(EasyStats plugin) {
        this.plugin = plugin;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params == null) return null;

        String[] args = params.toLowerCase().split("_");
        if (args.length < 2) return null;

        switch (args[0]) {
            case "playercount":
                if (args.length < 3) return null;
                String platform = args[1];
                String metric = args[2];
                Map<String, Object> stats = plugin.getDataManager().getPlayerCountStats(platform);
                
                switch (metric) {
                    case "current":
                        return String.valueOf(stats.getOrDefault("current", 0));
                    case "24h":
                        return String.valueOf(stats.getOrDefault("avg_24h", 0));
                    case "7d":
                        return String.valueOf(stats.getOrDefault("avg_7d", 0));
                    case "14d":
                        return String.valueOf(stats.getOrDefault("avg_14d", 0));
                    case "30d":
                        return String.valueOf(stats.getOrDefault("avg_30d", 0));
                    case "peak":
                        return String.valueOf(stats.getOrDefault("peak_count", 0));
                    case "peak_time":
                        return String.valueOf(stats.getOrDefault("peak_time", "N/A"));
                    default:
                        return null;
                }

            case "globalplayercount":
                if (args.length < 2) return null;
                metric = args[1];
                stats = plugin.getDataManager().getPlayerCountStats("global");
                
                switch (metric) {
                    case "current":
                        return String.valueOf(stats.getOrDefault("current", 0));
                    case "24h":
                        return String.valueOf(stats.getOrDefault("avg_24h", 0));
                    case "7d":
                        return String.valueOf(stats.getOrDefault("avg_7d", 0));
                    case "14d":
                        return String.valueOf(stats.getOrDefault("avg_14d", 0));
                    case "30d":
                        return String.valueOf(stats.getOrDefault("avg_30d", 0));
                    case "peak":
                        return String.valueOf(stats.getOrDefault("peak_count", 0));
                    case "peak_time":
                        return String.valueOf(stats.getOrDefault("peak_time", "N/A"));
                    default:
                        return null;
                }

            default:
                return null;
        }
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "easystats";
    }

    @Override
    public String getAuthor() {
        return "Gianluca";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
} 