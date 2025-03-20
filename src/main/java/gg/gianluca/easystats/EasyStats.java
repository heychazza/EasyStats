package gg.gianluca.easystats;

import gg.gianluca.easystats.api.EasyStatsAPI;
import gg.gianluca.easystats.api.EasyStatsAPIImpl;
import gg.gianluca.easystats.command.subcommands.EasyStatsCommand;
import gg.gianluca.easystats.data.DataManager;
import gg.gianluca.easystats.expansion.EasyStatsExpansion;
import gg.gianluca.easystats.session.SessionManager;
import gg.gianluca.easystats.listener.PlayerListener;
import gg.gianluca.easystats.util.DependencyManager;
import gg.gianluca.easystats.util.GeoIPManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyStats extends JavaPlugin {
    private DataManager dataManager;
    private SessionManager sessionManager;
    private EasyStatsAPI api;
    private DependencyManager dependencyManager;
    private GeoIPManager geoIPManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize GeoIP
        geoIPManager = new GeoIPManager(this);
        geoIPManager.initialize();

        // Initialize managers
        this.dependencyManager = new DependencyManager(this);
        this.dataManager = new DataManager(this);
        this.sessionManager = new SessionManager();
        this.api = new EasyStatsAPIImpl(this);

        // Register commands
        getCommand("easystats").setExecutor(new EasyStatsCommand(this));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Register PlaceholderAPI expansion if available
        if (dependencyManager.isPlaceholderAPIEnabled()) {
            new EasyStatsExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered successfully!");
        }
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