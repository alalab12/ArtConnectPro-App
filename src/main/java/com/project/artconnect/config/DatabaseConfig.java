package com.project.artconnect.config;

/**
 * Configuration de la base de données ArtConnect.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 *  PARAMÈTRES À MODIFIER avant le premier lancement :
 *    URL      → URL JDBC de votre instance MySQL (host, port, nom de la base)
 *    USER     → nom d'utilisateur MySQL
 *    PASSWORD → mot de passe MySQL
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *  Exemple pour une installation locale par défaut :
 *    URL  = "jdbc:mysql://localhost:3306/artconnect_db?useSSL=false&serverTimezone=UTC"
 *    USER = "root"
 *    PASS = "monMotDePasse"
 */
public final class DatabaseConfig {

    // ── Connexion ──────────────────────────────────────────────────────────────
    public static final String URL =
            "jdbc:mysql://localhost:3306/artconnect_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    public static final String USER     = "root";
    public static final String PASSWORD = "password"; // ← CHANGER ICI

    // ── Driver (chargé automatiquement via mysql-connector-j) ─────────────────
    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private DatabaseConfig() {}
}
