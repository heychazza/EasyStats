package gg.gianluca.easystats.command.subcommands;

import gg.gianluca.easystats.EasyStats;
import gg.gianluca.easystats.command.base.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class ExportCommand extends BaseCommand {
    private final EasyStats plugin;

    public ExportCommand(EasyStats plugin) {
        super(plugin, "easystats.export");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /easystats export <name> <timeframe>");
            return true;
        }

        String fileName = args[0];
        String timeframe = args[1];
        if (!fileName.endsWith(".json")) {
            fileName += ".json";
        }

        File exportsDir = new File(plugin.getDataFolder(), "exports");
        if (!exportsDir.exists()) {
            exportsDir.mkdirs();
        }

        File exportFile = new File(exportsDir, fileName);
        if (exportFile.exists()) {
            sender.sendMessage(ChatColor.RED + "An export with that name already exists!");
            return true;
        }

        try (FileWriter writer = new FileWriter(exportFile)) {
            JSONObject export = new JSONObject();
            
            // Add export metadata
            export.put("export_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            export.put("timeframe", timeframe);
            export.put("timeframe_days", timeframe.replace("d", ""));

            // Add platform stats with timeframe
            JSONObject platformStats = new JSONObject();
            for (String hostname : plugin.getDataManager().getAllHostnames()) {
                Map<String, Long> stats = plugin.getDataManager().getPlatformStats(hostname, timeframe);
                JSONObject hostStats = new JSONObject();
                hostStats.put("total", stats.getOrDefault("total", 0L));
                hostStats.put("java", stats.getOrDefault("java", 0L));
                hostStats.put("bedrock", stats.getOrDefault("bedrock", 0L));
                platformStats.put(hostname, hostStats);
            }
            export.put("platform_stats", platformStats);

            // Add revenue stats with timeframe
            JSONObject revenueStats = new JSONObject();
            for (String hostname : plugin.getDataManager().getAllHostnames()) {
                Map<String, Double> stats = plugin.getDataManager().getRevenueStats(hostname, timeframe);
                JSONObject hostStats = new JSONObject();
                for (Map.Entry<String, Double> entry : stats.entrySet()) {
                    hostStats.put(entry.getKey(), entry.getValue());
                }
                revenueStats.put(hostname, hostStats);
            }
            export.put("revenue_stats", revenueStats);

            // Add session stats
            JSONObject sessionStats = new JSONObject();
            for (String hostname : plugin.getDataManager().getAllHostnames()) {
                double avgTime = plugin.getSessionManager().getAverageSessionTime(hostname);
                JSONObject hostStats = new JSONObject();
                hostStats.put("average_time_seconds", avgTime);
                hostStats.put("average_time_formatted", formatDuration((long) avgTime));
                sessionStats.put(hostname, hostStats);
            }
            export.put("session_stats", sessionStats);

            // Add campaigns with timeframe-based stats
            JSONArray campaigns = new JSONArray();
            for (Map<String, Object> campaign : plugin.getDataManager().getAllCampaigns()) {
                String campaignName = (String) campaign.get("name");
                if (campaignName != null) {
                    JSONObject campaignObj = new JSONObject();
                    campaignObj.put("name", campaign.get("name"));
                    campaignObj.put("description", campaign.get("description"));
                    campaignObj.put("start_date", campaign.get("start_date"));
                    campaignObj.put("end_date", campaign.get("end_date"));
                    campaignObj.put("currency", campaign.get("currency"));
                    campaignObj.put("cost", campaign.get("cost"));
                    campaignObj.put("total_revenue", campaign.get("total_revenue"));
                    campaignObj.put("profit", campaign.get("total_revenue"));
                    campaignObj.put("roi", campaign.get("roi"));
                    campaignObj.put("status", campaign.get("status"));
                    
                    // Add hostnames
                    List<String> hostnames = plugin.getDataManager().getCampaignHostnames(campaignName);
                    campaignObj.put("hostnames", hostnames);
                    
                    // Add join stats for the specified timeframe
                    Map<String, Long> joinStats = plugin.getDataManager().getCampaignJoinStats(campaignName, timeframe);
                    JSONObject joinStatsObj = new JSONObject();
                    joinStatsObj.put("total", joinStats.getOrDefault("total", 0L));
                    joinStatsObj.put("java", joinStats.getOrDefault("java", 0L));
                    joinStatsObj.put("bedrock", joinStats.getOrDefault("bedrock", 0L));
                    campaignObj.put("join_stats", joinStatsObj);
                    
                    campaigns.put(campaignObj);
                }
            }
            export.put("campaigns", campaigns);

            writer.write(export.toString(2));
            sender.sendMessage(ChatColor.GREEN + "Successfully exported data to " + fileName);
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Failed to export data: " + e.getMessage());
            plugin.getLogger().severe("Failed to export data: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("export_name");
        } else if (args.length == 2) {
            completions.add("7d");
            completions.add("30d");
            completions.add("90d");
            completions.add("180d");
            completions.add("365d");
        }
        return completions;
    }
} 