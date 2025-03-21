package gg.gianluca.easystats.database;

import gg.gianluca.easystats.EasyStats;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseFactory {
    private final EasyStats plugin;
    private final String databasePath;

    public DatabaseFactory(EasyStats plugin) {
        this.plugin = plugin;
        this.databasePath = new File(plugin.getDataFolder(), "database.db").getAbsolutePath();
        createDataFolder();
    }

    private void createDataFolder() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }

    public void close() {
        // Nothing to close at factory level since connections are closed individually
    }
} 