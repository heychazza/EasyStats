package gg.gianluca.easystats.command.subcommands;

import gg.gianluca.easystats.EasyStats;
import gg.gianluca.easystats.command.base.BaseCommand;
import gg.gianluca.easystats.session.SessionManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class SessionCommand extends BaseCommand {
    private final SessionManager sessionManager;

    public SessionCommand(EasyStats plugin) {
        super(plugin, "session", "/easystats session view <platform> [-t <time>]", "View session statistics");
        this.sessionManager = plugin.getSessionManager();
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        String hostname = args[1];

        switch (subcommand) {
            case "check":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /easystats session check <hostname> <time>");
                    return true;
                }
                handleCheck(sender, hostname, args[2]);
                break;
            case "compare":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /easystats session compare <hostname1> <hostname2>");
                    return true;
                }
                handleCompare(sender, hostname, args[2]);
                break;
            default:
                sendHelp(sender);
                break;
        }
        return true;
    }

    private void handleCheck(CommandSender sender, String hostname, String time) {
        double avgTime = sessionManager.getAverageSessionTime(hostname);
        String formattedTime = formatDuration((long) avgTime);
        sender.sendMessage(ChatColor.GREEN + "Average session time for " + hostname + ": " + formattedTime);
    }

    private void handleCompare(CommandSender sender, String hostname1, String hostname2) {
        Map<String, Double> comparison = sessionManager.compareSessionTimes(hostname1, hostname2);
        
        String time1 = formatDuration(comparison.get(hostname1).longValue());
        String time2 = formatDuration(comparison.get(hostname2).longValue());
        String diff = formatDuration(comparison.get("difference").longValue());
        double diffPercent = comparison.get("difference_percentage");

        sender.sendMessage(ChatColor.GOLD + "=== Session Time Comparison ===");
        sender.sendMessage(ChatColor.YELLOW + hostname1 + ": " + time1);
        sender.sendMessage(ChatColor.YELLOW + hostname2 + ": " + time2);
        sender.sendMessage(ChatColor.YELLOW + "Difference: " + diff + " (" + String.format("%.1f", diffPercent) + "%)");
    }

    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Session Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/easystats session check <hostname> <time> - Check average session time");
        sender.sendMessage(ChatColor.YELLOW + "/easystats session compare <hostname1> <hostname2> - Compare session times");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("check", "compare");
        }
        return null;
    }
} 