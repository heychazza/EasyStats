package gg.gianluca.easystats.expansion;

import gg.gianluca.easystats.EasyStats;
import gg.gianluca.easystats.api.EasyStatsAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class EasyStatsExpansion extends PlaceholderExpansion {
    private final EasyStats plugin;
    private final EasyStatsAPI api;

    public EasyStatsExpansion(EasyStats plugin) {
        this.plugin = plugin;
        this.api = plugin.getAPI();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "easystats";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Gianluca";
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("_");
        if (args.length < 2) return null;

        String type = args[0].toLowerCase();
        String platform = args[1];

        switch (type) {
            case "platform":
                return handlePlatformPlaceholder(platform, args);
            case "country":
                return handleCountryPlaceholder(platform, args);
            case "revenue":
                return handleRevenuePlaceholder(platform, args);
            case "campaign":
                return handleCampaignPlaceholder(platform, args);
            case "session":
                return handleSessionPlaceholder(platform, args);
            default:
                return null;
        }
    }

    private String handlePlatformPlaceholder(String platform, String[] args) {
        if (args.length < 3) return null;

        Map<String, Long> stats = api.getPlatformStats(platform, null);
        String stat = args[2].toLowerCase();

        switch (stat) {
            case "total":
                return String.valueOf(stats.getOrDefault("total", 0L));
            case "java":
                return String.valueOf(stats.getOrDefault("java", 0L));
            case "bedrock":
                return String.valueOf(stats.getOrDefault("bedrock", 0L));
            case "java_percent":
                return api.formatPercentage(stats.getOrDefault("java", 0L) * 100.0 / stats.getOrDefault("total", 1L));
            case "bedrock_percent":
                return api.formatPercentage(stats.getOrDefault("bedrock", 0L) * 100.0 / stats.getOrDefault("total", 1L));
            default:
                return null;
        }
    }

    private String handleCountryPlaceholder(String platform, String[] args) {
        if (args.length < 4) return null;

        Map<String, Map<String, Map<String, Long>>> stats = api.getCountryStats(platform, null);
        String tier = args[2].toUpperCase();
        String country = args[3].toUpperCase();

        if (stats.containsKey(tier) && stats.get(tier).containsKey(country)) {
            Map<String, Long> clientStats = stats.get(tier).get(country);
            return String.valueOf(clientStats.values().stream().mapToLong(Long::longValue).sum());
        }
        return "0";
    }

    private String handleRevenuePlaceholder(String platform, String[] args) {
        if (args.length < 3) return null;

        Map<String, Double> stats = api.getRevenueStats(platform, null);
        String currency = args[2].toUpperCase();

        return api.formatNumber(stats.getOrDefault(currency, 0.0));
    }

    private String handleCampaignPlaceholder(String platform, String[] args) {
        if (args.length < 3) return null;

        String campaignName = args[2];
        Map<String, Object> campaign = api.getCampaign(campaignName);
        if (campaign == null) return "0";

        if (args.length > 3) {
            String stat = args[3].toLowerCase();
            double cost = (Double) campaign.get("cost");
            double revenue = (Double) campaign.get("total_revenue");
            double profit = revenue - cost;
            double roi = cost > 0 ? (profit / cost) * 100 : 0;

            switch (stat) {
                case "cost":
                    return api.formatNumber(cost);
                case "roi":
                    return api.formatPercentage(roi);
                case "profit":
                    return api.formatNumber(profit);
                default:
                    return api.formatNumber(revenue);
            }
        }

        return api.formatNumber((Double) campaign.getOrDefault("total_revenue", 0.0));
    }

    private String handleSessionPlaceholder(String platform, String[] args) {
        if (args.length < 3) return null;

        String stat = args[2].toLowerCase();
        switch (stat) {
            case "avg":
                return api.formatDuration((long) (api.getAverageSessionTime(platform) * 1000));
            case "total":
                Map<String, Double> comparison = api.compareSessionTimes(platform, platform);
                return api.formatDuration((long) (comparison.get("hostname1_avg") * 1000));
            default:
                return null;
        }
    }
} 