package gg.gianluca.easystats.command.subcommands;

import gg.gianluca.easystats.EasyStats;
import gg.gianluca.easystats.command.base.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class EasyStatsCommand extends BaseCommand {
    private final Map<String, BaseCommand> subcommands;

    public EasyStatsCommand(EasyStats plugin) {
        super(plugin, "easystats");
        this.subcommands = new HashMap<>();
        
        // Register subcommands
        subcommands.put("platform", new PlatformCommand(plugin));
        subcommands.put("countries", new CountriesCommand(plugin));
        subcommands.put("revenue", new RevenueCommand(plugin));
        subcommands.put("campaign", new CampaignCommand(plugin));
        subcommands.put("session", new SessionCommand(plugin));
        subcommands.put("reload", new ReloadCommand(plugin));
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();
        BaseCommand executor = subcommands.get(subcommand);

        if (executor == null) {
            sendHelp(sender);
            return true;
        }

        // Remove the subcommand from args and pass the rest to the executor
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        return executor.execute(sender, command, label, newArgs);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== EasyStats Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/easystats platform - Platform statistics");
        sender.sendMessage(ChatColor.YELLOW + "/easystats countries - Country statistics");
        sender.sendMessage(ChatColor.YELLOW + "/easystats revenue - Revenue tracking");
        sender.sendMessage(ChatColor.YELLOW + "/easystats campaign - Campaign management");
        sender.sendMessage(ChatColor.YELLOW + "/easystats session - Session statistics");
        sender.sendMessage(ChatColor.YELLOW + "/easystats reload - Reload configuration");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("platform", "countries", "revenue", "campaign", "session", "reload");
        }

        if (args.length > 1) {
            String subcommand = args[0].toLowerCase();
            BaseCommand executor = subcommands.get(subcommand);
            if (executor != null) {
                return executor.tabComplete(sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
            }
        }

        return null;
    }
} 