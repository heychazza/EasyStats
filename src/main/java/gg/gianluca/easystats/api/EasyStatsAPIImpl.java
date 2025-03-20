package gg.gianluca.easystats.api;

import gg.gianluca.easystats.EasyStats;
import gg.gianluca.easystats.data.DataManager;
import gg.gianluca.easystats.session.SessionManager;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class EasyStatsAPIImpl implements EasyStatsAPI {
    @SuppressWarnings("unused")
    private final EasyStats plugin;
    private final DataManager dataManager;
    private final SessionManager sessionManager;
    private final DecimalFormat numberFormat;
    private final DecimalFormat percentageFormat;

    public EasyStatsAPIImpl(EasyStats plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.sessionManager = plugin.getSessionManager();
        this.numberFormat = new DecimalFormat("#,##0.00");
        this.percentageFormat = new DecimalFormat("#,##0.00%");
    }

    @Override
    public Map<String, Long> getPlatformStats(String platform, String timeFilter) {
        return dataManager.getPlatformStats(platform, timeFilter);
    }

    @Override
    public Map<String, Long> comparePlatforms(String platform1, String platform2) {
        Map<String, Long> stats1 = getPlatformStats(platform1, null);
        Map<String, Long> stats2 = getPlatformStats(platform2, null);
        return Map.of(
                "platform1_total", stats1.getOrDefault("total", 0L),
                "platform2_total", stats2.getOrDefault("total", 0L),
                "difference", stats1.getOrDefault("total", 0L) - stats2.getOrDefault("total", 0L),
                "percent_difference", calculatePercentage(stats1.getOrDefault("total", 0L), stats2.getOrDefault("total", 0L))
        );
    }

    @Override
    public Map<String, Map<String, Long>> getCountryStats(String platform, String timeFilter) {
        return dataManager.getCountryStats(platform, timeFilter);
    }

    @Override
    public Map<String, Map<String, Long>> compareCountries(String platform1, String platform2) {
        Map<String, Map<String, Long>> stats1 = getCountryStats(platform1, null);
        Map<String, Map<String, Long>> stats2 = getCountryStats(platform2, null);
        
        // Create a result map to store comparison data
        Map<String, Map<String, Long>> comparison = new HashMap<>();
        
        // Process each country tier
        for (String tier : stats1.keySet()) {
            Map<String, Long> tierStats = new HashMap<>();
            Map<String, Long> platform1Stats = stats1.getOrDefault(tier, new HashMap<>());
            Map<String, Long> platform2Stats = stats2.getOrDefault(tier, new HashMap<>());
            
            // Calculate totals for each platform
            long platform1Total = platform1Stats.values().stream().mapToLong(Long::valueOf).sum();
            long platform2Total = platform2Stats.values().stream().mapToLong(Long::valueOf).sum();
            
            // Store comparison metrics
            tierStats.put(platform1 + "_total", platform1Total);
            tierStats.put(platform2 + "_total", platform2Total);
            tierStats.put("difference", platform1Total - platform2Total);
            tierStats.put("percent_difference", calculatePercentage(platform1Total, platform2Total));
            
            comparison.put(tier, tierStats);
        }
        
        // Add any tiers that are only in platform2
        for (String tier : stats2.keySet()) {
            if (!comparison.containsKey(tier)) {
                Map<String, Long> tierStats = new HashMap<>();
                Map<String, Long> platform2Stats = stats2.get(tier);
                
                long platform2Total = platform2Stats.values().stream().mapToLong(Long::valueOf).sum();
                
                tierStats.put(platform1 + "_total", 0L);
                tierStats.put(platform2 + "_total", platform2Total);
                tierStats.put("difference", -platform2Total);
                tierStats.put("percent_difference", -100L);
                
                comparison.put(tier, tierStats);
            }
        }
        
        return comparison;
    }

    @Override
    public Map<String, Double> getRevenueStats(String platform, String timeFilter) {
        return dataManager.getRevenueStats(platform, timeFilter);
    }

    @Override
    public Map<String, Double> compareRevenue(String platform1, String platform2) {
        Map<String, Double> stats1 = getRevenueStats(platform1, null);
        Map<String, Double> stats2 = getRevenueStats(platform2, null);
        
        Map<String, Double> comparison = new HashMap<>();
        
        // Calculate totals for each platform
        double platform1Total = stats1.values().stream().mapToDouble(Double::valueOf).sum();
        double platform2Total = stats2.values().stream().mapToDouble(Double::valueOf).sum();
        
        // Store basic comparison metrics
        comparison.put(platform1 + "_total", platform1Total);
        comparison.put(platform2 + "_total", platform2Total);
        comparison.put("difference", platform1Total - platform2Total);
        
        // Calculate percentage difference
        if (platform2Total != 0) {
            double percentDiff = ((platform1Total - platform2Total) / platform2Total) * 100.0;
            comparison.put("percent_difference", percentDiff);
        } else {
            comparison.put("percent_difference", platform1Total > 0 ? 100.0 : 0.0);
        }
        
        // Add currency-specific comparisons
        Set<String> allCurrencies = new HashSet<>();
        allCurrencies.addAll(stats1.keySet());
        allCurrencies.addAll(stats2.keySet());
        
        for (String currency : allCurrencies) {
            double value1 = stats1.getOrDefault(currency, 0.0);
            double value2 = stats2.getOrDefault(currency, 0.0);
            
            comparison.put(currency + "_" + platform1, value1);
            comparison.put(currency + "_" + platform2, value2);
            comparison.put(currency + "_difference", value1 - value2);
            
            if (value2 != 0) {
                double currencyPercentDiff = ((value1 - value2) / value2) * 100.0;
                comparison.put(currency + "_percent_difference", currencyPercentDiff);
            } else {
                comparison.put(currency + "_percent_difference", value1 > 0 ? 100.0 : 0.0);
            }
        }
        
        return comparison;
    }

    @Override
    public void addRevenue(String platform, double amount, String currency) {
        dataManager.addRevenue(platform, amount, currency);
    }

    @Override
    public void createCampaign(String name, String description, String startDate, String endDate, String currency, double cost) {
        dataManager.createCampaign(name, description, startDate, endDate, currency, cost);
    }

    @Override
    public Map<String, Object> getCampaign(String name) {
        return dataManager.getCampaign(name);
    }

    @Override
    public List<Map<String, Object>> getAllCampaigns() {
        return dataManager.getAllCampaigns();
    }

    @Override
    public void endCampaign(String name) {
        dataManager.endCampaign(name);
    }

    @Override
    public boolean addHostnameToCampaign(String name, String hostname) {
        return dataManager.addHostnameToCampaign(name, hostname);
    }

    @Override
    public boolean removeHostnameFromCampaign(String name, String hostname) {
        return dataManager.removeHostnameFromCampaign(name, hostname);
    }

    @Override
    public List<String> getCampaignHostnames(String name) {
        return dataManager.getCampaignHostnames(name);
    }

    @Override
    public double getAverageSessionTime(String hostname) {
        return sessionManager.getAverageSessionTime(hostname);
    }

    @Override
    public Map<String, Double> compareSessionTimes(String hostname1, String hostname2) {
        return sessionManager.compareSessionTimes(hostname1, hostname2);
    }

    @Override
    public void startSession(UUID playerId, String hostname) {
        sessionManager.startSession(playerId, hostname);
    }

    @Override
    public void endSession(UUID playerId, String hostname) {
        sessionManager.endSession(playerId, hostname);
    }

    @Override
    public String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes %= 60;
        seconds %= 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    @Override
    public String formatNumber(double number) {
        return numberFormat.format(number);
    }

    @Override
    public String formatPercentage(double percentage) {
        return percentageFormat.format(percentage / 100.0);
    }

    private long calculatePercentage(long value1, long value2) {
        if (value2 == 0) return 0;
        return (value1 - value2) * 100 / value2;
    }
} 