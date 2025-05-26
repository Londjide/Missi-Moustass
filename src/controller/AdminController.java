package controller;

import model.User;
import service.UserService;
import util.LogManager;
import util.ServiceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur pour gérer les opérations d'administration.
 * Cette classe suit le principe de responsabilité unique (SRP) en gérant uniquement
 * la logique de contrôle pour les opérations d'administration.
 */
public class AdminController {
    
    private static final String TAG = "AdminController";
    private final UserService userService;
    private final int adminId;
    
    /**
     * Constructeur qui initialise le contrôleur avec un ID d'administrateur spécifique.
     * 
     * @param adminId L'ID de l'administrateur connecté
     * @throws Exception Si l'utilisateur n'est pas un administrateur
     */
    public AdminController(int adminId) throws Exception {
        this.adminId = adminId;
        this.userService = ServiceFactory.getInstance().getUserService();
        
        // Vérifier que l'utilisateur est un administrateur
        if (!userService.isAdmin(adminId)) {
            LogManager.warning(TAG, "Tentative d'accès administrateur par un utilisateur non admin: " + adminId);
            throw new SecurityException("Vous n'avez pas les droits d'administration");
        }
        
        LogManager.info(TAG, "Contrôleur administrateur initialisé pour l'utilisateur " + adminId);
    }
    
    /**
     * Récupère l'ID de l'administrateur connecté.
     * 
     * @return L'ID de l'administrateur
     */
    public int getAdminId() {
        return adminId;
    }
    
    /**
     * Récupère la liste de tous les utilisateurs.
     * 
     * @return La liste des utilisateurs
     * @throws Exception Si une erreur survient lors de la récupération
     */
    public List<User> getAllUsers() throws Exception {
        try {
            LogManager.debug(TAG, "Récupération des utilisateurs");
            return userService.getAllUsers();
        } catch (Exception e) {
            LogManager.error(TAG, "Erreur lors de la récupération des utilisateurs", e);
            throw e;
        }
    }
    
    /**
     * Crée un nouvel utilisateur.
     * 
     * @param email L'email du nouvel utilisateur
     * @param password Le mot de passe en clair
     * @param isAdmin Indique si l'utilisateur est un administrateur
     * @return L'ID du nouvel utilisateur
     * @throws Exception Si une erreur survient lors de la création
     */
    public int createUser(String email, String password, boolean isAdmin) throws Exception {
        LogManager.info(TAG, "Création d'un nouvel utilisateur: " + email + " (admin: " + isAdmin + ")");
        return userService.createUser(email, password, isAdmin);
    }
    
    /**
     * Supprime un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur à supprimer
     * @return true si l'utilisateur a été supprimé, false sinon
     * @throws Exception Si une erreur survient lors de la suppression
     */
    public boolean deleteUser(int userId) throws Exception {
        // Vérifier que l'utilisateur n'est pas l'administrateur connecté
        if (userId == adminId) {
            LogManager.warning(TAG, "Tentative de suppression de l'administrateur connecté");
            return false;
        }
        
        // Vérifier que l'utilisateur existe
        User user = userService.getUserById(userId);
        if (user == null) {
            LogManager.warning(TAG, "Tentative de suppression d'un utilisateur inexistant: " + userId);
            return false;
        }
        
        // Supprimer l'utilisateur
        String sql = "DELETE FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ServiceFactory.getInstance().getDatabaseService().connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                LogManager.info(TAG, "Utilisateur " + userId + " supprimé avec succès");
                return true;
            } else {
                LogManager.warning(TAG, "Échec de la suppression de l'utilisateur " + userId);
                return false;
            }
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
    
    /**
     * Met à jour les informations d'un utilisateur.
     * 
     * @param user L'utilisateur à mettre à jour
     * @return true si l'utilisateur a été mis à jour, false sinon
     * @throws Exception Si une erreur survient lors de la mise à jour
     */
    public boolean updateUser(User user) throws Exception {
        // Cette méthode devrait être implémentée dans le UserService
        // Pour l'instant, nous retournons false
        LogManager.warning(TAG, "Tentative de mise à jour de l'utilisateur " + user.getId() + " (non implémenté)");
        return false;
    }
    
    /**
     * Réinitialise le mot de passe d'un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur
     * @param newPassword Le nouveau mot de passe en clair
     * @return true si la réinitialisation a réussi, false sinon
     * @throws Exception Si une erreur survient lors de la réinitialisation
     */
    public boolean resetUserPassword(int userId, String newPassword) throws Exception {
        LogManager.info(TAG, "Réinitialisation du mot de passe pour l'utilisateur " + userId);
        return userService.resetPassword(userId, newPassword);
    }
} 