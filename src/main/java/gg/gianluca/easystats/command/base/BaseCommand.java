package gg.gianluca.easystats.command.base;

import gg.gianluca.easystats.EasyStats;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    protected final EasyStats plugin;
    protected final String permission;

    public BaseCommand(EasyStats plugin, String permission) {
        this.plugin = plugin;
        this.permission = permission;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        return execute(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(permission)) {
            return null;
        }
        return tabComplete(sender, command, alias, args);
    }

    public abstract boolean execute(CommandSender sender, Command command, String label, String[] args);
    public abstract List<String> tabComplete(CommandSender sender, Command command, String alias, String[] args);
} 