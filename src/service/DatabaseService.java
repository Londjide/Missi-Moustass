package service;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface pour les services d'accès à la base de données.
 * Cette interface suit le principe d'inversion de dépendance (DIP) en définissant
 * un contrat que les classes concrètes devront implémenter.
 */
public interface DatabaseService {
    
    /**
     * Établit une connexion à la base de données.
     * 
     * @return Une connexion SQL active
     * @throws SQLException Si une erreur survient lors de la connexion
     */
    Connection connect() throws SQLException;
    
    /**
     * Ferme la connexion à la base de données.
     * 
     * @param conn La connexion à fermer
     */
    void closeConnection(Connection conn);
    
    /**
     * Exécute une requête SQL de mise à jour (INSERT, UPDATE, DELETE).
     * 
     * @param sql La requête SQL à exécuter
     * @param params Les paramètres à utiliser dans la requête
     * @return Le nombre de lignes affectées ou l'ID généré
     * @throws SQLException Si une erreur survient lors de l'exécution
     */
    int executeUpdate(String sql, Object... params) throws SQLException;
    
    /**
     * Vérifie si la base de données est correctement initialisée
     * et crée les tables nécessaires si elles n'existent pas.
     * 
     * @throws SQLException Si une erreur survient lors de l'initialisation
     */
    void initializeDatabase() throws SQLException;
} 