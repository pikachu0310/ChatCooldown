package me.AhmHkn.ChatCooldown;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class CooldownService {
    private final Main plugin;
    private final Map<UUID, Long> playerCooldowns = new ConcurrentHashMap<>();
    private final Map<String, Long> namedCooldowns = new ConcurrentHashMap<>();

    private long cooldownMillis;
    private String prefix;
    private String message;
    private String bypassPermission;

    public CooldownService(Main plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        cooldownMillis = Math.max(0L, Math.round(plugin.getConfig().getDouble("CooldownSeconds", 3.0D) * 1000.0D));
        prefix = color(plugin.getConfig().getString("Prefix", "&9ChatCooldown &8&l» "));
        message = plugin.getConfig().getString("Message", "&cCalm down! &7(%time%s)");
        bypassPermission = plugin.getConfig().getString("BypassPermission", "chatcooldown.bypass");
        playerCooldowns.clear();
        namedCooldowns.clear();
    }

    public CheckResult check(Player player) {
        if (player.hasPermission(bypassPermission)) {
            return CheckResult.allowed();
        }
        return check(player.getUniqueId(), null);
    }

    public CheckResult check(String memberName, boolean bypass) {
        if (bypass) {
            return CheckResult.allowed();
        }

        Player player = Bukkit.getPlayerExact(memberName);
        if (player != null) {
            return check(player);
        }

        String key = memberName.toLowerCase(Locale.ROOT);
        return check(null, key);
    }

    public String getBypassPermission() {
        return bypassPermission;
    }

    public void notify(Player player, long remainingMillis) {
        if (message == null || message.isBlank()) {
            return;
        }

        Runnable send = () -> player.sendMessage(formatMessage(remainingMillis));
        if (Bukkit.isPrimaryThread()) {
            send.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, send);
        }
    }

    public String formatMessage(long remainingMillis) {
        double seconds = Math.max(0.0D, remainingMillis / 1000.0D);
        String secondsText = String.format(Locale.US, "%.1f", seconds);
        String formatted = message
                .replace("%time%", secondsText)
                .replace("%seconds%", secondsText)
                .replace("{seconds}", secondsText);
        return prefix + color(formatted);
    }

    private CheckResult check(UUID uuid, String nameKey) {
        if (cooldownMillis <= 0L) {
            return CheckResult.allowed();
        }

        long now = System.currentTimeMillis();
        Long until = uuid != null ? playerCooldowns.get(uuid) : namedCooldowns.get(nameKey);
        if (until != null && until > now) {
            return CheckResult.blocked(until - now);
        }

        long next = now + cooldownMillis;
        if (uuid != null) {
            playerCooldowns.put(uuid, next);
        } else {
            namedCooldowns.put(nameKey, next);
        }
        return CheckResult.allowed();
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public static final class CheckResult {
        private final boolean allowed;
        private final long remainingMillis;

        private CheckResult(boolean allowed, long remainingMillis) {
            this.allowed = allowed;
            this.remainingMillis = remainingMillis;
        }

        public static CheckResult allowed() {
            return new CheckResult(true, 0L);
        }

        public static CheckResult blocked(long remainingMillis) {
            return new CheckResult(false, remainingMillis);
        }

        public boolean isAllowed() {
            return allowed;
        }

        public long remainingMillis() {
            return remainingMillis;
        }
    }
}
