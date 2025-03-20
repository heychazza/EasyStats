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
public class RevenueCommand extends BaseCommand {
    private final DataManager dataManager;

    public RevenueCommand(EasyStats plugin) {
        super(plugin, "easystats.revenue");
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        String platform = args[1];

        switch (subcommand) {
            case "view":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /easystats revenue view <platform> [-t <time>]");
                    return true;
                }
                String timeFilter = args.length > 3 && args[2].equals("-t") ? args[3] : null;
                handleView(sender, platform, timeFilter);
                break;
            case "compare":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /easystats revenue compare <platform1> <platform2>");
                    return true;
                }
                handleCompare(sender, platform, args[2]);
                break;
            case "add":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /easystats revenue add <platform> <amount> <currency>");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    String currency = args[3];
                    handleAdd(sender, platform, amount, currency);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount format!");
                }
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void handleView(CommandSender sender, String platform, String timeFilter) {
        Map<String, Double> stats = dataManager.getRevenueStats(platform, timeFilter);
        
        sender.sendMessage(ChatColor.GOLD + "Revenue Statistics for " + platform + ":");
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            String currency = entry.getKey();
            double amount = entry.getValue();
            sender.sendMessage(ChatColor.YELLOW + currency + ": " + String.format("%.2f", amount));
        }
    }

    private void handleCompare(CommandSender sender, String platform1, String platform2) {
        Map<String, Double> stats1 = dataManager.getRevenueStats(platform1, null);
        Map<String, Double> stats2 = dataManager.getRevenueStats(platform2, null);

        sender.sendMessage(ChatColor.GOLD + "Revenue Comparison:");
        for (String currency : stats1.keySet()) {
            double amount1 = stats1.getOrDefault(currency, 0.0);
            double amount2 = stats2.getOrDefault(currency, 0.0);
            double total = amount1 + amount2;
            double diff = amount1 - amount2;
            double diffPercent = amount2 > 0 ? (diff / amount2) * 100 : 0;

            sender.sendMessage(ChatColor.YELLOW + currency + ":");
            sender.sendMessage(ChatColor.YELLOW + "  Total: " + String.format("%.2f", total) + 
                    " (" + platform1 + ": " + String.format("%.2f", amount1) + 
                    " & " + platform2 + ": " + String.format("%.2f", amount2) + ")");
            
            if (amount2 > 0) {
                String message = "  " + platform1 + " is " + (diff >= 0 ? "outperforming" : "lacking to") + " " + platform2 + 
                        " by " + String.format("%.2f", Math.abs(diff)) + " " + currency + 
                        " (" + String.format("%.1f", Math.abs(diffPercent)) + "%)";
                sender.sendMessage(ChatColor.YELLOW + message);
            }
        }
    }

    private void handleAdd(CommandSender sender, String platform, double amount, String currency) {
        dataManager.addRevenue(platform, amount, currency);
        sender.sendMessage(ChatColor.GREEN + "Added " + String.format("%.2f", amount) + " " + currency + 
                " to " + platform + "'s revenue");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Revenue Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/easystats revenue view <platform> [-t <time>] - View revenue statistics");
        sender.sendMessage(ChatColor.YELLOW + "/easystats revenue compare <platform1> <platform2> - Compare revenue");
        sender.sendMessage(ChatColor.YELLOW + "/easystats revenue add <platform> <amount> <currency> - Add revenue");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("view", "compare", "add");
        }
        return null;
    }
} 