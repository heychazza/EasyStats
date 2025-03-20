package gg.gianluca.easystats.database;

import gg.gianluca.easystats.EasyStats;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private final EasyStats plugin;
    private final String dbPath;

    public Database(EasyStats plugin) {
        this.plugin = plugin;
        this.dbPath = new File(plugin.getDataFolder(), "easystats.db").getAbsolutePath();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Create joins table
            String createJoinsTable = "CREATE TABLE IF NOT EXISTS joins (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL," +
                    "player_name TEXT NOT NULL," +
                    "platform TEXT NOT NULL," +
                    "hostname TEXT," +
                    "country TEXT," +
                    "country_tier TEXT," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            
            // Create revenue table
            String createRevenueTable = "CREATE TABLE IF NOT EXISTS revenue (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL," +
                    "player_name TEXT NOT NULL," +
                    "amount DECIMAL(10,2) NOT NULL," +
                    "currency TEXT NOT NULL," +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            
            // Create campaigns table
            String createCampaignsTable = "CREATE TABLE IF NOT EXISTS campaigns (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "description TEXT," +
                    "start_date DATETIME NOT NULL," +
                    "end_date DATETIME NOT NULL," +
                    "total_revenue DECIMAL(10,2) DEFAULT 0," +
                    "currency TEXT NOT NULL" +
                    ")";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createJoinsTable);
                stmt.execute(createRevenueTable);
                stmt.execute(createCampaignsTable);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
} 