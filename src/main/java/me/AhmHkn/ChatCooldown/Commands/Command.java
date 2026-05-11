package me.AhmHkn.ChatCooldown.Commands;

import me.AhmHkn.ChatCooldown.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class Command implements CommandExecutor {
    private final Main plugin;

    public Command(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!sender.hasPermission("chatcooldown.reload")) {
            sender.sendMessage(color(plugin.getConfig().getString("Prefix")) + ChatColor.RED + "You don't have permission!");
            return true;
        }

        plugin.reloadSettings();
        sender.sendMessage(color(plugin.getConfig().getString("Prefix")) + ChatColor.GREEN + "Reloaded config.");
        return true;
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
