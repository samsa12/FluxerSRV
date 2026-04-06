package com.fluxer.srv.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private final JavaPlugin plugin;
    private final String owner;
    private final String repo;

    public UpdateChecker(JavaPlugin plugin, String owner, String repo) {
        this.plugin = plugin;
        this.owner = owner;
        this.repo = repo;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            HttpURLConnection connection = null;

            try {
                URL url = URI.create("https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest").toURL();

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("User-Agent", plugin.getName());

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    plugin.getLogger().warning("GitHub update check failed with HTTP " + responseCode);
                    return;
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream())
                )) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                // Extract "tag_name": "v1.2.3"
                Pattern pattern = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(response.toString());

                if (matcher.find()) {
                    String latestVersion = matcher.group(1).trim();

                    Bukkit.getScheduler().runTask(plugin, () -> consumer.accept(latestVersion));
                } else {
                    plugin.getLogger().warning("Could not find tag_name in GitHub release response.");
                }

            } catch (Exception exception) {
                plugin.getLogger().warning("Unable to check GitHub updates: " + exception.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}
