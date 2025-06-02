package de.Main.OneBlock.OneBlock.Player;

import de.Main.OneBlock.Main;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import static java.lang.Long.MIN_VALUE;

public class ActionBar {
    public static int frame = 0;
    public static boolean forward = true;

    public static void sendActionbarProgress(Player player, int currentLevel, int missingBlocks, int totalBlocks, boolean Durchgespielt) {

        if (currentLevel >= Main.config.getInt("maxlevel") || Durchgespielt == true) {
            StringBuilder barBuilder = new StringBuilder("§7[");

            if (forward) {
                frame++;
                if (frame >= 9) forward = false;
            } else {
                frame--;
                if (frame <= 0) forward = true;
            }

            for (int i = 0; i < 10; i++) {
                if (i == frame) {
                    barBuilder.append("§a█");
                } else {
                    barBuilder.append("§2█");
                }
            }

            barBuilder.append("§7]");

            String bar = barBuilder.toString();
            String msg = "§bLevel: " + currentLevel + " §8| §6§l∞ " + bar;
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
            return;
        } else if (totalBlocks <= 0 && missingBlocks == MIN_VALUE) totalBlocks = 1;
        double progress = (double) (totalBlocks - missingBlocks) / totalBlocks;
        int filled = (int) (progress * 10);

        double radians = frame * 0.3;
        int wavePos = (int) ((Math.sin(radians) + 1) * 4.5);

        StringBuilder bar = new StringBuilder("§7[");
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                if (i == wavePos) {
                    bar.append("§a█");
                } else {
                    bar.append("§a█");
                }
            } else {
                if (i == wavePos) {
                    bar.append("§7█");
                } else {
                    bar.append("§7█");
                }
            }
        }
        bar.append("§7]");

        String message = "§bLevel: §e" + currentLevel +
                " §8| " + bar +
                " §7Noch §c" + missingBlocks + " §7Blöcke";
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
