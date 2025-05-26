package service;

import model.UserKeys;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les clés de chiffrement des utilisateurs.
 * Ce service permet de stocker et récupérer les paires de clés RSA des utilisateurs.
 */
public class UserKeysService {
    
    private final DatabaseService databaseService;
    private final RSACryptographyService rsaService;
    
    /**
     * Constructeur qui initialise le service avec les dépendances nécessaires.
     * 
     * @param databaseService Le service de base de données
     * @param rsaService Le service de cryptographie RSA
     */
    public UserKeysService(DatabaseService databaseService, RSACryptographyService rsaService) {
        this.databaseService = databaseService;
        this.rsaService = rsaService;
        
        // Création de la table si elle n'existe pas
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Crée ou récupère les clés pour un utilisateur.
     * Si l'utilisateur n'a pas de clés, une nouvelle paire est générée.
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return Les clés de l'utilisateur
     * @throws Exception Si une erreur survient lors de la génération ou récupération des clés
     */
    public UserKeys getUserKeys(int userId) throws Exception {
        // Vérifier si l'utilisateur a déjà des clés
        UserKeys existingKeys = getKeysForUser(userId);
        if (existingKeys != null) {
            return existingKeys;
        }
        
        // Générer une nouvelle paire de clés
        String[] keyPair = rsaService.generateKeyPair();
        String publicKey = keyPair[0];
        String privateKey = keyPair[1];
        
        // Sauvegarder les clés
        saveUserKeys(userId, publicKey, privateKey);
        
        return new UserKeys(userId, publicKey, privateKey);
    }
    
    /**
     * Récupère la clé publique d'un utilisateur.
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return La clé publique de l'utilisateur, ou null si non trouvée
     * @throws SQLException Si une erreur SQL survient
     */
    public String getUserPublicKey(int userId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = databaseService.connect();
            String query = "SELECT public_key FROM user_keys WHERE user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("public_key");
            }
            
            return null;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Récupère les clés de tous les utilisateurs (clés publiques uniquement).
     * 
     * @return Une liste de UserKeys contenant les clés publiques de tous les utilisateurs
     * @throws SQLException Si une erreur SQL survient
     */
    public List<UserKeys> getAllPublicKeys() throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<UserKeys> publicKeys = new ArrayList<>();
        
        try {
            conn = databaseService.connect();
            String query = "SELECT user_id, public_key FROM user_keys";
            stmt = conn.prepareStatement(query);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String publicKey = rs.getString("public_key");
                publicKeys.add(new UserKeys(userId, publicKey));
            }
            
            return publicKeys;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Récupère les clés d'un utilisateur spécifique.
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return Les clés de l'utilisateur, ou null si non trouvées
     * @throws SQLException Si une erreur SQL survient
     */
    private UserKeys getKeysForUser(int userId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = databaseService.connect();
            String query = "SELECT user_id, public_key, private_key FROM user_keys WHERE user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new UserKeys(
                    rs.getInt("user_id"),
                    rs.getString("public_key"),
                    rs.getString("private_key")
                );
            }
            
            return null;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Sauvegarde les clés d'un utilisateur.
     * 
     * @param userId L'identifiant de l'utilisateur
     * @param publicKey La clé publique
     * @param privateKey La clé privée
     * @throws SQLException Si une erreur SQL survient
     */
    private void saveUserKeys(int userId, String publicKey, String privateKey) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = databaseService.connect();
            String query = "INSERT INTO user_keys (user_id, public_key, private_key) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, publicKey);
            stmt.setString(3, privateKey);
            
            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Crée la table des clés utilisateur si elle n'existe pas.
     * 
     * @throws SQLException Si une erreur SQL survient
     */
    private void createTableIfNotExists() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = databaseService.connect();
            stmt = conn.createStatement();
            
            String createTableQuery = "CREATE TABLE IF NOT EXISTS user_keys (" +
                "user_id INTEGER PRIMARY KEY, " +
                "public_key TEXT NOT NULL, " +
                "private_key TEXT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            
            stmt.executeUpdate(createTableQuery);
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    }
} 