package org.GhostLibs.oneBlock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Übersetzer {

    private static OneBlock plugin;
    private static String currentLanguage;
    private static final Map<String, String> translations = new HashMap<>();

    public static void init(OneBlock oneBlock, String sprache) {
        plugin = oneBlock;
        loadLanguage(sprache);
    }

    public static void loadLanguage(String language) {
        if (plugin == null) {
            throw new IllegalStateException("TranslateManager wurde noch nicht mit init() initialisiert!");
        }

        currentLanguage = language;
        translations.clear();

        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File langFile = new File(langFolder, language + ".json");

        if (!langFile.exists()) {
            if (plugin.getResource("lang/" + language + ".json") != null) {
                plugin.saveResource("lang/" + language + ".json", false);
            } else {
                plugin.getLogger().warning("Sprachdatei lang/" + language + ".json wurde nicht gefunden!");
                return;
            }
        }

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(langFile), StandardCharsets.UTF_8)) {
            JsonElement rootElement = JsonParser.parseReader(reader);
            if (rootElement != null && rootElement.isJsonObject()) {
                flattenJson("", rootElement.getAsJsonObject());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Laden der Sprachdatei " + language + ".json: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void flattenJson(String prefix, JsonObject jsonObject) {
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonObject()) {
                flattenJson(key, value.getAsJsonObject());
            } else if (value.isJsonPrimitive()) {
                translations.put(key, value.getAsString());
            }
        }
    }

    public static String get(String key) {
        String message = translations.getOrDefault(key, "§cMissing key: " + key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String get(String key, String... placeholders) {
        String message = get(key);
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        return message;
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }
}
