package service;

import model.User;
import java.util.List;

/**
 * Interface pour les services de gestion des utilisateurs.
 * Cette interface suit le principe d'inversion de dépendance (DIP) en définissant
 * un contrat que les classes concrètes devront implémenter.
 */
public interface UserService {
    
    /**
     * Authentifie un utilisateur.
     * 
     * @param email L'email de l'utilisateur
     * @param password Le mot de passe en clair
     * @return L'ID de l'utilisateur si l'authentification réussit, -1 sinon
     * @throws Exception Si une erreur survient lors de l'authentification
     */
    int authenticateUser(String email, String password) throws Exception;
    
    /**
     * Crée un nouvel utilisateur.
     * 
     * @param email L'email du nouvel utilisateur
     * @param password Le mot de passe en clair
     * @param isAdmin Indique si l'utilisateur est un administrateur
     * @return L'ID du nouvel utilisateur
     * @throws Exception Si une erreur survient lors de la création
     */
    int createUser(String email, String password, boolean isAdmin) throws Exception;
    
    /**
     * Récupère un utilisateur par son ID.
     * 
     * @param userId L'ID de l'utilisateur à récupérer
     * @return L'utilisateur ou null s'il n'existe pas
     * @throws Exception Si une erreur survient lors de la récupération
     */
    User getUserById(int userId) throws Exception;
    
    /**
     * Récupère un utilisateur par son email.
     * 
     * @param email L'email de l'utilisateur à récupérer
     * @return L'utilisateur ou null s'il n'existe pas
     * @throws Exception Si une erreur survient lors de la récupération
     */
    User getUserByEmail(String email) throws Exception;
    
    /**
     * Vérifie si un utilisateur est administrateur.
     * 
     * @param userId L'ID de l'utilisateur à vérifier
     * @return true si l'utilisateur est administrateur, false sinon
     * @throws Exception Si une erreur survient lors de la vérification
     */
    boolean isAdmin(int userId) throws Exception;
    
    /**
     * Réinitialise le mot de passe d'un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur
     * @param newPassword Le nouveau mot de passe en clair
     * @return true si la réinitialisation a réussi, false sinon
     * @throws Exception Si une erreur survient lors de la réinitialisation
     */
    boolean resetPassword(int userId, String newPassword) throws Exception;
    
    /**
     * Récupère tous les utilisateurs.
     * 
     * @return Une liste de tous les utilisateurs
     * @throws Exception Si une erreur survient lors de la récupération
     */
    List<User> getAllUsers() throws Exception;
    
    /**
     * Met à jour un utilisateur existant.
     * 
     * @param user L'utilisateur à mettre à jour
     * @throws Exception Si une erreur survient lors de la mise à jour
     */
    void updateUser(User user) throws Exception;
    
    /**
     * Supprime un utilisateur.
     * 
     * @param id L'ID de l'utilisateur à supprimer
     * @throws Exception Si une erreur survient lors de la suppression
     */
    void deleteUser(int id) throws Exception;
} 