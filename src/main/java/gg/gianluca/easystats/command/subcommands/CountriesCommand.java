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
public class CountriesCommand extends BaseCommand {
    private final DataManager dataManager;

    public CountriesCommand(EasyStats plugin) {
        super(plugin, "easystats.countries");
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
                    sender.sendMessage(ChatColor.RED + "Usage: /easystats countries view <platform> [-t <time>]");
                    return true;
                }
                String timeFilter = args.length > 3 && args[2].equals("-t") ? args[3] : null;
                handleView(sender, platform, timeFilter);
                break;
            case "compare":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /easystats countries compare <platform1> <platform2>");
                    return true;
                }
                handleCompare(sender, platform, args[2]);
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void handleView(CommandSender sender, String platform, String timeFilter) {
        Map<String, Map<String, Long>> stats = dataManager.getCountryStats(platform, timeFilter);
        
        sender.sendMessage(ChatColor.GOLD + "Country Statistics for " + platform + ":");
        for (Map.Entry<String, Map<String, Long>> entry : stats.entrySet()) {
            String tier = entry.getKey();
            Map<String, Long> tierStats = entry.getValue();
            long total = tierStats.getOrDefault("total", 0L);
            long java = tierStats.getOrDefault("java", 0L);
            long bedrock = tierStats.getOrDefault("bedrock", 0L);

            sender.sendMessage(ChatColor.YELLOW + tier + " Tier:");
            sender.sendMessage(ChatColor.YELLOW + "  Total: " + total + " (Java: " + java + " & Bedrock: " + bedrock + ")");
            if (total > 0) {
                sender.sendMessage(ChatColor.YELLOW + "  Java: " + java + " (" + String.format("%.2f", (java * 100.0 / total)) + "%)");
                sender.sendMessage(ChatColor.YELLOW + "  Bedrock: " + bedrock + " (" + String.format("%.2f", (bedrock * 100.0 / total)) + "%)");
            }
        }
    }

    private void handleCompare(CommandSender sender, String platform1, String platform2) {
        Map<String, Map<String, Long>> stats1 = dataManager.getCountryStats(platform1, null);
        Map<String, Map<String, Long>> stats2 = dataManager.getCountryStats(platform2, null);

        sender.sendMessage(ChatColor.GOLD + "Country Statistics Comparison:");
        for (String tier : Arrays.asList("S", "A", "B", "C", "D")) {
            Map<String, Long> tierStats1 = stats1.getOrDefault(tier, Map.of());
            Map<String, Long> tierStats2 = stats2.getOrDefault(tier, Map.of());

            long total1 = tierStats1.getOrDefault("total", 0L);
            long total2 = tierStats2.getOrDefault("total", 0L);
            long java1 = tierStats1.getOrDefault("java", 0L);
            long java2 = tierStats2.getOrDefault("java", 0L);
            long bedrock1 = tierStats1.getOrDefault("bedrock", 0L);
            long bedrock2 = tierStats2.getOrDefault("bedrock", 0L);

            sender.sendMessage(ChatColor.YELLOW + tier + " Tier:");
            sender.sendMessage(ChatColor.YELLOW + "  Total: " + (total1 + total2) + " (" + platform1 + ": " + total1 + " & " + platform2 + ": " + total2 + ")");
            sender.sendMessage(ChatColor.YELLOW + "  Java: " + (java1 + java2) + " (" + platform1 + ": " + java1 + " & " + platform2 + ": " + java2 + ")");
            sender.sendMessage(ChatColor.YELLOW + "  Bedrock: " + (bedrock1 + bedrock2) + " (" + platform1 + ": " + bedrock1 + " & " + platform2 + ": " + bedrock2 + ")");

            if (total2 > 0) {
                double totalDiff = ((total1 - total2) * 100.0 / total2);
                double javaDiff = ((java1 - java2) * 100.0 / java2);
                double bedrockDiff = ((bedrock1 - bedrock2) * 100.0 / bedrock2);

                String message = "  " + platform1 + " is " + (totalDiff >= 0 ? "outperforming" : "lacking to") + " " + platform2 + " in ";
                message += "total joins by " + String.format("%.1f", Math.abs(totalDiff)) + "%, ";
                message += "Java joins by " + String.format("%.1f", Math.abs(javaDiff)) + "%, ";
                message += (bedrockDiff >= 0 ? "and" : "but is lacking in") + " Bedrock joins by " + String.format("%.1f", Math.abs(bedrockDiff)) + "%";
                sender.sendMessage(ChatColor.YELLOW + message);
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Country Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/easystats countries view <platform> [-t <time>] - View country statistics");
        sender.sendMessage(ChatColor.YELLOW + "/easystats countries compare <platform1> <platform2> - Compare country statistics");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("view", "compare");
        }
        return null;
    }
} 