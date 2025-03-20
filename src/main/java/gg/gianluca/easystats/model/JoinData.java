package gg.gianluca.easystats.model;

import java.time.Instant;
import java.util.UUID;

public class JoinData {
    private final UUID playerId;
    private final String playerName;
    private final String platform;
    private final boolean isBedrock;
    private final Instant joinTime;
    private final String hostname;
    private final String country;
    private final String countryTier;

    public JoinData(UUID playerId, String playerName, String platform, boolean isBedrock, String hostname, String country, String countryTier) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.platform = platform;
        this.isBedrock = isBedrock;
        this.joinTime = Instant.now();
        this.hostname = hostname;
        this.country = country;
        this.countryTier = countryTier;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPlatform() {
        return platform;
    }

    public boolean isBedrock() {
        return isBedrock;
    }

    public Instant getJoinTime() {
        return joinTime;
    }

    public String getHostname() {
        return hostname;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryTier() {
        return countryTier;
    }
} 