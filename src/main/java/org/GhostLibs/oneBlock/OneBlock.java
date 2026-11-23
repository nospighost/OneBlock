package org.GhostLibs.oneBlock;

import de.main.database.db.DBM;
import org.GhostLibs.oneBlock.manager.OBManager;
import org.GhostLibs.oneBlock.manager.PhaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class OneBlock extends JavaPlugin {

    private DBM dbm;
    private OBManager obManager;
    private static OneBlock instance;

    public static OneBlock getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        DBM.createDBConfig(this);
        dbm = DBM.connect(this);

        DatabaseSchema databaseSchema = new DatabaseSchema(dbm);
        databaseSchema.create();

        obManager = new OBManager(dbm);

        Übersetzer.init(this, getConfig().getString("language"));
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

    public DBM getDbm() {
        return dbm;
    }

    public OBManager getObManager() {
        return obManager;
    }
}
