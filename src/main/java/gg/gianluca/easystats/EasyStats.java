package gg.gianluca.easystats;

import gg.gianluca.easystats.api.EasyStatsAPI;
import gg.gianluca.easystats.api.EasyStatsAPIImpl;
import gg.gianluca.easystats.command.base.BaseCommand;
import gg.gianluca.easystats.command.subcommands.*;
import gg.gianluca.easystats.data.DataManager;
import gg.gianluca.easystats.database.DatabaseFactory;
import gg.gianluca.easystats.expansion.EasyStatsExpansion;
import gg.gianluca.easystats.listener.PlayerListener;
import gg.gianluca.easystats.session.SessionManager;
import gg.gianluca.easystats.util.DependencyManager;
import gg.gianluca.easystats.util.GeoIPManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class EasyStats extends JavaPlugin {
    private DataManager dataManager;
    private SessionManager sessionManager;
    private EasyStatsAPI api;
    private DependencyManager dependencyManager;
    private GeoIPManager geoIPManager;
    private final Map<String, BaseCommand> subcommands = new HashMap<>();

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.dependencyManager = new DependencyManager(this);
        this.dataManager = new DataManager(this);
        this.sessionManager = new SessionManager();
        this.api = new EasyStatsAPIImpl(this);
        this.geoIPManager = new GeoIPManager(this);

        // Register commands
        getCommand("easystats").setExecutor(new EasyStatsCommand(this));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Register PlaceholderAPI expansion if available
        if (dependencyManager.isPlaceholderAPIEnabled()) {
            PlaceholderExpansion expansion = new EasyStatsExpansion(this);
            expansion.register();
            getLogger().info("PlaceholderAPI expansion registered successfully!");
        }

        // Register subcommands
        subcommands.put("reload", new ReloadCommand(this));
        subcommands.put("platform", new PlatformCommand(this));
        subcommands.put("countries", new CountriesCommand(this));
        subcommands.put("revenue", new RevenueCommand(this));
        subcommands.put("campaign", new CampaignCommand(this));
        subcommands.put("session", new SessionCommand(this));
        subcommands.put("export", new ExportCommand(this));
    }

    @Override
    public void onDisable() {
        if (geoIPManager != null) {
            geoIPManager.close();
        }
        if (dataManager != null) {
            dataManager.close();
        }
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public EasyStatsAPI getAPI() {
        return api;
    }

    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public GeoIPManager getGeoIPManager() {
        return geoIPManager;
    }
} 