package org.GhostLibs.oneBlock;

import co.aikar.commands.PaperCommandManager;
import de.main.database.db.DBM;
import de.main.database.util.MessageUtil;
import org.GhostLibs.oneBlock.commands.OBCommands;
import org.GhostLibs.oneBlock.gui.IslandListGUIListener;
import org.GhostLibs.oneBlock.listener.BlockListener;
import org.GhostLibs.oneBlock.listener.IslandCacheListener;
import org.GhostLibs.oneBlock.listener.JoinListener;
import org.GhostLibs.oneBlock.manager.IslandCache;
import org.GhostLibs.oneBlock.manager.OBManager;
import org.GhostLibs.oneBlock.manager.PhaseManager;
import org.GhostLibs.oneBlock.manager.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.print.Paper;

public final class OneBlock extends JavaPlugin {

    private DBM dbm;
    private OBManager obManager;
    private static OneBlock instance;
    private WorldManager worldManager;
    public static OneBlock getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("lang/de.json", false);

        DBM.createDBConfig(this);
        dbm = DBM.connect(this);

        DatabaseSchema databaseSchema = new DatabaseSchema(dbm);
        databaseSchema.create();

        worldManager = new WorldManager(this);
        obManager = new OBManager(dbm, this, worldManager);

        Sprache.init(this, getConfig().getString("language"));
        PhaseManager.setPlugin(this);
        PhaseManager.loadPhases();

        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new OBCommands(this));

        IslandCache cache = new IslandCache(obManager);
        getServer().getPluginManager().registerEvents(new JoinListener(worldManager), this);
        getServer().getPluginManager().registerEvents(new IslandCacheListener(cache, worldManager), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this, worldManager, cache), this);
        getServer().getPluginManager().registerEvents(new IslandListGUIListener(), this);

        MessageUtil.setPREFIX(Sprache.get("prefix"));
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

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public DBM getDbm() {
        return dbm;
    }

    public OBManager getObManager() {
        return obManager;
    }

}
