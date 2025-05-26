package util;

import model.User;
import java.io.*;
import java.util.*;

/**
 * Gestionnaire des utilisateurs récemment connectés.
 * Cette classe permet de stocker et de récupérer la liste des utilisateurs 
 * qui se sont récemment connectés à l'application.
 */
public class RecentUsersManager {
    
    private static final String RECENT_USERS_FILE = "recent_users.dat";
    private static final int MAX_RECENT_USERS = 5;
    
    private static RecentUsersManager instance;
    private List<User> recentUsers;
    
    /**
     * Constructeur privé pour le singleton.
     */
    private RecentUsersManager() {
        recentUsers = new ArrayList<>();
        loadRecentUsers();
    }
    
    /**
     * Récupère l'instance unique du gestionnaire.
     * 
     * @return L'instance du gestionnaire
     */
    public static RecentUsersManager getInstance() {
        if (instance == null) {
            instance = new RecentUsersManager();
        }
        return instance;
    }
    
    /**
     * Ajoute un utilisateur à la liste des utilisateurs récents.
     * Si l'utilisateur est déjà dans la liste, il est déplacé en tête.
     * 
     * @param user L'utilisateur à ajouter
     */
    public void addRecentUser(User user) {
        // Ne pas ajouter d'utilisateur null
        if (user == null) {
            return;
        }
        
        // Supprimer l'utilisateur s'il est déjà dans la liste
        recentUsers.removeIf(u -> u.getId() == user.getId());
        
        // Ajouter l'utilisateur en tête de liste
        recentUsers.add(0, user);
        
        // Limiter la taille de la liste
        if (recentUsers.size() > MAX_RECENT_USERS) {
            recentUsers = recentUsers.subList(0, MAX_RECENT_USERS);
        }
        
        // Sauvegarder la liste
        saveRecentUsers();
    }
    
    /**
     * Récupère la liste des utilisateurs récents.
     * 
     * @return La liste des utilisateurs récents
     */
    public List<User> getRecentUsers() {
        return new ArrayList<>(recentUsers);
    }
    
    /**
     * Charge la liste des utilisateurs récents depuis le fichier.
     */
    @SuppressWarnings("unchecked")
    private void loadRecentUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RECENT_USERS_FILE))) {
            recentUsers = (List<User>) ois.readObject();
        } catch (FileNotFoundException e) {
            // Le fichier n'existe pas encore, ce n'est pas une erreur
            System.out.println("Fichier des utilisateurs récents non trouvé. Une nouvelle liste sera créée.");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des utilisateurs récents: " + e.getMessage());
        }
    }
    
    /**
     * Sauvegarde la liste des utilisateurs récents dans le fichier.
     */
    private void saveRecentUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RECENT_USERS_FILE))) {
            oos.writeObject(recentUsers);
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde des utilisateurs récents: " + e.getMessage());
        }
    }
    
    /**
     * Supprime un utilisateur de la liste des utilisateurs récents.
     * 
     * @param userId L'identifiant de l'utilisateur à supprimer
     */
    public void removeRecentUser(int userId) {
        recentUsers.removeIf(u -> u.getId() == userId);
        saveRecentUsers();
    }
    
    /**
     * Efface la liste des utilisateurs récents.
     */
    public void clearRecentUsers() {
        recentUsers.clear();
        saveRecentUsers();
    }
} 