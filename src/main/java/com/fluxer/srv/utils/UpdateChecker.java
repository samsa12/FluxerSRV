package com.fluxer.srv.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateChecker {
    private final JavaPlugin plugin;
    private final int resourceId;

    public UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = java.net.URI.create(
                    "https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).toURL().openStream();
                    Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (Exception exception) {
                plugin.getLogger().info("Unable to check for updates: " + exception.getMessage());
            }
        });
    }
}
