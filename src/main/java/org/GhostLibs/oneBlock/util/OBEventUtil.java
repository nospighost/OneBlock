package org.GhostLibs.oneBlock.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public final class OBEventUtil {

    private OBEventUtil() {
    }

    public static <T extends Event> CompletableFuture<T> callSync(JavaPlugin plugin, T event) {
        CompletableFuture<T> future = new CompletableFuture<>();

        Runnable task = () -> {
            try {
                Bukkit.getPluginManager().callEvent(event);
                future.complete(event);
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        };

        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }

        return future;
    }
}