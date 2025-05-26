package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

/**
 * Utilitaire pour tester la connexion d'un utilisateur.
 */
public class UserLogin {
    
    private static final String DB_URL = "jdbc:sqlite:database.db";
    
    /**
     * Point d'entrée pour tester la connexion d'un utilisateur.
     */
    public static void main(String[] args) {
        try {
            // Charger le pilote SQLite
            Class.forName("org.sqlite.JDBC");
            
            Scanner scanner = new Scanner(System.in);
            
            // Demander l'email
            System.out.print("Email: ");
            String email = scanner.nextLine();
            
            // Demander le mot de passe
            System.out.print("Mot de passe: ");
            String password = scanner.nextLine();
            
            // Tester la connexion
            boolean success = testLogin(email, password);
            
            if (success) {
                System.out.println("\nConnexion réussie!");
            } else {
                System.out.println("\nÉchec de la connexion. Email ou mot de passe incorrect.");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Teste la connexion d'un utilisateur.
     */
    private static boolean testLogin(String email, String password) throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT id, password_hash, salt FROM users WHERE email = ?")) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String storedHash = rs.getString("password_hash");
                    String salt = rs.getString("salt");
                    
                    // Calcul du hash du mot de passe fourni avec le sel stocké
                    String calculatedHash = hashPassword(password, salt);
                    
                    System.out.println("\nDétails de l'authentification:");
                    System.out.println("- ID utilisateur: " + userId);
                    System.out.println("- Email: " + email);
                    System.out.println("- Hash stocké: " + storedHash);
                    System.out.println("- Sel: " + salt);
                    System.out.println("- Hash calculé: " + calculatedHash);
                    System.out.println("- Correspondent? " + storedHash.equals(calculatedHash));
                    
                    // Comparaison des hachages
                    return storedHash.equals(calculatedHash);
                }
                
                System.out.println("\nUtilisateur non trouvé: " + email);
                return false;
            }
        }
    }
    
    /**
     * Calcule le hachage d'un mot de passe avec un sel.
     */
    private static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
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
} 