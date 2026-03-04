package com.fluxer.srv.listeners;

import com.fluxer.srv.FluxerSRV;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final FluxerSRV plugin;

    public ChatListener(FluxerSRV plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String mainChannelId = plugin.getMainChannelId();
        if (mainChannelId == null || mainChannelId.isEmpty() || mainChannelId.equals("000000000000000000")) {
            return;
        }

        String rawMessage = event.getMessage();
        if (plugin.getConfig().getBoolean("fluxer-global-only", true)) {
            if (!rawMessage.startsWith("!")) {
                return; // Local chat, DO NOT SEND to Fluxer
            }
            rawMessage = rawMessage.substring(1).trim(); // Remove '!' for Discord
        }

        String format = plugin.getConfig().getString("formats.chat", "**%player_name%**: %message%");
        String message = format
                .replace("%player_name%", event.getPlayer().getName())
                .replace("%message%", rawMessage);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getFluxerClient().getRestApi().sendMessage(mainChannelId, message);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send chat message to Fluxer: " + e.getMessage());
            }
        });
    }
}
