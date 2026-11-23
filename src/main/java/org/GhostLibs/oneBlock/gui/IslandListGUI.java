package org.GhostLibs.oneBlock.gui;

import org.GhostLibs.oneBlock.CustomItems;
import org.GhostLibs.oneBlock.OneBlock;
import org.GhostLibs.oneBlock.Übersetzer;
import org.GhostLibs.oneBlock.model.PlayerData;
import org.GhostLibs.oneBlock.model.PlayerIsland;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IslandListGUI implements InventoryHolder {

    private PlayerIsland island;
    private PlayerData playerData;
    private Inventory inv;
    private UUID ownerUUID;

    public IslandListGUI(UUID uuid) {
        this.ownerUUID = uuid;
        OneBlock.getInstance().getObManager().getPlayerData(uuid).thenAccept(data -> {
            this.playerData = data;
        });
        CreateGUI();
    }

    private void CreateGUI(){
        IslandListGUI guiHolder = new IslandListGUI(ownerUUID);

        inv = Bukkit.createInventory(guiHolder, 27, "§b§lInseln");
        fillBorder();

        OneBlock.getInstance().getObManager().getPlayerIsland(ownerUUID).thenAccept(island -> {
            inv.setItem(11, createIslandItem(island));
        });

    }

    private ItemStack createIslandItem(PlayerIsland island) {
        ItemStack islandItem = new ItemStack(island.getOwnerId() == playerData.getPlayerId() ? Material.BEACON : Material.GRASS_BLOCK);
        islandItem.editMeta(meta -> {
           List<String> lore = new ArrayList<>();

            String ownerName = Bukkit.getOfflinePlayer(ownerUUID).getName();
            if (ownerName == null) {
                ownerName = "Unbekannt";
            }

            meta.setDisplayName(Übersetzer.get("UI.islandItemTitle", "%player%", ownerName));

            lore.add("");
            lore.add(Übersetzer.get("UI.islandName", "%name%", island.getIslandName()));
            lore.add(Übersetzer.get("UI.islandLocked", "%state%", island.isLocked() ? Übersetzer.get("UI.islandLockedYes") : Übersetzer.get("UI.islandLockedNo")));

        });

        return islandItem;
    }

    private void fillBorder() {
        ItemStack borderItem = CustomItems.EMPTY;
        int size = inv.getSize();

        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, borderItem);
            }
        }
    }


    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public void setInventory(Inventory inv) {
        this.inv = inv;
    }

    public void setIsland(PlayerIsland island) {
        this.island = island;
    }

    public PlayerIsland getIsland() {
        return island;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }
}

