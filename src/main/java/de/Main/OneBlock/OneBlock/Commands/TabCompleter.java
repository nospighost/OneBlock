package de.Main.OneBlock.OneBlock.Commands;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    private final Map<String, String> permissionMap = new HashMap<>();

    public TabCompleter() {
        permissionMap.put("join", "oneblock.join");
        permissionMap.put("delete", "oneblock.delete");
        permissionMap.put("visit", "oneblock.visit");
        permissionMap.put("rebirth", "oneblock.rebirth");
        permissionMap.put("trust", "oneblock.trust");
        permissionMap.put("accept", "oneblock.accept");
        permissionMap.put("deny", "oneblock.deny");
        permissionMap.put("unban", "oneblock.unban");
        permissionMap.put("remove", "oneblock.remove");
        permissionMap.put("leave", "oneblock.leave");
        permissionMap.put("decline", "oneblock.decline");
        permissionMap.put("switchchunkbiome", "oneblock.switchChunkBiome");
        permissionMap.put("switchislandbiome", "oneblock.switchIslandBiome");

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        Player player = (Player) sender;

        if (!command.getName().equalsIgnoreCase("ob")) return Collections.emptyList();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();

            for (String sub : permissionMap.keySet()) {
                if (sub.startsWith(input) && hasPermission(player, sub)) {
                    matches.add(sub);
                }
            }

            Collections.sort(matches);
            return matches;
        }

        if (args.length == 2) {
            String firstArg = args[0].toLowerCase();
            String input = args[1].toLowerCase();


            if ((firstArg.equals("switchchunkbiome") || firstArg.equals("switchislandbiome")) && hasPermission(player, firstArg)) {
                List<String> biomeMatches = new ArrayList<>();
                for (Biome biome : Biome.values()) {
                    String biomeName = biome.name().toLowerCase();
                    if (biomeName.startsWith(input)) {
                        biomeMatches.add(biome.name());
                    }
                }
                Collections.sort(biomeMatches);
                return biomeMatches;
            }



            if (permissionMap.containsKey(firstArg) && hasPermission(player, firstArg)) {
                List<String> playerMatches = new ArrayList<>();

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    String name = onlinePlayer.getName();
                    if (name.toLowerCase().startsWith(input)) {
                        playerMatches.add(name);
                    }
                }

                Collections.sort(playerMatches);
                return playerMatches;
            }
        }


        return Collections.emptyList();
    }

    private boolean hasPermission(Player player, String subCommand) {
        return player.hasPermission("oneblock.admin")
                || player.hasPermission(permissionMap.get(subCommand))
                || player.isOp();
    }
}
