package com.fluxer.srv.logging;

import com.fluxer.srv.FluxerSRV;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Plugin(name = "FluxerConsoleAppender", category = "Core", elementType = "appender", printObject = true)
public class FluxerConsoleAppender extends AbstractAppender {

    private final FluxerSRV plugin;
    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private int taskId = -1;

    public FluxerConsoleAppender(FluxerSRV plugin) {
        super("FluxerConsoleAppender", null, createFlexiblePatternLayout(), false);
        this.plugin = plugin;
    }

    private static org.apache.logging.log4j.core.layout.PatternLayout createFlexiblePatternLayout() {
        try {
            // Try Log4j 2.14+ (Minecraft 1.17+)
            try {
                java.lang.reflect.Method newBuilder = PatternLayout.class.getMethod("newBuilder");
                Object builder = newBuilder.invoke(null);
                java.lang.reflect.Method withPattern = builder.getClass().getMethod("withPattern", String.class);
                withPattern.invoke(builder, "[%d{HH:mm:ss} %level]: %msg");
                java.lang.reflect.Method build = builder.getClass().getMethod("build");
                return (PatternLayout) build.invoke(builder);
            } catch (Exception ignored) {
            }

            // Try Log4j 2.8.1 (Minecraft 1.8.8 - 1.11)
            try {
                java.lang.reflect.Method create = PatternLayout.class.getMethod("createLayout",
                        String.class, Class.forName("org.apache.logging.log4j.core.layout.PatternSelector"),
                        Class.forName("org.apache.logging.log4j.core.config.Configuration"),
                        Class.forName("org.apache.logging.log4j.core.pattern.RegexReplacement"),
                        java.nio.charset.Charset.class,
                        boolean.class, boolean.class, String.class, String.class);
                return (PatternLayout) create.invoke(null, "[%d{HH:mm:ss} %level]: %msg", null, null, null, null, false,
                        false, null, null);
            } catch (Exception ignored) {
            }

            // Try Log4j 2.0-beta9 (Minecraft 1.7.10)
            try {
                java.lang.reflect.Method create = PatternLayout.class.getMethod("createLayout",
                        String.class, Class.forName("org.apache.logging.log4j.core.config.Configuration"),
                        Class.forName("org.apache.logging.log4j.core.pattern.RegexReplacement"), String.class,
                        String.class);
                return (PatternLayout) create.invoke(null, "[%d{HH:mm:ss} %level]: %msg", null, null, null, null);
            } catch (Exception ignored) {
            }

            // Fallback (older 2.x versions)
            java.lang.reflect.Method createDefault = PatternLayout.class.getMethod("createDefaultLayout");
            return (PatternLayout) createDefault.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void append(LogEvent event) {
        String message = event.getMessage().getFormattedMessage();
        if (event.getThrown() != null) {
            message += "\n" + event.getThrown().getMessage();
        }

        // Strip ANSI color codes
        message = message.replaceAll("\u001B\\[[;\\d]*m", "");
        messageQueue.add(message);
    }

    public void startTask() {
        if (taskId != -1)
            return;

        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (messageQueue.isEmpty())
                return;

            String consoleChannelId = plugin.getConsoleChannelId();
            if (consoleChannelId == null || consoleChannelId.isEmpty()
                    || consoleChannelId.equals("000000000000000000")) {
                messageQueue.clear();
                return;
            }

            StringBuilder sb = new StringBuilder("```\n");
            int count = 0;
            while (!messageQueue.isEmpty() && sb.length() < 1800 && count < 50) {
                sb.append(messageQueue.poll()).append("\n");
                count++;
            }
            sb.append("```");

            final String payload = sb.toString();

            new Thread(() -> {
                try {
                    plugin.getFluxerClient().getRestApi().sendMessage(consoleChannelId, payload);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to send console logs to Fluxer: " + e.getMessage());
                }
            }).start();

        }, 20L * 5, 20L * 5); // Run every 5 seconds
    }

    public void stopTask() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}
