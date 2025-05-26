package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

/**
 * Utilitaire pour générer des hachages de mots de passe.
 */
public class PasswordHashGenerator {
    
    /**
     * Point d'entrée pour générer des hachages de mots de passe.
     */
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("=== Générateur de hachage de mot de passe ===\n");
            
            // Demander l'email
            System.out.print("Email: ");
            String email = scanner.nextLine();
            
            // Demander le mot de passe
            System.out.print("Mot de passe: ");
            String password = scanner.nextLine();
            
            System.out.print("Est-ce un administrateur? (o/n): ");
            boolean isAdmin = scanner.nextLine().toLowerCase().startsWith("o");
            
            // Générer le sel aléatoire
            String salt = generateSalt();
            
            // Calculer le hachage
            String passwordHash = hashPassword(password, salt);
            
            System.out.println("\nInformations de l'utilisateur:");
            System.out.println("- Email: " + email);
            System.out.println("- Sel: " + salt);
            System.out.println("- Hash: " + passwordHash);
            System.out.println("- Admin: " + (isAdmin ? "Oui" : "Non"));
            
            System.out.println("\nRequête SQL pour insérer cet utilisateur:");
            System.out.println("INSERT INTO users (email, password_hash, salt, is_admin) VALUES");
            System.out.println("('" + email + "', '" + passwordHash + "', '" + salt + "', " + (isAdmin ? "1" : "0") + ");");
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Génère un sel aléatoire pour le hachage des mots de passe.
     */
    private static String generateSalt() {
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