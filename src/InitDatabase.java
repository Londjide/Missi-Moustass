import java.sql.*;
import java.io.*;

/**
 * Classe utilitaire pour initialiser la base de données SQLite
 */
public class InitDatabase {

    public static void main(String[] args) {
        Connection conn = null;
        
        try {
            // Charger le driver JDBC SQLite
            Class.forName("org.sqlite.JDBC");
            
            // Se connecter à la base de données (la crée si elle n'existe pas)
            conn = DriverManager.getConnection("jdbc:sqlite:users.db");
            System.out.println("Base de données connectée.");
            
            // Créer les tables
            createTables(conn);
            
            // Insérer les utilisateurs par défaut
            insertDefaultUsers(conn);
            
            System.out.println("Base de données initialisée avec succès !");
            System.out.println("Utilisateurs créés :");
            System.out.println("- admin@missie.com / StrongP@ss143 (admin)");
            System.out.println("- user@missie.com / StrongP@ss143 (user)");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
    
    /**
     * Crée les tables nécessaires dans la base de données
     */
    private static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Table des utilisateurs
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS users (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    email TEXT UNIQUE NOT NULL," +
            "    password TEXT NOT NULL," +
            "    role TEXT DEFAULT 'user'," +
            "    is_admin BOOLEAN DEFAULT 0" +
            ")"
        );
        
        // Table des enregistrements audio
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS recordings (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    name TEXT NOT NULL," +
            "    timestamp TEXT NOT NULL," +
            "    duration INTEGER," +
            "    audio BLOB," +
            "    encryption_key TEXT," +
            "    audio_hash TEXT," +
            "    user_id INTEGER," +
            "    FOREIGN KEY(user_id) REFERENCES users(id)" +
            ")"
        );
        
        stmt.close();
        System.out.println("Tables créées avec succès.");
    }
    
    /**
     * Insère les utilisateurs par défaut dans la base de données
     */
    private static void insertDefaultUsers(Connection conn) throws SQLException {
        // Vérifier si les utilisateurs existent déjà
        PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
        ResultSet rs = checkStmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        rs.close();
        checkStmt.close();
        
        // Si des utilisateurs existent déjà, ne pas en ajouter de nouveaux
        if (count > 0) {
            System.out.println("Des utilisateurs existent déjà. Aucun utilisateur ajouté.");
            return;
        }
        
        // Insérer l'utilisateur admin
        PreparedStatement adminStmt = conn.prepareStatement(
            "INSERT INTO users (email, password, role, is_admin) VALUES (?, ?, ?, ?)"
        );
        adminStmt.setString(1, "admin@missie.com");
        adminStmt.setString(2, "StrongP@ss143");
        adminStmt.setString(3, "admin");
        adminStmt.setBoolean(4, true);
        adminStmt.executeUpdate();
        adminStmt.close();
        
        // Insérer l'utilisateur standard
        PreparedStatement userStmt = conn.prepareStatement(
            "INSERT INTO users (email, password, role, is_admin) VALUES (?, ?, ?, ?)"
        );
        userStmt.setString(1, "user@missie.com");
        userStmt.setString(2, "StrongP@ss143");
        userStmt.setString(3, "user");
        userStmt.setBoolean(4, false);
        userStmt.executeUpdate();
        userStmt.close();
        
        System.out.println("Utilisateurs par défaut ajoutés.");
    }
} 