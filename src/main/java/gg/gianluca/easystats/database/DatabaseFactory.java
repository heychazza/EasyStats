package gg.gianluca.easystats.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.gianluca.easystats.EasyStats;
import org.bukkit.configuration.ConfigurationSection;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseFactory {
    private final EasyStats plugin;
    private DataSource dataSource;

    public DatabaseFactory(EasyStats plugin) {
        this.plugin = plugin;
        setupDataSource();
    }

    private void setupDataSource() {
        String type = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        
        if (type.equals("mysql")) {
            setupMySQLDataSource();
        } else {
            setupSQLiteDataSource();
        }
    }

    private void setupMySQLDataSource() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("database.mysql");
        ConfigurationSection poolConfig = config.getConfigurationSection("pool");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s",
                config.getString("host"),
                config.getInt("port"),
                config.getString("database")));
        hikariConfig.setUsername(config.getString("username"));
        hikariConfig.setPassword(config.getString("password"));
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Pool settings
        hikariConfig.setMaximumPoolSize(poolConfig.getInt("maximum-pool-size", 10));
        hikariConfig.setMinimumIdle(poolConfig.getInt("minimum-idle", 5));
        hikariConfig.setIdleTimeout(poolConfig.getLong("idle-timeout", 300000));
        hikariConfig.setMaxLifetime(poolConfig.getLong("max-lifetime", 600000));
        hikariConfig.setConnectionTimeout(poolConfig.getLong("connection-timeout", 5000));

        // Additional properties
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize MySQL connection pool: " + e.getMessage());
            plugin.getLogger().warning("Falling back to SQLite...");
            setupSQLiteDataSource();
        }
    }

    private void setupSQLiteDataSource() {
        String fileName = plugin.getConfig().getString("database.sqlite.file", "database.db");
        File databaseFile = new File(plugin.getDataFolder(), fileName);

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        
        // SQLite-specific settings
        hikariConfig.setMaximumPoolSize(1); // SQLite only supports one connection at a time
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.addDataSourceProperty("foreign_keys", "true");

        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize SQLite database: " + e.getMessage());
            throw new RuntimeException("Could not initialize database connection", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }
} 