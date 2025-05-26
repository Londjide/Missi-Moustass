package service;

import model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Implémentation du service de gestion des utilisateurs.
 * Cette classe suit le principe de responsabilité unique (SRP) en gérant
 * uniquement les fonctionnalités liées aux utilisateurs.
 */
public class UserServiceImpl implements UserService {
    
    private final DatabaseService databaseService;
    
    /**
     * Constructeur qui prend en paramètre le service de base de données.
     * Cette approche suit le principe d'inversion de dépendance (DIP) en
     * dépendant des abstractions plutôt que des implémentations concrètes.
     * 
     * @param databaseService Le service de base de données
     */
    public UserServiceImpl(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }
    
    @Override
    public int authenticateUser(String email, String password) throws Exception {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = databaseService.connect();
            pstmt = conn.prepareStatement("SELECT id, password_hash, salt FROM users WHERE email = ?");
            pstmt.setString(1, email);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("id");
                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                
                // Calcul du hash du mot de passe fourni avec le sel stocké
                String calculatedHash = hashPassword(password, salt);
                
                // Comparaison des hachages
                if (storedHash.equals(calculatedHash)) {
                    return userId; // Authentification réussie
                }
            }
            
            return -1; // Échec de l'authentification
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
            databaseService.closeConnection(conn);
        }
    }
    
    @Override
    public int createUser(String email, String password, boolean isAdmin) throws Exception {
        // Génération d'un sel aléatoire
        String salt = generateSalt();
        
        // Hachage du mot de passe avec le sel
        String passwordHash = hashPassword(password, salt);
        
        String sql = "INSERT INTO users (email, password_hash, salt, is_admin) VALUES (?, ?, ?, ?)";
        return databaseService.executeUpdate(sql, email, passwordHash, salt, isAdmin ? 1 : 0);
    }
    
    @Override
    public User getUserById(int userId) throws Exception {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = databaseService.connect();
            pstmt = conn.prepareStatement("SELECT id, email, password_hash, salt, is_admin FROM users WHERE id = ?");
            pstmt.setInt(1, userId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
            
            return null;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
            databaseService.closeConnection(conn);
        }
    }
    
    @Override
    public User getUserByEmail(String email) throws Exception {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = databaseService.connect();
            pstmt = conn.prepareStatement("SELECT id, email, password_hash, salt, is_admin FROM users WHERE email = ?");
            pstmt.setString(1, email);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
            
            return null;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
            databaseService.closeConnection(conn);
        }
    }
    
    @Override
    public boolean isAdmin(int userId) throws Exception {
        User user = getUserById(userId);
        return user != null && user.isAdmin();
    }
    
    @Override
    public boolean resetPassword(int userId, String newPassword) throws Exception {
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }
        
        // Génération d'un nouveau sel
        String newSalt = generateSalt();
        
        // Hachage du nouveau mot de passe
        String newPasswordHash = hashPassword(newPassword, newSalt);
        
        String sql = "UPDATE users SET password_hash = ?, salt = ? WHERE id = ?";
        int rowsAffected = databaseService.executeUpdate(sql, newPasswordHash, newSalt, userId);
        
        return rowsAffected > 0;
    }
    
    /**
     * Crée un objet User à partir d'un ResultSet.
     * 
     * @param rs Le ResultSet contenant les données de l'utilisateur
     * @return Un objet User
     * @throws SQLException Si une erreur survient lors de la lecture du ResultSet
     */
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String salt = rs.getString("salt");
        boolean isAdmin = rs.getInt("is_admin") == 1;
        
        return new User(id, email, passwordHash, isAdmin, salt);
    }
    
    /**
     * Génère un sel aléatoire pour le hachage des mots de passe.
     * 
     * @return Un sel sous forme de chaîne Base64
     */
    private String generateSalt() {
        byte[] salt = new byte[16];
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.nextBytes(salt);
            return Base64.getEncoder().encodeToString(salt);
        } catch (NoSuchAlgorithmException e) {
            // Fallback si l'algorithme n'est pas disponible
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);
            return Base64.getEncoder().encodeToString(salt);
        }
    }
    
    /**
     * Calcule le hachage d'un mot de passe avec un sel.
     * 
     * @param password Le mot de passe en clair
     * @param salt Le sel à utiliser
     * @return Le hachage du mot de passe
     * @throws NoSuchAlgorithmException Si l'algorithme de hachage n'est pas disponible
     */
    private String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
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
    
    /**
     * Récupère tous les utilisateurs de la base de données.
     * 
     * @return Une liste de tous les utilisateurs
     * @throws Exception Si une erreur survient lors de la récupération
     */
    public List<User> getAllUsers() throws Exception {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
            conn = databaseService.connect();
            pstmt = conn.prepareStatement("SELECT id, email, password_hash, salt, is_admin FROM users ORDER BY id");
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
            
            return users;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignore */ }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
            databaseService.closeConnection(conn);
        }
    }
    
    @Override
    public void deleteUser(int userId) throws Exception {
        String sql = "DELETE FROM users WHERE id = ?";
        databaseService.executeUpdate(sql, userId);
    }
    
    @Override
    public void updateUser(User user) throws Exception {
        String sql = "UPDATE users SET email = ?, is_admin = ? WHERE id = ?";
        databaseService.executeUpdate(sql, user.getEmail(), user.isAdmin() ? 1 : 0, user.getId());
    }
} 