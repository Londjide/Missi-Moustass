package controller;

import model.User;
import service.UserService;
import util.ServiceFactory;

/**
 * Contrôleur pour gérer les opérations d'authentification et de gestion des utilisateurs.
 * Cette classe suit le principe de responsabilité unique (SRP) en gérant uniquement
 * la logique de contrôle pour l'authentification et les utilisateurs.
 */
public class AuthController {
    
    private final UserService userService;
    
    /**
     * Constructeur qui initialise le contrôleur avec les services nécessaires.
     */
    public AuthController() {
        this.userService = ServiceFactory.getInstance().getUserService();
    }
    
    /**
     * Authentifie un utilisateur.
     * 
     * @param email L'email de l'utilisateur
     * @param password Le mot de passe en clair
     * @return L'ID de l'utilisateur si l'authentification réussit, -1 sinon
     * @throws Exception Si une erreur survient lors de l'authentification
     */
    public int login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            return -1;
        }
        
        return userService.authenticateUser(email, password);
    }
    
    /**
     * Vérifie si un email est valide (format simple).
     * 
     * @param email L'email à vérifier
     * @return true si l'email est valide, false sinon
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Vérification basique du format email (peut être améliorée)
        return email.contains("@") && email.contains(".");
    }
    
    /**
     * Vérifie si un mot de passe est suffisamment fort.
     * 
     * @param password Le mot de passe à vérifier
     * @return true si le mot de passe est fort, false sinon
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Vérification basique de la force du mot de passe (peut être améliorée)
        boolean hasLowerCase = false;
        boolean hasUpperCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecialChar = true;
            }
        }
        
        return hasLowerCase && hasUpperCase && hasDigit && hasSpecialChar;
    }
    
    /**
     * Enregistre un nouvel utilisateur.
     * 
     * @param email L'email du nouvel utilisateur
     * @param password Le mot de passe en clair
     * @param isAdmin Indique si l'utilisateur est un administrateur
     * @return L'ID du nouvel utilisateur si l'enregistrement réussit, -1 sinon
     * @throws Exception Si une erreur survient lors de l'enregistrement
     */
    public int register(String email, String password, boolean isAdmin) throws Exception {
        if (!isValidEmail(email) || !isStrongPassword(password)) {
            return -1;
        }
        
        // Vérifier si l'email existe déjà
        User existingUser = userService.getUserByEmail(email);
        if (existingUser != null) {
            return -1;
        }
        
        return userService.createUser(email, password, isAdmin);
    }
    
    /**
     * Vérifie si un utilisateur est administrateur.
     * 
     * @param userId L'ID de l'utilisateur à vérifier
     * @return true si l'utilisateur est administrateur, false sinon
     * @throws Exception Si une erreur survient lors de la vérification
     */
    public boolean isAdmin(int userId) throws Exception {
        return userService.isAdmin(userId);
    }
    
    /**
     * Réinitialise le mot de passe d'un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur
     * @param newPassword Le nouveau mot de passe en clair
     * @return true si la réinitialisation a réussi, false sinon
     * @throws Exception Si une erreur survient lors de la réinitialisation
     */
    public boolean resetPassword(int userId, String newPassword) throws Exception {
        if (!isStrongPassword(newPassword)) {
            return false;
        }
        
        return userService.resetPassword(userId, newPassword);
    }
    
    /**
     * Récupère un utilisateur par son ID.
     * 
     * @param userId L'ID de l'utilisateur à récupérer
     * @return L'utilisateur correspondant, ou null s'il n'existe pas
     */
    public User getUserById(int userId) {
        try {
            return userService.getUserById(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Récupère un utilisateur par son email.
     * 
     * @param email L'email de l'utilisateur à récupérer
     * @return L'utilisateur ou null s'il n'existe pas
     * @throws Exception Si une erreur survient lors de la récupération
     */
    public User getUserByEmail(String email) throws Exception {
        if (!isValidEmail(email)) {
            return null;
        }
        
        return userService.getUserByEmail(email);
    }
} 