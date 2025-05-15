package de.Main.OneBlock;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {


        if (command.getName().equalsIgnoreCase("ob")) {


            if (args.length == 1) {
                List<String> subCommands = Arrays.asList("join", "delete", "visit", "rebirth");
                List<String> result = new ArrayList<>();

                for (String sub : subCommands) {
                    if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                        result.add(sub);
                    }
                }

                return result;
            }


        }

        return null;
    }
}
