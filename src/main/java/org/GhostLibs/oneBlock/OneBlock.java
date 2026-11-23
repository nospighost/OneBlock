package org.GhostLibs.oneBlock;

import co.aikar.commands.PaperCommandManager;
import de.main.database.db.DBM;
import de.main.database.util.MessageUtil;
import org.GhostLibs.oneBlock.commands.OBCommands;
import org.GhostLibs.oneBlock.listener.JoinListener;
import org.GhostLibs.oneBlock.manager.OBManager;
import org.GhostLibs.oneBlock.manager.PhaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneBlock extends JavaPlugin {

    private DBM dbm;
    private OBManager obManager;
    private static OneBlock instance;
    private PaperCommandManager commandManager;
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

        obManager = new OBManager(dbm, this);

        Sprache.init(this, getConfig().getString("language"));
        PhaseManager.setPlugin(this);
        PhaseManager.loadPhases();

        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new OBCommands(this));

        getServer().getPluginManager().registerEvents(new JoinListener(), this);

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

    public DBM getDbm() {
        return dbm;
    }

    public OBManager getObManager() {
        return obManager;
    }

}
