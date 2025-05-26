package controller;

import model.User;
import service.UserService;
import service.impl.UserServiceImplFix;
import util.LogManager;
import util.ServiceFactory;

import java.util.List;

/**
 * Version corrigée du contrôleur pour gérer les opérations d'administration.
 * Cette classe utilise explicitement l'implémentation corrigée du service utilisateur.
 */
public class AdminControllerFix {
    
    private static final String TAG = "AdminControllerFix";
    private final UserService userService;
    private final int adminId;
    
    /**
     * Constructeur qui initialise le contrôleur avec un ID d'administrateur spécifique.
     * 
     * @param adminId L'ID de l'administrateur connecté
     * @throws Exception Si l'utilisateur n'est pas un administrateur
     */
    public AdminControllerFix(int adminId) throws Exception {
        this.adminId = adminId;
        // Utilisation explicite de l'implémentation corrigée
        this.userService = new UserServiceImplFix(ServiceFactory.getInstance().getDatabaseService(), ServiceFactory.getInstance().getCryptographyService());
        
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
     * Cette méthode utilise l'implémentation corrigée qui récupère correctement tous les utilisateurs.
     * 
     * @return La liste des utilisateurs
     */
    public List<User> getAllUsers() {
        try {
            LogManager.debug(TAG, "Récupération des utilisateurs avec l'implémentation corrigée");
            return userService.getAllUsers();
        } catch (Exception e) {
            LogManager.error(TAG, "Erreur lors de la récupération des utilisateurs", e);
            return List.of(); // Retourner une liste vide en cas d'erreur
        }
    }
    
    /**
     * Crée un nouvel utilisateur.
     * 
     * @param email L'email du nouvel utilisateur
     * @param password Le mot de passe en clair
     * @param isAdmin Indique si l'utilisateur est un administrateur
     * @return L'ID du nouvel utilisateur
     */
    public int createUser(String email, String password, boolean isAdmin) {
        LogManager.info(TAG, "Création d'un nouvel utilisateur: " + email + " (admin: " + isAdmin + ")");
        try {
            return userService.createUser(email, password, isAdmin);
        } catch (Exception e) {
            LogManager.error(TAG, "Erreur lors de la création de l'utilisateur", e);
            return -1;
        }
    }
    
    /**
     * Réinitialise le mot de passe d'un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur
     * @param newPassword Le nouveau mot de passe en clair
     * @return true si la réinitialisation a réussi, false sinon
     */
    public boolean resetUserPassword(int userId, String newPassword) {
        LogManager.info(TAG, "Réinitialisation du mot de passe pour l'utilisateur " + userId);
        try {
            return userService.resetPassword(userId, newPassword);
        } catch (Exception e) {
            LogManager.error(TAG, "Erreur lors de la réinitialisation du mot de passe", e);
            return false;
        }
    }
} 