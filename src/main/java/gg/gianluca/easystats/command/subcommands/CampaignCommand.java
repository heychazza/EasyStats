package gg.gianluca.easystats.command.subcommands;

import gg.gianluca.easystats.EasyStats;
import gg.gianluca.easystats.command.base.BaseCommand;
import gg.gianluca.easystats.data.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class CampaignCommand extends BaseCommand {
    private final DataManager dataManager;

    public CampaignCommand(EasyStats plugin) {
        super(plugin, "easystats.campaign");
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
                handleView(sender, campaignName);
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

    private void handleView(CommandSender sender, String campaignName) {
        Map<String, Object> campaign = dataManager.getCampaign(campaignName);
        if (campaign == null) {
            sender.sendMessage(ChatColor.RED + "Campaign not found!");
            return;
        }

        double cost = (Double) campaign.get("cost");
        double revenue = (Double) campaign.get("total_revenue");
        double profit = revenue - cost;
        double roi = cost > 0 ? (profit / cost) * 100 : 0;

        sender.sendMessage(ChatColor.GOLD + "=== Campaign Details ===");
        sender.sendMessage(ChatColor.YELLOW + "Name: " + campaign.get("name"));
        sender.sendMessage(ChatColor.YELLOW + "Description: " + campaign.get("description"));
        sender.sendMessage(ChatColor.YELLOW + "Start Date: " + campaign.get("start_date"));
        sender.sendMessage(ChatColor.YELLOW + "End Date: " + campaign.get("end_date"));
        sender.sendMessage(ChatColor.YELLOW + "Currency: " + campaign.get("currency"));
        sender.sendMessage(ChatColor.YELLOW + "Cost: " + String.format("%.2f", cost));
        sender.sendMessage(ChatColor.YELLOW + "Total Revenue: " + String.format("%.2f", revenue));
        sender.sendMessage(ChatColor.YELLOW + "Profit: " + String.format("%.2f", profit));
        sender.sendMessage(ChatColor.YELLOW + "ROI: " + String.format("%.2f%%", roi));
        sender.sendMessage(ChatColor.YELLOW + "Status: " + campaign.get("status"));

        // Display hostnames
        List<String> hostnames = dataManager.getCampaignHostnames(campaignName);
        if (!hostnames.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Hostnames:");
            for (String hostname : hostnames) {
                sender.sendMessage(ChatColor.YELLOW + "  - " + hostname);
            }
        }
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

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Campaign Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign create <name> <description> <start_date> <end_date> <currency> <cost> - Create a campaign");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign view <name> - View campaign details");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign list - List all campaigns");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign end <name> - End a campaign");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign addhostname <name> <hostname> - Add hostname to campaign");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign removehostname <name> <hostname> - Remove hostname from campaign");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign listhostnames <name> - List campaign hostnames");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "view", "list", "end", "addhostname", "removehostname", "listhostnames");
        }
        return null;
    }
} 