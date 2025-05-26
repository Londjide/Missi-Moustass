package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utilitaire pour afficher le contenu de la table users.
 */
public class UserTableViewer {
    
    private static final String DB_URL = "jdbc:sqlite:database.db";
    
    /**
     * Point d'entrée pour l'affichage des utilisateurs.
     */
    public static void main(String[] args) {
        try {
            // Charger le pilote SQLite
            Class.forName("org.sqlite.JDBC");
            
            // Afficher les utilisateurs
            displayUsers();
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche les utilisateurs dans la base de données.
     */
    private static void displayUsers() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Vérifier si la table users existe
            ResultSet tables = conn.getMetaData().getTables(null, null, "users", null);
            if (!tables.next()) {
                System.out.println("La table 'users' n'existe pas dans la base de données!");
                return;
            }
            
            // Récupérer les utilisateurs
            ResultSet rs = stmt.executeQuery("SELECT id, email, password_hash, salt, is_admin FROM users");
            
            // Afficher l'en-tête
            System.out.println("+----+--------------------+------------------------------------------------------------------+------------+--------+");
            System.out.println("| ID | Email              | Password Hash                                                    | Salt       | Admin  |");
            System.out.println("+----+--------------------+------------------------------------------------------------------+------------+--------+");
            
            // Afficher les utilisateurs
            boolean hasUsers = false;
            while (rs.next()) {
                hasUsers = true;
                int id = rs.getInt("id");
                String email = rs.getString("email");
                String passwordHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                boolean isAdmin = rs.getInt("is_admin") == 1;
                
                System.out.printf("| %-2d | %-18s | %-64s | %-10s | %-6s |\n", 
                    id, email, passwordHash, salt, isAdmin ? "Oui" : "Non");
            }
            
            System.out.println("+----+--------------------+------------------------------------------------------------------+------------+--------+");
            
            if (!hasUsers) {
                System.out.println("Aucun utilisateur trouvé dans la base de données!");
            }
        }
    }
} 