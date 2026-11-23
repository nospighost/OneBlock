package org.GhostLibs.oneBlock;

import org.GhostLibs.oneBlock.manager.PhaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneBlock extends JavaPlugin {

    @Override
    public void onEnable() {

        PhaseManager.setPlugin(this);
        PhaseManager.loadPhases();

        getLogger().info("╔════════════════════════════════════╗");
        getLogger().info("║     Oneblock Start erfolgreich!    ║");
        getLogger().info("║     Plugin ist einsatzbereit       ║");
        getLogger().info("╚════════════════════════════════════╝");
    }

    @Override
    public void onDisable() {

        getLogger().info("╔════════════════════════════════════╗");
        getLogger().info("║     Oneblock Wurde deaktiviert!    ║");
        getLogger().info("╚════════════════════════════════════╝");


    }
}
