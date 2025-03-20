package gg.gianluca.easystats.listener;

import gg.gianluca.easystats.EasyStats;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.net.InetAddress;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final EasyStats plugin;

    public PlayerListener(EasyStats plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        UUID playerId = event.getUniqueId();
        InetAddress address = event.getAddress();
        String hostname = address.getHostName();
        String clientType = plugin.getDependencyManager().getClientType(playerId.toString());

        // Record the join in the database
        plugin.getDataManager().recordJoin(playerId, hostname, clientType);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String hostname = event.getPlayer().getAddress().getHostName();
        plugin.getSessionManager().startSession(event.getPlayer().getUniqueId(), hostname);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String hostname = event.getPlayer().getAddress().getHostName();
        plugin.getSessionManager().endSession(event.getPlayer().getUniqueId(), hostname);
    }
} 