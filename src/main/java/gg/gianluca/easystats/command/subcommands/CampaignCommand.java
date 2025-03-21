package gg.gianluca.easystats.command.subcommands;

import gg.gianluca.easystats.EasyStats;
import gg.gianluca.easystats.command.base.BaseCommand;
import gg.gianluca.easystats.data.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class CampaignCommand extends BaseCommand {
    private final DataManager dataManager;

    public CampaignCommand(EasyStats plugin) {
        super(plugin, "campaign", "/easystats campaign <create|info|end|addhost|removehost> <name> [args...]", "Manage campaigns");
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        String campaignName = args[1];

        switch (subcommand) {
            case "create":
                if (args.length < 6) {
                    sender.sendMessage(ChatColor.RED + "Usage: /easystats campaign create <name> <description> <start_date> <end_date> <currency> <cost>");
                    return true;
                }
                handleCreate(sender, campaignName, args[2], args[3], args[4], args[5], Double.parseDouble(args[6]));
                break;
            case "view":
                handleView(sender, args);
                break;
            case "list":
                handleList(sender);
                break;
            case "end":
                handleEnd(sender, campaignName);
                break;
            case "addhostname":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /easystats campaign addhostname <name> <hostname>");
                    return true;
                }
                handleAddHostname(sender, campaignName, args[2]);
                break;
            case "removehostname":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /easystats campaign removehostname <name> <hostname>");
                    return true;
                }
                handleRemoveHostname(sender, campaignName, args[2]);
                break;
            case "listhostnames":
                handleListHostnames(sender, campaignName);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            case "addhost":
                handleAddHost(sender, args);
                break;
            case "removehost":
                handleRemoveHost(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String name, String description, String startDate, String endDate, String currency, double cost) {
        try {
            dataManager.createCampaign(name, description, startDate, endDate, currency, cost);
            sender.sendMessage(ChatColor.GREEN + "Campaign '" + name + "' created successfully!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to create campaign: " + e.getMessage());
        }
    }

    private void handleView(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /easystats campaign view <name> [timeFilter]");
            return;
        }

        String name = args[2];
        String timeFilter = args.length > 3 ? args[3] : null;
        Map<String, Object> campaign = dataManager.getCampaign(name);
        
        if (campaign == null) {
            sender.sendMessage(ChatColor.RED + "Campaign not found: " + name);
            return;
        }

        // Get campaign metrics
        Map<String, Object> metrics = dataManager.getCampaignMetrics(name);
        double cost = ((Number) metrics.getOrDefault("cost", 0.0)).doubleValue();
        double revenue = ((Number) metrics.getOrDefault("revenue", 0.0)).doubleValue();
        double profit = revenue - cost;
        double roi = cost > 0 ? (profit / cost) * 100 : 0;

        // Get campaign join stats
        Map<String, Long> joinStats = dataManager.getCampaignJoinStats(name, timeFilter);
        long totalJoins = joinStats.getOrDefault("total", 0L);
        long javaJoins = joinStats.getOrDefault("java", 0L);
        long bedrockJoins = joinStats.getOrDefault("bedrock", 0L);

        // Get campaign hostnames
        List<String> hostnames = dataManager.getCampaignHostnames(name);

        // Format dates
        String startDate = campaign.get("start_date").toString();
        String endDate = campaign.get("end_date").toString();
        String status = campaign.get("status").toString();
        String currency = campaign.get("currency").toString();

        // Build message
        StringBuilder message = new StringBuilder();
        message.append(ChatColor.GOLD).append("=== Campaign: ").append(name).append(" ===\n");
        message.append(ChatColor.YELLOW).append("Description: ").append(ChatColor.WHITE).append(campaign.get("description")).append("\n");
        message.append(ChatColor.YELLOW).append("Status: ").append(ChatColor.WHITE).append(status).append("\n");
        message.append(ChatColor.YELLOW).append("Period: ").append(ChatColor.WHITE).append(startDate).append(" to ").append(endDate).append("\n");
        message.append(ChatColor.YELLOW).append("Hostnames: ").append(ChatColor.WHITE).append(String.join(", ", hostnames)).append("\n");
        message.append(ChatColor.YELLOW).append("Cost: ").append(ChatColor.WHITE).append(String.format("%.2f %s", cost, currency)).append("\n");
        message.append(ChatColor.YELLOW).append("Revenue: ").append(ChatColor.WHITE).append(String.format("%.2f %s", revenue, currency)).append("\n");
        message.append(ChatColor.YELLOW).append("Profit: ").append(ChatColor.WHITE).append(String.format("%.2f %s", profit, currency)).append("\n");
        message.append(ChatColor.YELLOW).append("ROI: ").append(ChatColor.WHITE).append(String.format("%.2f%%", roi)).append("\n");
        message.append(ChatColor.YELLOW).append("Total Joins: ").append(ChatColor.WHITE).append(totalJoins).append("\n");
        message.append(ChatColor.YELLOW).append("Java Joins: ").append(ChatColor.WHITE).append(javaJoins).append("\n");
        message.append(ChatColor.YELLOW).append("Bedrock Joins: ").append(ChatColor.WHITE).append(bedrockJoins);

        sender.sendMessage(message.toString());
    }

    private void handleList(CommandSender sender) {
        List<Map<String, Object>> campaigns = dataManager.getAllCampaigns();
        if (campaigns.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No campaigns found.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Active Campaigns ===");
        for (Map<String, Object> campaign : campaigns) {
            double cost = (Double) campaign.get("cost");
            double revenue = (Double) campaign.get("total_revenue");
            double profit = revenue - cost;
            double roi = cost > 0 ? (profit / cost) * 100 : 0;

            sender.sendMessage(ChatColor.YELLOW + "- " + campaign.get("name") + 
                    " (" + campaign.get("currency") + ": " + 
                    String.format("%.2f", revenue) + ", ROI: " + 
                    String.format("%.2f%%", roi) + ")");
        }
    }

    private void handleEnd(CommandSender sender, String campaignName) {
        try {
            dataManager.endCampaign(campaignName);
            sender.sendMessage(ChatColor.GREEN + "Campaign '" + campaignName + "' ended successfully!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to end campaign: " + e.getMessage());
        }
    }

    private void handleAddHostname(CommandSender sender, String campaignName, String hostname) {
        try {
            if (dataManager.addHostnameToCampaign(campaignName, hostname)) {
                sender.sendMessage(ChatColor.GREEN + "Added hostname '" + hostname + "' to campaign '" + campaignName + "'");
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to add hostname. Campaign may not exist or hostname is already added.");
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to add hostname: " + e.getMessage());
        }
    }

    private void handleRemoveHostname(CommandSender sender, String campaignName, String hostname) {
        try {
            if (dataManager.removeHostnameFromCampaign(campaignName, hostname)) {
                sender.sendMessage(ChatColor.GREEN + "Removed hostname '" + hostname + "' from campaign '" + campaignName + "'");
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to remove hostname. Campaign or hostname may not exist.");
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to remove hostname: " + e.getMessage());
        }
    }

    private void handleListHostnames(CommandSender sender, String campaignName) {
        List<String> hostnames = dataManager.getCampaignHostnames(campaignName);
        if (hostnames.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No hostnames found for campaign '" + campaignName + "'");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Campaign Hostnames ===");
        for (String hostname : hostnames) {
            sender.sendMessage(ChatColor.YELLOW + "- " + hostname);
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /easystats campaign info <name>");
            return;
        }

        String name = args[1];
        Map<String, Object> campaign = dataManager.getCampaign(name);
        if (campaign == null) {
            sender.sendMessage(ChatColor.RED + "Campaign not found.");
            return;
        }

        Map<String, Object> metrics = dataManager.getCampaignMetrics(name);
        sender.sendMessage(ChatColor.GREEN + "Campaign Information:");
        sender.sendMessage(ChatColor.YELLOW + "Name: " + campaign.get("name"));
        sender.sendMessage(ChatColor.YELLOW + "Description: " + campaign.get("description"));
        sender.sendMessage(ChatColor.YELLOW + "Start Date: " + campaign.get("start_date"));
        sender.sendMessage(ChatColor.YELLOW + "End Date: " + (campaign.get("end_date") != null ? campaign.get("end_date") : "Ongoing"));
        sender.sendMessage(ChatColor.YELLOW + "Hostname: " + campaign.get("hostname"));
        sender.sendMessage(ChatColor.YELLOW + "Budget: $" + campaign.get("budget"));
        sender.sendMessage(ChatColor.YELLOW + "Total Joins: " + metrics.get("total_joins"));
        sender.sendMessage(ChatColor.YELLOW + "Unique Players: " + metrics.get("unique_players"));
    }

    private void handleAddHost(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /easystats campaign addhost <name> <hostname>");
            return;
        }

        String name = args[1];
        String hostname = args[2];
        dataManager.addHostnameToCampaign(name, hostname);
        sender.sendMessage(ChatColor.GREEN + "Successfully added hostname to campaign.");
    }

    private void handleRemoveHost(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /easystats campaign removehost <name> <hostname>");
            return;
        }

        String name = args[1];
        String hostname = args[2];
        dataManager.removeHostnameFromCampaign(name, hostname);
        sender.sendMessage(ChatColor.GREEN + "Successfully removed hostname from campaign.");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Campaign Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign create <name> <description> <start_date> <end_date> <currency> <cost> - Create a campaign");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign view <name> [timeFilter] - View campaign details");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign list - List all campaigns");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign end <name> - End a campaign");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign addhostname <name> <hostname> - Add hostname to campaign");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign removehostname <name> <hostname> - Remove hostname from campaign");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign listhostnames <name> - List campaign hostnames");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign info <name> - View campaign information");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign addhost <name> <hostname> - Add hostname to campaign");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign removehost <name> <hostname> - Remove hostname from campaign");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "view", "list", "end", "addhostname", "removehostname", "listhostnames", "info", "addhost", "removehost");
        }
        return null;
    }
} 