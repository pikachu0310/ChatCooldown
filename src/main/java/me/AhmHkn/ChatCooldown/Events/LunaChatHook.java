package me.AhmHkn.ChatCooldown.Events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import me.AhmHkn.ChatCooldown.CooldownService;
import me.AhmHkn.ChatCooldown.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

public final class LunaChatHook {
    private static final String PRE_CHAT_EVENT = "com.github.ucchyocean.lc3.bukkit.event.LunaChatBukkitPreChatEvent";

    private LunaChatHook() {
    }

    public static boolean register(Main plugin, CooldownService cooldownService) {
        if (!Bukkit.getPluginManager().isPluginEnabled("LunaChat")) {
            return false;
        }

        try {
            Class<? extends Event> eventClass = Class.forName(PRE_CHAT_EVENT).asSubclass(Event.class);
            Listener listener = new Listener() {
            };
            EventExecutor executor = new LunaChatExecutor(cooldownService);
            Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    listener,
                    EventPriority.LOWEST,
                    executor,
                    plugin,
                    true);
            return true;
        } catch (ClassNotFoundException | ClassCastException ex) {
            plugin.getLogger().warning("LunaChat is installed, but its PreChat event was not found: " + ex.getMessage());
            return false;
        }
    }

    private static final class LunaChatExecutor implements EventExecutor {
        private final CooldownService cooldownService;

        private LunaChatExecutor(CooldownService cooldownService) {
            this.cooldownService = cooldownService;
        }

        @Override
        public void execute(Listener listener, Event event) throws EventException {
            if (!PRE_CHAT_EVENT.equals(event.getClass().getName())) {
                return;
            }

            if (!(event instanceof Cancellable cancellable) || cancellable.isCancelled()) {
                return;
            }

            try {
                Object member = event.getClass().getMethod("getMember").invoke(event);
                String memberName = invokeString(member, "getName");
                boolean bypass = invokeBoolean(member, "hasPermission", cooldownService.getBypassPermission());
                CooldownService.CheckResult result = cooldownService.check(memberName, bypass);
                if (result.isAllowed()) {
                    return;
                }

                cancellable.setCancelled(true);
                Player player = Bukkit.getPlayerExact(memberName);
                if (player != null) {
                    cooldownService.notify(player, result.remainingMillis());
                } else {
                    invokeSendMessage(member, cooldownService.formatMessage(result.remainingMillis()));
                }
            } catch (ReflectiveOperationException ex) {
                throw new EventException(ex);
            }
        }

        private String invokeString(Object target, String methodName)
                throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value == null ? "" : value.toString();
        }

        private boolean invokeBoolean(Object target, String methodName, String argument)
                throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Method method = target.getClass().getMethod(methodName, String.class);
            Object value = method.invoke(target, argument);
            return value instanceof Boolean && (Boolean) value;
        }

        private void invokeSendMessage(Object target, String message)
                throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Method method = target.getClass().getMethod("sendMessage", String.class);
            method.invoke(target, message);
        }
    }
}
