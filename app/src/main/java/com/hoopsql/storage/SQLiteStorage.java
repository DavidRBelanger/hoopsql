package com.hoopsql.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteStorage {
    private static final String DB_URL = "jdbc:sqlite:/home/david-belanger/dev/HoopsQL/SQLite/hoopsql.db";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public boolean testConnection() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='games';")) {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
