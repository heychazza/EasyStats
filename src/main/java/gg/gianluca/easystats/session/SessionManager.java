package gg.gianluca.easystats.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<UUID, Long> sessionStartTimes;
    private final Map<String, SessionStats> hostnameStats;

    public SessionManager() {
        this.sessionStartTimes = new ConcurrentHashMap<>();
        this.hostnameStats = new ConcurrentHashMap<>();
    }

    public void startSession(UUID playerId, String hostname) {
        sessionStartTimes.put(playerId, System.currentTimeMillis());
    }

    public void endSession(UUID playerId, String hostname) {
        Long startTime = sessionStartTimes.remove(playerId);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            hostnameStats.computeIfAbsent(hostname, k -> new SessionStats())
                    .addSession(duration);
        }
    }

    public double getAverageSessionTime(String hostname) {
        SessionStats stats = hostnameStats.get(hostname);
        return stats != null ? stats.getAverageSessionTime() : 0.0;
    }

    public Map<String, Double> compareSessionTimes(String hostname1, String hostname2) {
        double avg1 = getAverageSessionTime(hostname1);
        double avg2 = getAverageSessionTime(hostname2);
        double diff = avg1 - avg2;
        double percentDiff = avg2 > 0 ? (diff / avg2) * 100 : 0;

        return Map.of(
                "hostname1_avg", avg1,
                "hostname2_avg", avg2,
                "difference", diff,
                "percent_difference", percentDiff
        );
    }

    private static class SessionStats {
        private long totalSessions;
        private long totalDuration;

        public void addSession(long duration) {
            totalSessions++;
            totalDuration += duration;
        }

        public double getAverageSessionTime() {
            return totalSessions > 0 ? (double) totalDuration / totalSessions : 0.0;
        }
    }
} 