package com.fluxer.srv.listeners;

import com.fluxer.srv.FluxerListener;
import com.fluxer.srv.FluxerSRV;
import com.google.gson.JsonObject;
import org.bukkit.ChatColor;

public class FluxerMessageListener implements FluxerListener {

    private final FluxerSRV plugin;

    public FluxerMessageListener(FluxerSRV plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageCreate(JsonObject message) {
        if (!message.has("author") || !message.has("channel_id") || !message.has("content")) {
            return;
        }

        JsonObject author = message.getAsJsonObject("author");
        if (author.has("bot") && author.get("bot").getAsBoolean()) {
            return; // Ignore bot messages
        }

        String channelId = message.get("channel_id").getAsString();
        String content = message.get("content").getAsString();
        String username = author.get("username").getAsString();

        if (channelId.equals(plugin.getMainChannelId())) {
            // Translate color codes just in case, and format the message
            String formattedMessage = ChatColor.translateAlternateColorCodes('&',
                    "&9[Fluxer] &7" + username + "&f: " + content);

            // Broadcast to the server
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getServer().broadcastMessage(formattedMessage);
            });

        } else if (channelId.equals(plugin.getConsoleChannelId())) {
            // Execute command
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getLogger().info("Executing console command from Fluxer (" + username + "): " + content);
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), content);
            });
        }
    }
}
