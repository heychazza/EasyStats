package gg.gianluca.easystats.data;

import gg.gianluca.easystats.EasyStats;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataManager {
    private final EasyStats plugin;
    private final FileConfiguration config;

    public DataManager(EasyStats plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection connection = getConnection()) {
            // Create tables if they don't exist
            createTables(connection);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }

    private void createTables(Connection connection) throws SQLException {
        // Create platform_stats table
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS platform_stats (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "platform VARCHAR(50) NOT NULL," +
                    "client_type VARCHAR(20) NOT NULL," +
                    "join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Create country_stats table
            stmt.execute("CREATE TABLE IF NOT EXISTS country_stats (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "platform VARCHAR(50) NOT NULL," +
                    "country VARCHAR(50) NOT NULL," +
                    "tier VARCHAR(10) NOT NULL," +
                    "client_type VARCHAR(20) NOT NULL," +
                    "join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Create revenue table
            stmt.execute("CREATE TABLE IF NOT EXISTS revenue (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "platform VARCHAR(50) NOT NULL," +
                    "amount DECIMAL(10,2) NOT NULL," +
                    "currency VARCHAR(3) NOT NULL," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Create campaigns table
            stmt.execute("CREATE TABLE IF NOT EXISTS campaigns (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(50) NOT NULL UNIQUE," +
                    "description TEXT," +
                    "start_date DATE NOT NULL," +
                    "end_date DATE," +
                    "hostname VARCHAR(255)," +
                    "budget DECIMAL(10,2) NOT NULL" +
                    ")");

            // Create joins table
            stmt.execute("CREATE TABLE IF NOT EXISTS joins (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "uuid VARCHAR(36) NOT NULL," +
                    "platform VARCHAR(50) NOT NULL," +
                    "hostname VARCHAR(255) NOT NULL," +
                    "join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Create player_counts table
            stmt.execute("CREATE TABLE IF NOT EXISTS player_counts (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "platform VARCHAR(50) NOT NULL," +
                    "count INT NOT NULL," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
        }
    }

    private Connection getConnection() throws SQLException {
        String url = config.getString("database.url");
        String username = config.getString("database.username");
        String password = config.getString("database.password");
        return DriverManager.getConnection(url, username, password);
    }

    public void recordJoin(UUID uuid, String platform, String hostname) {
        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO joins (uuid, platform, hostname, join_time) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                statement.setString(2, platform);
                statement.setString(3, hostname);
                statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error recording join: " + e.getMessage());
        }
    }

    public void addRevenue(String platform, double amount, String currency) {
        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO revenue (platform, amount, currency, timestamp) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, platform);
                statement.setDouble(2, amount);
                statement.setString(3, currency);
                statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error adding revenue: " + e.getMessage());
        }
    }

    public void createCampaign(String name, String description, String startDate, String endDate, String hostname, double budget) {
        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO campaigns (name, description, start_date, end_date, hostname, budget) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.setString(2, description);
                statement.setString(3, startDate);
                statement.setString(4, endDate);
                statement.setString(5, hostname);
                statement.setDouble(6, budget);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating campaign: " + e.getMessage());
        }
    }

    public Map<String, Object> getCampaign(String name) {
        try (Connection connection = getConnection()) {
            String sql = "SELECT * FROM campaigns WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Map<String, Object> campaign = new HashMap<>();
                        campaign.put("name", resultSet.getString("name"));
                        campaign.put("description", resultSet.getString("description"));
                        campaign.put("start_date", resultSet.getString("start_date"));
                        campaign.put("end_date", resultSet.getString("end_date"));
                        campaign.put("hostname", resultSet.getString("hostname"));
                        campaign.put("budget", resultSet.getDouble("budget"));
                        return campaign;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting campaign: " + e.getMessage());
        }
        return null;
    }

    public void endCampaign(String name) {
        try (Connection connection = getConnection()) {
            String sql = "UPDATE campaigns SET end_date = ? WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
                statement.setString(2, name);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error ending campaign: " + e.getMessage());
        }
    }

    public boolean addHostnameToCampaign(String name, String hostname) {
        try (Connection connection = getConnection()) {
            String sql = "UPDATE campaigns SET hostname = ? WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, hostname);
                statement.setString(2, name);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error adding hostname to campaign: " + e.getMessage());
            return false;
        }
    }

    public boolean removeHostnameFromCampaign(String name, String hostname) {
        try (Connection connection = getConnection()) {
            String sql = "UPDATE campaigns SET hostname = NULL WHERE name = ? AND hostname = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.setString(2, hostname);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error removing hostname from campaign: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getCampaignMetrics(String name) {
        Map<String, Object> metrics = new HashMap<>();
        try (Connection connection = getConnection()) {
            String sql = "SELECT COUNT(*) as joins, COUNT(DISTINCT uuid) as unique_players FROM joins j " +
                        "INNER JOIN campaigns c ON j.hostname = c.hostname " +
                        "WHERE c.name = ? AND j.join_time BETWEEN c.start_date AND COALESCE(c.end_date, CURRENT_TIMESTAMP)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        metrics.put("total_joins", resultSet.getInt("joins"));
                        metrics.put("unique_players", resultSet.getInt("unique_players"));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting campaign metrics: " + e.getMessage());
        }
        return metrics;
    }

    public Map<String, Long> getPlatformStats(String platform, String timeFilter) {
        Map<String, Long> stats = new HashMap<>();
        String query = timeFilter != null 
            ? "SELECT client_type, COUNT(*) as count FROM platform_stats WHERE platform = ? AND join_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY client_type"
            : "SELECT client_type, COUNT(*) as count FROM platform_stats WHERE platform = ? GROUP BY client_type";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, platform);
            if (timeFilter != null) {
                stmt.setString(2, timeFilter.replace("d", ""));
            }
            ResultSet rs = stmt.executeQuery();

            long total = 0;
            while (rs.next()) {
                String clientType = rs.getString("client_type");
                long count = rs.getLong("count");
                stats.put(clientType.toLowerCase(), count);
                total += count;
            }
            stats.put("total", total);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get platform stats: " + e.getMessage());
        }
        return stats;
    }

    public Map<String, Double> getRevenueStats(String platform, String timeFilter) {
        Map<String, Double> stats = new HashMap<>();
        String query = timeFilter != null 
            ? "SELECT currency, SUM(amount) as total FROM revenue WHERE platform = ? AND timestamp >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY currency"
            : "SELECT currency, SUM(amount) as total FROM revenue WHERE platform = ? GROUP BY currency";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, platform);
            if (timeFilter != null) {
                stmt.setString(2, timeFilter.replace("d", ""));
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String currency = rs.getString("currency");
                double amount = rs.getDouble("total");
                stats.put(currency, amount);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get revenue stats: " + e.getMessage());
        }
        return stats;
    }

    public Map<String, Map<String, Map<String, Long>>> getCountryStats(String platform, String timeFilter) {
        Map<String, Map<String, Map<String, Long>>> stats = new HashMap<>();
        String query = timeFilter != null 
            ? "SELECT country, tier, client_type, COUNT(*) as count FROM country_stats WHERE platform = ? AND join_time >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY country, tier, client_type"
            : "SELECT country, tier, client_type, COUNT(*) as count FROM country_stats WHERE platform = ? GROUP BY country, tier, client_type";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, platform);
            if (timeFilter != null) {
                stmt.setString(2, timeFilter.replace("d", ""));
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String country = rs.getString("country");
                String tier = rs.getString("tier");
                String clientType = rs.getString("client_type").toLowerCase();
                long count = rs.getLong("count");

                if (!stats.containsKey(tier)) {
                    stats.put(tier, new HashMap<>());
                }
                if (!stats.get(tier).containsKey(country)) {
                    stats.get(tier).put(country, new HashMap<>());
                }
                stats.get(tier).get(country).put(clientType, count);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get country stats: " + e.getMessage());
        }
        return stats;
    }

    public Map<String, Object> getPlayerCountStats(String platform) {
        Map<String, Object> stats = new HashMap<>();
        try (Connection connection = getConnection()) {
            // Get current count
            try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT count FROM player_counts WHERE platform = ? AND timestamp = (SELECT MAX(timestamp) FROM player_counts WHERE platform = ?)"
            )) {
                stmt.setString(1, platform);
                stmt.setString(2, platform);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.put("current", rs.getInt("count"));
                }
            }

            // Get averages for different time periods
            Map<String, Integer> averages = new HashMap<>();
            String[] periods = {"24h", "7d", "14d", "30d"};
            for (String period : periods) {
                String timeExpr = switch (period) {
                    case "24h" -> "DATETIME('now', '-1 day')";
                    case "7d" -> "DATETIME('now', '-7 days')";
                    case "14d" -> "DATETIME('now', '-14 days')";
                    case "30d" -> "DATETIME('now', '-30 days')";
                    default -> "DATETIME('now', '-1 day')";
                };
                
                try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT AVG(count) as avg_count FROM player_counts WHERE platform = ? AND timestamp >= " + timeExpr
                )) {
                    stmt.setString(1, platform);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        averages.put(period, (int) Math.floor(rs.getDouble("avg_count")));
                    }
                }
            }
            stats.put("averages", averages);

            // Get peak player count and time
            try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT count, timestamp FROM player_counts WHERE platform = ? ORDER BY count DESC LIMIT 1"
            )) {
                stmt.setString(1, platform);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.put("peak_count", rs.getInt("count"));
                    stats.put("peak_time", rs.getTimestamp("timestamp").toString());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get player count stats: " + e.getMessage());
        }
        return stats;
    }

    public Map<String, Object> getGlobalPlayerCountStats() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection connection = getConnection()) {
            // Get current global count
            try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT SUM(count) as total FROM (SELECT platform, count FROM player_counts pc1 WHERE timestamp = (SELECT MAX(timestamp) FROM player_counts pc2 WHERE pc2.platform = pc1.platform) GROUP BY platform)"
            )) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.put("current", rs.getInt("total"));
                }
            }

            // Get averages for different time periods
            Map<String, Integer> averages = new HashMap<>();
            String[] periods = {"24h", "7d", "14d", "30d"};
            for (String period : periods) {
                String timeExpr = switch (period) {
                    case "24h" -> "DATETIME('now', '-1 day')";
                    case "7d" -> "DATETIME('now', '-7 days')";
                    case "14d" -> "DATETIME('now', '-14 days')";
                    case "30d" -> "DATETIME('now', '-30 days')";
                    default -> "DATETIME('now', '-1 day')";
                };
                
                try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT AVG(total) as avg_count FROM (SELECT timestamp, SUM(count) as total FROM player_counts WHERE timestamp >= " + timeExpr + " GROUP BY timestamp)"
                )) {
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        averages.put(period, (int) Math.floor(rs.getDouble("avg_count")));
                    }
                }
            }
            stats.put("averages", averages);

            // Get peak player count and time
            try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT SUM(count) as total, timestamp FROM player_counts GROUP BY timestamp ORDER BY total DESC LIMIT 1"
            )) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.put("peak_count", rs.getInt("total"));
                    stats.put("peak_time", rs.getTimestamp("timestamp").toString());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get global player count stats: " + e.getMessage());
        }
        return stats;
    }

    public void recordPlayerCount(String platform, int count) {
        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO player_counts (platform, count, timestamp) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, platform);
                statement.setInt(2, count);
                statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error recording player count: " + e.getMessage());
        }
    }

    public List<String> getAllHostnames() {
        List<String> hostnames = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String sql = "SELECT DISTINCT hostname FROM campaigns WHERE hostname IS NOT NULL";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    hostnames.add(rs.getString("hostname"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting all hostnames: " + e.getMessage());
        }
        return hostnames;
    }

    public List<Map<String, Object>> getAllCampaigns() {
        List<Map<String, Object>> campaigns = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String sql = "SELECT * FROM campaigns";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, Object> campaign = new HashMap<>();
                    campaign.put("name", rs.getString("name"));
                    campaign.put("description", rs.getString("description"));
                    campaign.put("start_date", rs.getString("start_date"));
                    campaign.put("end_date", rs.getString("end_date"));
                    campaign.put("hostname", rs.getString("hostname"));
                    campaign.put("budget", rs.getDouble("budget"));
                    campaigns.add(campaign);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting all campaigns: " + e.getMessage());
        }
        return campaigns;
    }

    public List<String> getCampaignHostnames(String campaignName) {
        List<String> hostnames = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String sql = "SELECT hostname FROM campaigns WHERE name = ? AND hostname IS NOT NULL";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, campaignName);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        hostnames.add(rs.getString("hostname"));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting campaign hostnames: " + e.getMessage());
        }
        return hostnames;
    }

    public List<String> getPlatforms() {
        List<String> platforms = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String sql = "SELECT DISTINCT platform FROM platform_stats";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    platforms.add(rs.getString("platform"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting platforms: " + e.getMessage());
        }
        return platforms;
    }

    public Map<String, Long> getCampaignJoinStats(String name, String timeFilter) {
        Map<String, Long> stats = new HashMap<>();
        try (Connection connection = getConnection()) {
            String sql = "SELECT client_type, COUNT(*) as count FROM joins j " +
                        "INNER JOIN campaigns c ON j.hostname = c.hostname " +
                        "WHERE c.name = ? " +
                        (timeFilter != null ? "AND j.join_time >= DATE_SUB(NOW(), INTERVAL ? DAY) " : "") +
                        "GROUP BY client_type";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, name);
                if (timeFilter != null) {
                    stmt.setString(2, timeFilter.replace("d", ""));
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    long total = 0;
                    while (rs.next()) {
                        String clientType = rs.getString("client_type").toLowerCase();
                        long count = rs.getLong("count");
                        stats.put(clientType, count);
                        total += count;
                    }
                    stats.put("total", total);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting campaign join stats: " + e.getMessage());
        }
        return stats;
    }

    public void close() {
        // Clean up any resources if needed
    }
} 