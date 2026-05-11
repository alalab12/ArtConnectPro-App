package com.project.artconnect.util;

import com.project.artconnect.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {

    static {
        try {
            Class.forName(DatabaseConfig.DRIVER);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Pilote MySQL introuvable. Vérifiez la dépendance mysql-connector-j dans pom.xml.\n" + e.getMessage());
        }
    }

    private ConnectionManager() {}


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD);
    }


    public static void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) {}
        }
    }


    public static void close(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
