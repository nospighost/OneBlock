package de.Main.OneBlock.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {

    private Connection connection;
    private final String[] informations = new String[5];

    public SQLConnection(String host, int port, String database, String user, String password) {
        informations[0] = host;//Datenbank Informationen
        informations[1] = port + "";//Datenbank Informationen
        informations[2] = database;//Datenbank Informationen
        informations[3] = user;//Datenbank Informationen
        informations[4] = password;//Datenbank Informationen
        conncet(); //Verbindung aufbauen

    }


    public void conncet() { //SQL connecten
        if (!isConnected()) {
            try {
                connection = DriverManager.getConnection("jdbc:mysql://" + informations[0] + ":" + informations[1] + "/" + informations[2], informations[3], informations[4]); //Informationen übergeben
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isConnected() {
        return this.connection != null; //Gucken, ob es bereits eine Verbindung gibt
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
                connection = null; //Connection auf null setzen damit es wieder connecten kann
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Connection getConnection(){
        return this.connection;
    }
}
