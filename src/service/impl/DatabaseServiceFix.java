package service.impl;

import service.DatabaseService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class DatabaseServiceFix implements DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:database.db";
    
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
                // Ignorer l'erreur de fermeture
            }
        }
    }
    
    @Override
    public int executeUpdate(String query, Object... params) throws SQLException {
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            return stmt.executeUpdate();
        }
    }
    
    @Override
    public void initializeDatabase() throws SQLException {
        try (Connection conn = connect()) {
            // Création de la table users si elle n'existe pas
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "email TEXT UNIQUE NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "salt TEXT NOT NULL," +
                "is_admin INTEGER NOT NULL DEFAULT 0" +
                ")"
            );
            
            // Création de la table recordings si elle n'existe pas
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS recordings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "timestamp TEXT NOT NULL," +
                "duration INTEGER," +
                "audio BLOB NOT NULL," +
                "encryption_key TEXT NOT NULL," +
                "audio_hash TEXT NOT NULL," +
                "user_id INTEGER NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")"
            );
        }
    }
} 