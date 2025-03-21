package gg.gianluca.easystats.command.subcommands;

import gg.gianluca.easystats.EasyStats;
import gg.gianluca.easystats.command.base.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.file.Files;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExportCommand extends BaseCommand {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ExportCommand(EasyStats plugin) {
        super(plugin, 
              "easystats.export",
              "/easystats export <filename>",
              "Export statistics to a JSON file");
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }

        String hostname = args[1];
        Map<String, Object> stats = new HashMap<>();

        // Get platform stats
        stats.put("platform_stats", plugin.getDataManager().getPlatformStats(hostname, null));

        // Get country stats
        Map<String, Map<String, Map<String, Long>>> countryStats = plugin.getDataManager().getCountryStats(hostname, null);
        Map<String, Object> formattedCountryStats = new HashMap<>();
        for (Map.Entry<String, Map<String, Map<String, Long>>> tierEntry : countryStats.entrySet()) {
            String tier = tierEntry.getKey();
            Map<String, Object> tierStats = new HashMap<>();
            for (Map.Entry<String, Map<String, Long>> countryEntry : tierEntry.getValue().entrySet()) {
                String country = countryEntry.getKey();
                Map<String, Long> clientStats = countryEntry.getValue();
                long total = clientStats.values().stream().mapToLong(Long::longValue).sum();
                Map<String, Object> countryData = new HashMap<>();
                countryData.put("total", total);
                countryData.put("java", clientStats.getOrDefault("java", 0L));
                countryData.put("bedrock", clientStats.getOrDefault("bedrock", 0L));
                tierStats.put(country, countryData);
            }
            formattedCountryStats.put(tier, tierStats);
        }
        stats.put("country_stats", formattedCountryStats);

        // Get revenue stats
        stats.put("revenue_stats", plugin.getDataManager().getRevenueStats(hostname, null));

        // Get player count stats
        stats.put("player_count_stats", plugin.getDataManager().getPlayerCountStats(hostname));

        // Convert to JSON
        try {
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(stats);
            String fileName = "stats_" + hostname + "_" + System.currentTimeMillis() + ".json";
            File file = new File(plugin.getDataFolder(), fileName);
            Files.write(file.toPath(), json.getBytes());
            sender.sendMessage(ChatColor.GREEN + "Statistics exported to " + fileName);
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Failed to export statistics: " + e.getMessage());
            plugin.getLogger().severe("Failed to export statistics: " + e.getMessage());
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            return new ArrayList<>();
        }

        if (args.length == 2) {
            return Arrays.asList("export.json", "stats.json");
        }

        return new ArrayList<>();
    }

    private String toJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            json.append("  \"").append(entry.getKey()).append("\": ");
            
            Object value = entry.getValue();
            if (value instanceof Map) {
                json.append(toJson((Map<String, Object>) value));
            } else if (value instanceof List) {
                json.append(toJsonArray((List<?>) value));
            } else if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof Boolean) {
                json.append(value);
            } else if (value == null) {
                json.append("null");
            }
            
            if (it.hasNext()) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("}");
        return json.toString();
    }

    private String toJsonArray(List<?> list) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        
        Iterator<?> it = list.iterator();
        while (it.hasNext()) {
            Object value = it.next();
            if (value instanceof Map) {
                json.append(toJson((Map<String, Object>) value));
            } else if (value instanceof List) {
                json.append(toJsonArray((List<?>) value));
            } else if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof Boolean) {
                json.append(value);
            } else if (value == null) {
                json.append("null");
            }
            
            if (it.hasNext()) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("]");
        return json.toString();
    }
} 