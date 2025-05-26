package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Utilitaire pour recréer la base de données avec des utilisateurs valides.
 */
public class DatabaseRecreator {
    
    private static final String DB_URL = "jdbc:sqlite:database.db";
    
    /**
     * Point d'entrée pour recréer la base de données.
     */
    public static void main(String[] args) {
        try {
            // Charger le pilote SQLite
            Class.forName("org.sqlite.JDBC");
            
            // Recréer la base de données
            recreateDatabase();
            
            System.out.println("Base de données recréée avec succès!");
            System.out.println();
            System.out.println("Utilisateurs créés:");
            System.out.println(" - Admin: admin@missie.com / StrongP@ss143");
            System.out.println(" - Utilisateur: user@missie.com / StrongP@ss143");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la recréation de la base de données: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Recréer la base de données avec les utilisateurs valides.
     */
    private static void recreateDatabase() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Supprimer les tables existantes
            stmt.executeUpdate("DROP TABLE IF EXISTS audio_recordings");
            stmt.executeUpdate("DROP TABLE IF EXISTS recordings");
            stmt.executeUpdate("DROP TABLE IF EXISTS users");
            
            // Créer la table des utilisateurs
            stmt.executeUpdate(
                "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT NOT NULL UNIQUE," +
                "password_hash TEXT NOT NULL," +
                "salt TEXT NOT NULL," +
                "is_admin INTEGER NOT NULL DEFAULT 0" +
                ");"
            );
            
            // Créer la table des enregistrements audio
            stmt.executeUpdate(
                "CREATE TABLE recordings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "timestamp TEXT NOT NULL," +
                "file_path TEXT," +
                "duration INTEGER NOT NULL," +
                "audio_data BLOB," +
                "encryption_key TEXT," +
                "integrity_hash TEXT," +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ");"
            );
            
            // Créer les utilisateurs avec des hachages nouveaux
            String adminSalt = "abc123def456";
            String userSalt = "xyz789uvw567";
            
            String adminHash = hashPassword("StrongP@ss143", adminSalt);
            String userHash = hashPassword("StrongP@ss143", userSalt);
            
            // Créer un utilisateur administrateur
            stmt.executeUpdate(
                "INSERT INTO users (email, password_hash, salt, is_admin) VALUES " +
                "('admin@missie.com', '" + adminHash + "', '" + adminSalt + "', 1);"
            );
            
            // Créer un utilisateur standard
            stmt.executeUpdate(
                "INSERT INTO users (email, password_hash, salt, is_admin) VALUES " +
                "('user@missie.com', '" + userHash + "', '" + userSalt + "', 0);"
            );
            
            // Afficher les hachages générés
            System.out.println("Hachages générés pour l'authentification:");
            System.out.println("Admin (admin@missie.com):");
            System.out.println("- Sel: " + adminSalt);
            System.out.println("- Hash: " + adminHash);
            System.out.println();
            System.out.println("Utilisateur (user@missie.com):");
            System.out.println("- Sel: " + userSalt);
            System.out.println("- Hash: " + userHash);
        }
    }
    
    /**
     * Calcule le hachage d'un mot de passe avec un sel.
     */
    private static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        String passwordWithSalt = password + salt;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(passwordWithSalt.getBytes());
        
        // Conversion en chaîne hexadécimale
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }
} 