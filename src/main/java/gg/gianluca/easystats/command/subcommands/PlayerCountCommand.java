package gg.gianluca.easystats.command.subcommands;

import gg.gianluca.easystats.EasyStats;
import gg.gianluca.easystats.command.base.BaseCommand;
import gg.gianluca.easystats.data.DataManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlayerCountCommand extends BaseCommand {
    private final DataManager dataManager;

    public PlayerCountCommand(EasyStats plugin) {
        super(plugin, "easystats.playercount", "/easystats playercount view <platform> [-all]", "View player count statistics for a platform");
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: " + getUsage());
            return true;
        }

        String platform = args[2];
        boolean showAll = args.length > 3 && args[3].equalsIgnoreCase("-all");

        if (showAll) {
            // Show global player count stats
            Map<String, Object> globalStats = dataManager.getGlobalPlayerCountStats();
            sender.sendMessage("§6=== Global Player Count Statistics ===");
            sender.sendMessage("§7Current Players: §f" + globalStats.get("current"));
            sender.sendMessage("§7Average Players:");
            Map<String, Integer> averages = (Map<String, Integer>) globalStats.get("averages");
            sender.sendMessage("  §7- 24h: §f" + averages.get("24h"));
            sender.sendMessage("  §7- 7d: §f" + averages.get("7d"));
            sender.sendMessage("  §7- 14d: §f" + averages.get("14d"));
            sender.sendMessage("  §7- 30d: §f" + averages.get("30d"));
            sender.sendMessage("§7Peak Players: §f" + globalStats.get("peak_count"));
            sender.sendMessage("§7Peak Time: §f" + globalStats.get("peak_time"));
        } else {
            // Show platform-specific stats
            Map<String, Object> stats = dataManager.getPlayerCountStats(platform);
            if (stats == null) {
                sender.sendMessage("§cNo statistics found for platform: " + platform);
                return true;
            }

            sender.sendMessage("§6=== Player Count Statistics for " + platform + " ===");
            sender.sendMessage("§7Current Players: §f" + stats.get("current"));
            sender.sendMessage("§7Average Players:");
            Map<String, Integer> averages = (Map<String, Integer>) stats.get("averages");
            sender.sendMessage("  §7- 24h: §f" + averages.get("24h"));
            sender.sendMessage("  §7- 7d: §f" + averages.get("7d"));
            sender.sendMessage("  §7- 14d: §f" + averages.get("14d"));
            sender.sendMessage("  §7- 30d: §f" + averages.get("30d"));
            sender.sendMessage("§7Peak Players: §f" + stats.get("peak_count"));
            sender.sendMessage("§7Peak Time: §f" + stats.get("peak_time"));
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            return new ArrayList<>();
        }

        if (args.length == 3) {
            // Return list of platforms
            return new ArrayList<>(dataManager.getPlatforms());
        } else if (args.length == 4) {
            // Return -all option
            return Arrays.asList("-all");
        }

        return new ArrayList<>();
    }
} 