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
        if (isPaperChatEventAvailable()) {
            Bukkit.getPluginManager().registerEvents(new PaperChatListener(cooldownService), this);
            getLogger().info("Using Paper AsyncChatEvent for chat cooldowns.");
        } else {
            Bukkit.getPluginManager().registerEvents(new LegacyChatListener(cooldownService), this);
            getLogger().info("Using Bukkit AsyncPlayerChatEvent for chat cooldowns.");
        }

        if (getConfig().getBoolean("LunaChat.HookPreChat", false) && LunaChatHook.register(this, cooldownService)) {
            getLogger().info("Using LunaChat PreChat hook for additional chat cooldown coverage.");
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
