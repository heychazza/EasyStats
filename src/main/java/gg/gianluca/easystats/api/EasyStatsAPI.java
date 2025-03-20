package gg.gianluca.easystats.api;

import java.util.Map;
import java.util.List;
import java.util.UUID;

public interface EasyStatsAPI {
    // Platform Statistics
    Map<String, Long> getPlatformStats(String platform, String timeFilter);
    Map<String, Long> comparePlatforms(String platform1, String platform2);

    // Country Statistics
    Map<String, Map<String, Long>> getCountryStats(String platform, String timeFilter);
    Map<String, Map<String, Long>> compareCountries(String platform1, String platform2);

    // Revenue Statistics
    Map<String, Double> getRevenueStats(String platform, String timeFilter);
    Map<String, Double> compareRevenue(String platform1, String platform2);
    void addRevenue(String platform, double amount, String currency);

    // Campaign Management
    void createCampaign(String name, String description, String startDate, String endDate, String currency, double cost);
    Map<String, Object> getCampaign(String name);
    List<Map<String, Object>> getAllCampaigns();
    void endCampaign(String name);
    boolean addHostnameToCampaign(String name, String hostname);
    boolean removeHostnameFromCampaign(String name, String hostname);
    List<String> getCampaignHostnames(String name);

    // Session Statistics
    double getAverageSessionTime(String hostname);
    Map<String, Double> compareSessionTimes(String hostname1, String hostname2);
    void startSession(UUID playerId, String hostname);
    void endSession(UUID playerId, String hostname);

    // Utility Methods
    String formatDuration(long milliseconds);
    String formatNumber(double number);
    String formatPercentage(double percentage);
} 