package gg.gianluca.easystats.data;

import gg.gianluca.easystats.EasyStats;
import gg.gianluca.easystats.database.DatabaseFactory;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class DataManager {
    private final EasyStats plugin;
    @SuppressWarnings("unused")
    private final FileConfiguration config;
    private final DatabaseFactory databaseFactory;

    public DataManager(EasyStats plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.databaseFactory = new DatabaseFactory(plugin);
        initializeTables();
    }

    private void initializeTables() {
        try (Connection conn = databaseFactory.getConnection()) {
            // Create platform stats table
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS platform_stats (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    platform VARCHAR(255) NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    client_type VARCHAR(10) NOT NULL,
                    country VARCHAR(255),
                    country_tier VARCHAR(10),
                    INDEX idx_platform (platform),
                    INDEX idx_join_time (join_time),
                    INDEX idx_player_uuid (player_uuid)
                )
            """)) {
                stmt.execute();
            }

            // Create revenue table
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS revenue (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    platform VARCHAR(255) NOT NULL,
                    amount DECIMAL(10,2) NOT NULL,
                    currency VARCHAR(10) NOT NULL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_platform_time (platform, timestamp),
                    INDEX idx_currency (currency)
                )
            """)) {
                stmt.execute();
            }

            // Create campaigns table
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS campaigns (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(255) NOT NULL UNIQUE,
                    description TEXT,
                    start_date DATE NOT NULL,
                    end_date DATE NOT NULL,
                    currency VARCHAR(10) NOT NULL,
                    cost DECIMAL(10,2) NOT NULL,
                    total_revenue DECIMAL(10,2) DEFAULT 0.0,
                    status VARCHAR(20) DEFAULT 'active',
                    INDEX idx_name (name),
                    INDEX idx_dates (start_date, end_date),
                    INDEX idx_status (status)
                )
            """)) {
                stmt.execute();
            }

            // Create campaign hostnames table
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS campaign_hostnames (
                    campaign_id INTEGER NOT NULL,
                    hostname VARCHAR(255) NOT NULL,
                    FOREIGN KEY (campaign_id) REFERENCES campaigns(id),
                    PRIMARY KEY (campaign_id, hostname),
                    INDEX idx_hostname (hostname)
                )
            """)) {
                stmt.execute();
            }

            // Create session stats table
            try (PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS session_stats (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    player_uuid VARCHAR(36) NOT NULL,
                    hostname VARCHAR(255) NOT NULL,
                    start_time TIMESTAMP NOT NULL,
                    end_time TIMESTAMP,
                    duration BIGINT,
                    INDEX idx_player (player_uuid),
                    INDEX idx_hostname_time (hostname, start_time),
                    INDEX idx_end_time (end_time)
                )
            """)) {
                stmt.execute();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database tables", e);
        }
    }

    public void loadConfig() {
        // Reload any configuration-dependent settings
        // This method will be called when the config is reloaded
        // Currently empty as we don't have any config-dependent settings
    }

    public Map<String, Long> getPlatformStats(String platform, String timeFilter) {
        Map<String, Long> stats = new HashMap<>();
        String query = timeFilter != null 
            ? "SELECT client_type, COUNT(*) as count FROM platform_stats WHERE platform = ? AND join_time >= DATE_SUB(NOW(), INTERVAL ? DAY)"
            : "SELECT client_type, COUNT(*) as count FROM platform_stats WHERE platform = ?";
        query += " GROUP BY client_type";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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
            plugin.getLogger().log(Level.SEVERE, "Failed to get platform stats", e);
        }
        return stats;
    }

    public Map<String, Map<String, Long>> getCountryStats(String platform, String timeFilter) {
        Map<String, Map<String, Long>> stats = new HashMap<>();
        String query = timeFilter != null 
            ? """
                SELECT country_tier, client_type, COUNT(*) as count 
                FROM platform_stats 
                WHERE platform = ? 
                AND country_tier IS NOT NULL 
                AND join_time >= DATE_SUB(NOW(), INTERVAL ? DAY)
              """
            : """
                SELECT country_tier, client_type, COUNT(*) as count 
                FROM platform_stats 
                WHERE platform = ? 
                AND country_tier IS NOT NULL
              """;
        query += " GROUP BY country_tier, client_type";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, platform);
            if (timeFilter != null) {
                stmt.setString(2, timeFilter.replace("d", ""));
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tier = rs.getString("country_tier");
                String clientType = rs.getString("client_type");
                long count = rs.getLong("count");

                stats.computeIfAbsent(tier, k -> new HashMap<>())
                     .put(clientType.toLowerCase(), count);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get country stats", e);
        }
        return stats;
    }

    public Map<String, Double> getRevenueStats(String platform, String timeFilter) {
        Map<String, Double> stats = new HashMap<>();
        String query = timeFilter != null 
            ? "SELECT currency, SUM(amount) as total FROM revenue WHERE platform = ? AND timestamp >= DATE_SUB(NOW(), INTERVAL ? DAY) GROUP BY currency"
            : "SELECT currency, SUM(amount) as total FROM revenue WHERE platform = ? GROUP BY currency";

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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
            plugin.getLogger().log(Level.SEVERE, "Failed to get revenue stats", e);
        }
        return stats;
    }

    public void addRevenue(String platform, double amount, String currency) {
        try (Connection conn = databaseFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert revenue record
                String insertQuery = "INSERT INTO revenue (platform, amount, currency) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setString(1, platform);
                    stmt.setDouble(2, amount);
                    stmt.setString(3, currency);
                    stmt.executeUpdate();
                }

                // Update campaign revenue if the platform matches any campaign hostname
                updateCampaignRevenue(platform, amount);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to add revenue", e);
        }
    }

    public void createCampaign(String name, String description, String startDate, String endDate, String currency, double cost) {
        String query = """
            INSERT INTO campaigns (name, description, start_date, end_date, currency, cost) 
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, startDate);
            stmt.setString(4, endDate);
            stmt.setString(5, currency);
            stmt.setDouble(6, cost);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create campaign", e);
        }
    }

    public Map<String, Object> getCampaign(String name) {
        String query = "SELECT * FROM campaigns WHERE name = ?";
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> campaign = new HashMap<>();
                campaign.put("name", rs.getString("name"));
                campaign.put("description", rs.getString("description"));
                campaign.put("start_date", rs.getString("start_date"));
                campaign.put("end_date", rs.getString("end_date"));
                campaign.put("currency", rs.getString("currency"));
                campaign.put("cost", rs.getDouble("cost"));
                campaign.put("total_revenue", rs.getDouble("total_revenue"));
                campaign.put("status", rs.getString("status"));
                return campaign;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get campaign", e);
        }
        return null;
    }

    public List<Map<String, Object>> getAllCampaigns() {
        List<Map<String, Object>> campaigns = new ArrayList<>();
        String query = "SELECT * FROM campaigns ORDER BY start_date DESC";
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> campaign = new HashMap<>();
                campaign.put("name", rs.getString("name"));
                campaign.put("description", rs.getString("description"));
                campaign.put("start_date", rs.getString("start_date"));
                campaign.put("end_date", rs.getString("end_date"));
                campaign.put("currency", rs.getString("currency"));
                campaign.put("cost", rs.getDouble("cost"));
                campaign.put("total_revenue", rs.getDouble("total_revenue"));
                campaign.put("status", rs.getString("status"));
                campaigns.add(campaign);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get all campaigns", e);
        }
        return campaigns;
    }

    public void endCampaign(String name) {
        String query = "UPDATE campaigns SET status = 'ended' WHERE name = ?";
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to end campaign", e);
        }
    }

    public void purgeOldData(int days) {
        try (Connection conn = databaseFactory.getConnection()) {
            // Define the queries with their respective time columns
            Map<String, String> tableQueries = Map.of(
                "platform_stats", "DELETE FROM platform_stats WHERE join_time < DATE_SUB(NOW(), INTERVAL ? DAY)",
                "revenue", "DELETE FROM revenue WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY)",
                "session_stats", "DELETE FROM session_stats WHERE start_time < DATE_SUB(NOW(), INTERVAL ? DAY)"
            );

            for (Map.Entry<String, String> entry : tableQueries.entrySet()) {
                try (PreparedStatement stmt = conn.prepareStatement(entry.getValue())) {
                    stmt.setInt(1, days);
                    int deleted = stmt.executeUpdate();
                    plugin.getLogger().info("Purged " + deleted + " records from " + entry.getKey());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to purge old data", e);
        }
    }

    public void recordJoin(UUID playerId, String hostname, String clientType) {
        String query = """
            INSERT INTO platform_stats (platform, player_uuid, client_type) 
            VALUES (?, ?, ?)
        """;
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hostname);
            stmt.setString(2, playerId.toString());
            stmt.setString(3, clientType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to record player join", e);
        }
    }

    public boolean addHostnameToCampaign(String campaignName, String hostname) {
        String query = """
            INSERT INTO campaign_hostnames (campaign_id, hostname)
            SELECT id, ? FROM campaigns WHERE name = ?
        """;
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hostname);
            stmt.setString(2, campaignName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to add hostname to campaign", e);
            return false;
        }
    }

    public boolean removeHostnameFromCampaign(String campaignName, String hostname) {
        String query = """
            DELETE FROM campaign_hostnames 
            WHERE campaign_id = (SELECT id FROM campaigns WHERE name = ?)
            AND hostname = ?
        """;
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, campaignName);
            stmt.setString(2, hostname);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to remove hostname from campaign", e);
            return false;
        }
    }

    public List<String> getCampaignHostnames(String campaignName) {
        List<String> hostnames = new ArrayList<>();
        String query = """
            SELECT hostname FROM campaign_hostnames ch
            JOIN campaigns c ON ch.campaign_id = c.id
            WHERE c.name = ?
        """;
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, campaignName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                hostnames.add(rs.getString("hostname"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get campaign hostnames", e);
        }
        return hostnames;
    }

    public void close() {
        databaseFactory.close();
    }

    public void startSession(UUID playerId, String hostname) {
        String query = "INSERT INTO session_stats (player_uuid, hostname, start_time) VALUES (?, ?, NOW())";
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerId.toString());
            stmt.setString(2, hostname);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start session", e);
        }
    }

    public void endSession(UUID playerId) {
        String query = """
            UPDATE session_stats 
            SET end_time = NOW(),
                duration = TIMESTAMPDIFF(SECOND, start_time, NOW())
            WHERE player_uuid = ? 
            AND end_time IS NULL
        """;
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to end session", e);
        }
    }

    public Map<String, Long> getSessionStats(String hostname, String timeFilter) {
        Map<String, Long> stats = new HashMap<>();
        String query = timeFilter != null
            ? """
                SELECT 
                    COUNT(DISTINCT player_uuid) as unique_players,
                    COUNT(*) as total_sessions,
                    COALESCE(AVG(duration), 0) as avg_duration,
                    COALESCE(SUM(duration), 0) as total_duration
                FROM session_stats 
                WHERE hostname = ? 
                AND start_time >= DATE_SUB(NOW(), INTERVAL ? DAY)
              """
            : """
                SELECT 
                    COUNT(DISTINCT player_uuid) as unique_players,
                    COUNT(*) as total_sessions,
                    COALESCE(AVG(duration), 0) as avg_duration,
                    COALESCE(SUM(duration), 0) as total_duration
                FROM session_stats 
                WHERE hostname = ?
              """;

        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hostname);
            if (timeFilter != null) {
                stmt.setString(2, timeFilter.replace("d", ""));
            }
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stats.put("unique_players", rs.getLong("unique_players"));
                stats.put("total_sessions", rs.getLong("total_sessions"));
                stats.put("avg_duration", rs.getLong("avg_duration"));
                stats.put("total_duration", rs.getLong("total_duration"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get session stats", e);
        }
        return stats;
    }

    public void updateCampaignRevenue(String hostname, double amount) {
        String query = """
            UPDATE campaigns c
            SET total_revenue = total_revenue + ?
            WHERE EXISTS (
                SELECT 1 FROM campaign_hostnames ch
                WHERE ch.campaign_id = c.id
                AND ch.hostname = ?
                AND c.status = 'active'
            )
        """;
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, amount);
            stmt.setString(2, hostname);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update campaign revenue", e);
        }
    }

    public Map<String, Double> getCampaignMetrics(String name) {
        Map<String, Double> metrics = new HashMap<>();
        String query = """
            SELECT 
                cost,
                total_revenue,
                (total_revenue - cost) as profit,
                CASE 
                    WHEN cost > 0 THEN ((total_revenue - cost) / cost) * 100 
                    ELSE 0 
                END as roi
            FROM campaigns 
            WHERE name = ?
        """;
        try (Connection conn = databaseFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                metrics.put("cost", rs.getDouble("cost"));
                metrics.put("revenue", rs.getDouble("total_revenue"));
                metrics.put("profit", rs.getDouble("profit"));
                metrics.put("roi", rs.getDouble("roi"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get campaign metrics", e);
        }
        return metrics;
    }
} 