package com.fluxer.srv;

import okhttp3.OkHttpClient;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class FluxerSRV extends JavaPlugin {

    private FluxerClient fluxerClient;
    private OkHttpClient httpClient;

    private String mainChannelId;
    private String consoleChannelId;

    private com.fluxer.srv.logging.FluxerConsoleAppender consoleAppender;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String token = getConfig().getString("botToken");
        mainChannelId = getConfig().getString("channels.global");
        consoleChannelId = getConfig().getString("channels.console");

        if (token == null || token.isEmpty() || token.equals("Bot Token_Here")) {
            getLogger().severe("Bot token is not set! Please configure it in config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        httpClient = new OkHttpClient();
        fluxerClient = new FluxerClient(token, getLogger(), httpClient);

        // Register listeners and command executors
        getServer().getPluginManager().registerEvents(new com.fluxer.srv.listeners.ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new com.fluxer.srv.listeners.JoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new com.fluxer.srv.listeners.DeathListener(this), this);

        try {
            fluxerClient.setListener(new com.fluxer.srv.listeners.FluxerMessageListener(this));
            fluxerClient.connect();
            if (mainChannelId != null && !mainChannelId.isEmpty() && !mainChannelId.equals("000000000000000000")) {
                fluxerClient.getRestApi().sendMessage(mainChannelId,
                        getConfig().getString("formats.serverStart", "🟩 **Server Started**"));
            }
        } catch (Exception e) {
            getLogger().severe("Failed to connect to Fluxer Gateway: " + e.getMessage());
            e.printStackTrace();
        }

        // Setup Console Appender
        try {
            org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) org.apache.logging.log4j.LogManager
                    .getRootLogger();
            consoleAppender = new com.fluxer.srv.logging.FluxerConsoleAppender(this);
            consoleAppender.start();
            coreLogger.addAppender(consoleAppender);
            consoleAppender.startTask();
        } catch (Exception e) {
            getLogger().warning("Failed to initialize FluxerConsoleAppender. Console logs won't be forwarded.");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (consoleAppender != null) {
            consoleAppender.stopTask();
            consoleAppender.stop();
            try {
                org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) org.apache.logging.log4j.LogManager
                        .getRootLogger();
                coreLogger.removeAppender(consoleAppender);
            } catch (Exception ignored) {
            }
        }

        if (fluxerClient != null) {
            if (mainChannelId != null && !mainChannelId.isEmpty() && !mainChannelId.equals("000000000000000000")) {
                try {
                    fluxerClient.getRestApi().sendMessage(mainChannelId,
                            getConfig().getString("formats.serverStop", "🟥 **Server Stopped**"));
                } catch (IOException e) {
                    getLogger().warning("Failed to send server stop message: " + e.getMessage());
                }
            }
            fluxerClient.shutdown();
        }

        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }

    public FluxerClient getFluxerClient() {
        return fluxerClient;
    }

    public String getMainChannelId() {
        return mainChannelId;
    }

    public String getConsoleChannelId() {
        return consoleChannelId;
    }
}
