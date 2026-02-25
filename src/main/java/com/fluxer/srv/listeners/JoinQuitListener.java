package com.fluxer.srv.listeners;

import com.fluxer.srv.FluxerSRV;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    private final FluxerSRV plugin;

    public JoinQuitListener(FluxerSRV plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        sendMessage("formats.join", "**%player_name%** joined the server!", event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        sendMessage("formats.quit", "**%player_name%** left the server!", event.getPlayer().getName());
    }

    private void sendMessage(String formatKey, String defaultFormat, String playerName) {
        String mainChannelId = plugin.getMainChannelId();
        if (mainChannelId == null || mainChannelId.isEmpty() || mainChannelId.equals("000000000000000000")) {
            return;
        }

        String format = plugin.getConfig().getString(formatKey, defaultFormat);
        String message = format.replace("%player_name%", playerName);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getFluxerClient().getRestApi().sendMessage(mainChannelId, message);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send join/quit message to Fluxer: " + e.getMessage());
            }
        });
    }
}
