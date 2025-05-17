package de.Main.OneBlock;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    private final List<String> subCommands = Arrays.asList(
            "join", "delete", "visit", "rebirth", "trust", "accept", "deny", "unban", "remove", "leave"
    );


    private final List<String> commandsWithPlayerArgument = Arrays.asList(
            "visit", "add", "trust", "deny", "unban", "remove", "leave"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (!command.getName().equalsIgnoreCase("ob")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> matches = new ArrayList<>();

            for (String sub : subCommands) {
                if (sub.startsWith(input)) {
                    matches.add(sub);
                }
            }

            Collections.sort(matches);
            return matches;
        }


        if (args.length == 2) {
            String input = args[1].toLowerCase();
            String firstArg = args[0].toLowerCase();

            for (String cmd : commandsWithPlayerArgument) {
                if (cmd.startsWith(firstArg)) {
                    List<String> matches = new ArrayList<>();
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        String name = onlinePlayer.getName();
                        if (name.toLowerCase().startsWith(input)) {
                            matches.add(name);
                        }
                    }
                    Collections.sort(matches);
                    return matches;
                }
            }
        }

        return Collections.emptyList();
    }
}
