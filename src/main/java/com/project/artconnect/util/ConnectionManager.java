package com.project.artconnect.util;

import com.project.artconnect.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fournit des connexions JDBC vers la base ArtConnect.
 *
 * <p>Utilisation standard dans un DAO :</p>
 * <pre>{@code
 * try (Connection conn = ConnectionManager.getConnection();
 *      PreparedStatement ps = conn.prepareStatement("SELECT ...")) {
 *     // ...
 * }
 * }</pre>
 *
 * <p>Utilisation avec transaction :</p>
 * <pre>{@code
 * Connection conn = ConnectionManager.getConnection();
 * try {
 *     conn.setAutoCommit(false);
 *     // opérations...
 *     conn.commit();
 * } catch (SQLException e) {
 *     ConnectionManager.rollback(conn);
 *     throw e;
 * } finally {
 *     ConnectionManager.close(conn);
 * }
 * }</pre>
 */
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

    /**
     * Retourne une nouvelle connexion JDBC.
     *
     * @return connexion active, à fermer par l'appelant
     * @throws SQLException si la connexion échoue
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.URL,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD);
    }

    /** Effectue un rollback silencieux (ignore {@link SQLException}). */
    public static void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ignored) {}
        }
    }

    /** Ferme une connexion silencieusement. */
    public static void close(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
