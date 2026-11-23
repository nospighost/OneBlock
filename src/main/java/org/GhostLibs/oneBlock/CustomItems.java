package org.GhostLibs.oneBlock;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CustomItems {

    public static ItemStack EMPTY;

    public void init(){
        EMPTY = new ItemStack(Material.PAPER);
        EMPTY.editMeta(meta -> {
            meta.setDisplayName("");
            meta.setCustomModelData(10005);
        });
    }

}
