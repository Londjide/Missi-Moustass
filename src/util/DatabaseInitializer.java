package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Classe utilitaire pour initialiser la base de données.
 */
public class DatabaseInitializer {
    
    private static final String DB_URL = "jdbc:sqlite:database.db";
    
    /**
     * Point d'entrée pour l'initialisation de la base de données.
     */
    public static void main(String[] args) {
        try {
            // Charger le pilote SQLite
            Class.forName("org.sqlite.JDBC");
            
            // Initialiser la base de données
            initializeDatabase();
            
            System.out.println("Base de données initialisée avec succès!");
            System.out.println();
            System.out.println("Utilisateurs par défaut créés:");
            System.out.println(" - Admin: admin@missie.com / StrongP@ss143");
            System.out.println(" - Utilisateur: user@missie.com / StrongP@ss143");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Initialise la base de données avec le schéma et les données par défaut.
     */
    private static void initializeDatabase() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Créer la table des utilisateurs
            stmt.executeUpdate(
                "CREATE TABLE users (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    email TEXT NOT NULL UNIQUE," +
                "    password_hash TEXT NOT NULL," +
                "    salt TEXT NOT NULL," +
                "    is_admin INTEGER NOT NULL DEFAULT 0" +
                ");"
            );
            
            // Créer la table des enregistrements audio
            stmt.executeUpdate(
                "CREATE TABLE audio_recordings (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    user_id INTEGER NOT NULL," +
                "    title TEXT NOT NULL," +
                "    recorded_at TIMESTAMP NOT NULL," +
                "    file_path TEXT NOT NULL," +
                "    duration INTEGER NOT NULL," +
                "    encrypted INTEGER NOT NULL DEFAULT 1," +
                "    FOREIGN KEY (user_id) REFERENCES users(id)" +
                ");"
            );
            
            // Créer un utilisateur administrateur par défaut
            stmt.executeUpdate(
                "INSERT INTO users (email, password_hash, salt, is_admin) VALUES " +
                "('admin@missie.com', '5a1e6d7748674685e92f293095f69a5c5740dd4c9e1cb8a57e3b9d555b616631', 'hS7pQ9tR3', 1);"
            );
            
            // Créer un utilisateur standard par défaut
            stmt.executeUpdate(
                "INSERT INTO users (email, password_hash, salt, is_admin) VALUES " +
                "('user@missie.com', '8a1e5d7748674685e92f293095f69a5c5740dd4c9e1cb8a57e3b9d555b616521', 'jD2kL5mN6', 0);"
            );
        }
    }
} 