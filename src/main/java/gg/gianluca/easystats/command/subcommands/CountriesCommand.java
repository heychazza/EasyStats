package gg.gianluca.easystats.command.subcommands;

import gg.gianluca.easystats.EasyStats;
import gg.gianluca.easystats.command.base.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CountriesCommand extends BaseCommand {
    public CountriesCommand(EasyStats plugin) {
        super(plugin, "easystats.countries", "/easystats countries view <platform> [-t <time>]", "View country statistics");
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: " + getUsage());
            return true;
        }

        String platform = args[1];
        String timeFilter = args.length > 3 && args[2].equals("-t") ? args[3] : null;

        Map<String, Map<String, Map<String, Long>>> countryStats = plugin.getDataManager().getCountryStats(platform, timeFilter);
        if (countryStats.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No statistics available for platform: " + platform);
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Country Statistics for " + platform + " ===");
        for (Map.Entry<String, Map<String, Map<String, Long>>> tierEntry : countryStats.entrySet()) {
            String tier = tierEntry.getKey();
            sender.sendMessage(ChatColor.YELLOW + "\nTier: " + tier);

            for (Map.Entry<String, Map<String, Long>> countryEntry : tierEntry.getValue().entrySet()) {
                String country = countryEntry.getKey();
                Map<String, Long> clientStats = countryEntry.getValue();
                long total = clientStats.values().stream().mapToLong(Long::longValue).sum();
                long javaCount = clientStats.getOrDefault("java", 0L);
                long bedrockCount = clientStats.getOrDefault("bedrock", 0L);

                double javaPercent = total > 0 ? (javaCount * 100.0) / total : 0;
                double bedrockPercent = total > 0 ? (bedrockCount * 100.0) / total : 0;

                sender.sendMessage(ChatColor.WHITE + country + ": " + total + " players " +
                        String.format("(Java: %.1f%%, Bedrock: %.1f%%)", javaPercent, bedrockPercent));
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("view");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
            completions.addAll(plugin.getDataManager().getAllHostnames());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("view")) {
            completions.add("-t");
        }

        return completions;
    }
} 