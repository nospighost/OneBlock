package org.GhostLibs.oneBlock.manager;

import org.GhostLibs.oneBlock.OneBlock;
import org.GhostLibs.oneBlock.models.OBPhase;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhaseManager {

    private static final Map<Integer, OBPhase> phaseCache = new HashMap<>();
    private static int highestPhaseId = 1;
    private static JavaPlugin plugin;

    public static void reloadPhases() {
        loadPhases();
    }

    public static void setPlugin(JavaPlugin plugin) {
        PhaseManager.plugin = plugin;
    }

    public static void loadPhases() {
        phaseCache.clear();
        highestPhaseId = 1;

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection phasesSection = config.getConfigurationSection("phases");

        if (phasesSection == null) return;

        for (String key : phasesSection.getKeys(false)) {
            try {
                int phaseId = Integer.parseInt(key);
                ConfigurationSection singlePhaseSection = phasesSection.getConfigurationSection(key);

                if (singlePhaseSection == null) continue;

                String name = singlePhaseSection.getString("name", "Unbekannte Phase");
                int blockAmount = singlePhaseSection.getInt("blockAmount", 1000);

                List<String> blockStrings = singlePhaseSection.getStringList("blocks");
                List<Material> materials = new ArrayList<>();

                for (String blockStr : blockStrings) {
                    try {
                        materials.add(Material.valueOf(blockStr.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Ungültiges Material '" + blockStr + "' in Phase " + phaseId);
                    }
                }

                OBPhase phase = new OBPhase(phaseId, name, blockAmount, materials);
                phaseCache.put(phaseId, phase);

                if (phaseId > highestPhaseId) {
                    highestPhaseId = phaseId;
                }

            } catch (NumberFormatException e) {
                plugin.getLogger().severe("Phasen-ID in der Config muss eine Zahl sein: " + key);
            }
        }
        plugin.getLogger().info(phaseCache.size() + " OneBlock-Phasen erfolgreich geladen. Höchste Phase: " + highestPhaseId);
    }

    public static OBPhase getPhase(int phaseId) {
        if (phaseId > highestPhaseId) {
            return phaseCache.get(highestPhaseId);
        }

        if (phaseCache.containsKey(phaseId)) {
            return phaseCache.get(phaseId);
        }

        plugin.getLogger().info("Phase " + phaseId + " fehlt im Cache. Lade nach...");
        loadPhases();

        return phaseCache.get(phaseId);
    }

    public static List<Material> getPhaseMaterials(int phaseId) {
        OBPhase phase = getPhase(phaseId);
        return phase != null ? phase.getBlocks() : List.of(Material.DIRT);
    }

    public static Map<Integer, OBPhase> getPhaseCache() {
        return phaseCache;
    }


    public static int getHighestPhaseId() {
        return highestPhaseId;
    }
}
