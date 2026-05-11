package me.AhmHkn.ChatCooldown.Events;

import me.AhmHkn.ChatCooldown.CooldownService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class LegacyChatListener implements Listener {
    private final CooldownService cooldownService;

    public LegacyChatListener(CooldownService cooldownService) {
        this.cooldownService = cooldownService;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        CooldownService.CheckResult result = cooldownService.check(event.getPlayer());
        if (result.isAllowed()) {
            return;
        }

        event.setCancelled(true);
        cooldownService.notify(event.getPlayer(), result.remainingMillis());
    }
}
