package service.impl;

import service.DatabaseService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLiteDatabaseService implements DatabaseService {
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
    public int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = connect();
            pstmt = conn.prepareStatement(sql);
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            return pstmt.executeUpdate();
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
            closeConnection(conn);
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
        }
    }
} 