package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * Implémentation du service de base de données pour SQLite.
 * Cette classe suit le principe de ségrégation d'interface (ISP) en implémentant
 * uniquement les méthodes nécessaires pour le fonctionnement avec SQLite.
 */
public class SQLiteDatabaseService implements DatabaseService {
    
    private static final String DB_URL = "jdbc:sqlite:database.db";
    
    /**
     * Constructeur qui initialise le driver JDBC pour SQLite.
     * 
     * @throws RuntimeException Si le driver ne peut pas être chargé
     */
    public SQLiteDatabaseService() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver SQLite non trouvé", e);
        }
    }
    
    @Override
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    @Override
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }
    
    @Override
    public int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = connect();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // Définir les paramètres
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            int result = pstmt.executeUpdate();
            
            // Si c'est une insertion, on récupère l'ID généré
            if (sql.trim().toUpperCase().startsWith("INSERT")) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // Retourne l'ID généré
                }
            }
            
            return result;
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du statement: " + e.getMessage());
                }
            }
            closeConnection(conn);
        }
    }
    
    @Override
    public void initializeDatabase() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = connect();
            stmt = conn.createStatement();
            
            // Vérifier si la base de données a déjà été initialisée
            ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users'");
            boolean tableExists = rs.next();
            rs.close();
            
            if (!tableExists) {
                // Créer la table des utilisateurs si elle n'existe pas
                stmt.execute(
                    "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT NOT NULL UNIQUE," +
                    "password_hash TEXT NOT NULL," +
                    "salt TEXT NOT NULL," +
                    "is_admin INTEGER NOT NULL DEFAULT 0" +
                    ");"
                );
                
                // Créer la table des enregistrements audio si elle n'existe pas
                stmt.execute(
                    "CREATE TABLE audio_recordings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "title TEXT NOT NULL," +
                    "recorded_at TIMESTAMP NOT NULL," +
                    "file_path TEXT NOT NULL," +
                    "duration INTEGER NOT NULL," +
                    "encrypted INTEGER NOT NULL DEFAULT 1," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ");"
                );
                
                // Créer un utilisateur administrateur par défaut
                stmt.execute(
                    "INSERT INTO users (email, password_hash, salt, is_admin) VALUES " +
                    "('admin@missie.com', '5a1e6d7748674685e92f293095f69a5c5740dd4c9e1cb8a57e3b9d555b616631', 'hS7pQ9tR3', 1);"
                );
                
                // Créer un utilisateur standard par défaut
                stmt.execute(
                    "INSERT INTO users (email, password_hash, salt, is_admin) VALUES " +
                    "('user@missie.com', '8a1e5d7748674685e92f293095f69a5c5740dd4c9e1cb8a57e3b9d555b616521', 'jD2kL5mN6', 0);"
                );
                
                System.out.println("Base de données initialisée avec succès");
            } else {
                System.out.println("Base de données déjà initialisée");
            }
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du statement: " + e.getMessage());
                }
            }
            closeConnection(conn);
        }
    }
} 