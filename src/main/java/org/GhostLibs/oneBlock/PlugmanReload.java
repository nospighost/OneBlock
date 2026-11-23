package org.GhostLibs.oneBlock;

import com.github.t9t.minecraftrconclient.RconClient;

public class PlugmanReload {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 25575;
        String password = "WC32sdfsdfhJLK";

        try (RconClient client = RconClient.open(host, port, password)) {
            String response = client.sendCommand("plugman unload One-Block");
            System.out.println("Serverantwort: " + response);
            String response2 = client.sendCommand("plugman load One-Block");
            String seperater2 = client.sendCommand("bc ---------------");
            String brodcast = client.sendCommand("bc Plugin One-Block wurde neugeladen!");
            String seperater = client.sendCommand("bc ---------------");
          System.out.println("Serverantwort: " + response2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
