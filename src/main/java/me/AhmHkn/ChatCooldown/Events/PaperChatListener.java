package me.AhmHkn.ChatCooldown.Events;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.AhmHkn.ChatCooldown.CooldownService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PaperChatListener implements Listener {
    private final CooldownService cooldownService;

    public PaperChatListener(CooldownService cooldownService) {
        this.cooldownService = cooldownService;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        CooldownService.CheckResult result = cooldownService.check(event.getPlayer());
        if (result.isAllowed()) {
            return;
        }

        event.setCancelled(true);
        cooldownService.notify(event.getPlayer(), result.remainingMillis());
    }
}
