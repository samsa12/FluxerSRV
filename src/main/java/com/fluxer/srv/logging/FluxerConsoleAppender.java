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
        super("FluxerConsoleAppender", null,
                PatternLayout.newBuilder().withPattern("[%d{HH:mm:ss} %level]: %msg").build(), false);
        this.plugin = plugin;
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
