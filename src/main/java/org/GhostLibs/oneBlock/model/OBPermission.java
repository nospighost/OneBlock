package org.GhostLibs.oneBlock.model;

import org.bukkit.Material;

public enum OBPermission {
    BREAK_ONEBLOCK(Material.GRASS_BLOCK);

    private final Material icon;

    OBPermission(Material icon) {
        this.icon = icon;
    }

    public Material getIcon() {
        return icon;
    }
}
