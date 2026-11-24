package org.GhostLibs.oneBlock.util;

import org.GhostLibs.oneBlock.OneBlock;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ErrorLogger {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static void log(String contextMessage, Throwable throwable) {
        OneBlock plugin = OneBlock.getInstance();
        if (plugin == null) return;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File logFile = new File(plugin.getDataFolder(), "errors.log");

        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {

            String time = dateFormat.format(new Date());
            pw.println("[" + time + "] ERROR: " + contextMessage);

            if (throwable != null) {
                throwable.printStackTrace(pw);
            }
            pw.println("------------------------------------------------------------------------");

        } catch (IOException e) {
            plugin.getLogger().severe("Konnte Fehler nicht in die errors.log schreiben: " + e.getMessage());
        }
    }


    public static void log(String contextMessage) {
        log(contextMessage, null);
    }
}
