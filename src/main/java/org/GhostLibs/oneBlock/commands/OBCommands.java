package org.GhostLibs.oneBlock.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import org.GhostLibs.oneBlock.gui.IslandListGUI;
import org.bukkit.entity.Player;

@CommandAlias("ob")
public class OBCommands extends BaseCommand {

    public OBCommands() {

    }

    @Default
    @CommandPermission("oneblock.default")
    public void onDefault(Player player) {
        IslandListGUI islandListGUI = new IslandListGUI(player.getUniqueId());
        player.openInventory(islandListGUI.getInventory());
    }

}
