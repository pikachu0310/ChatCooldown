package me.AhmHkn.ChatCooldown;

import me.AhmHkn.ChatCooldown.Commands.Command;
import me.AhmHkn.ChatCooldown.Events.LegacyChatListener;
import me.AhmHkn.ChatCooldown.Events.LunaChatHook;
import me.AhmHkn.ChatCooldown.Events.PaperChatListener;
import me.AhmHkn.ChatCooldown.bStats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private CooldownService cooldownService;
    private Metrics metrics;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        cooldownService = new CooldownService(this);
        cooldownService.reload();

        registerChatListeners();
        registerCommands();

        metrics = new Metrics(this, 12728);
    }

    public CooldownService getCooldownService() {
        return cooldownService;
    }

    public void reloadSettings() {
        reloadConfig();
        cooldownService.reload();
    }

    private void registerChatListeners() {
        boolean lunaChatHooked = false;
        if (getConfig().getBoolean("LunaChat.Enabled", true)) {
            lunaChatHooked = LunaChatHook.register(this, cooldownService);
        }

        if (lunaChatHooked && !getConfig().getBoolean("LunaChat.AlsoListenToNativeChat", false)) {
            getLogger().info("Using LunaChat hook for chat cooldowns.");
            return;
        }

        if (isPaperChatEventAvailable()) {
            Bukkit.getPluginManager().registerEvents(new PaperChatListener(cooldownService), this);
            getLogger().info("Using Paper AsyncChatEvent for chat cooldowns.");
        } else {
            Bukkit.getPluginManager().registerEvents(new LegacyChatListener(cooldownService), this);
            getLogger().info("Using Bukkit AsyncPlayerChatEvent for chat cooldowns.");
        }
    }

    private void registerCommands() {
        PluginCommand command = getCommand("cdreload");
        if (command != null) {
            command.setExecutor(new Command(this));
        }
    }

    private boolean isPaperChatEventAvailable() {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent", false, getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
