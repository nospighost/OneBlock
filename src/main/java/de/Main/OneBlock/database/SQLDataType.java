package de.Main.OneBlock.database;

public enum SQLDataType {
    CHAR(255), //Datentypen
    BOOLEAN,
    TEXT(255),//Datentypen
    INT(255); //Datentypen
    private final long size;

    SQLDataType() {
        this.size = -1;
    }

    SQLDataType(int size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public String toSQL() {
        if (size > 0)
            return this.name().toUpperCase() + "(" + this.size + ")";
        return this.name().toUpperCase();


    }
}
