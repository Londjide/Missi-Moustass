package test;

import controller.AuthController;
import util.ServiceFactory;

public class TestInscription {
    public static void main(String[] args) {
        try {
            // Créer une instance du contrôleur d'authentification
            AuthController authController = new AuthController();
            
            // Tester l'inscription d'un nouvel utilisateur
            String email = "test@example.com";
            String password = "Test123!@#";
            
            System.out.println("Tentative d'inscription...");
            int userId = authController.register(email, password, false);
            
            if (userId > 0) {
                System.out.println("Inscription réussie ! ID utilisateur : " + userId);
                
                // Tester la connexion
                System.out.println("\nTentative de connexion...");
                int loginUserId = authController.login(email, password);
                
                if (loginUserId > 0) {
                    System.out.println("Connexion réussie ! ID utilisateur : " + loginUserId);
                } else {
                    System.out.println("Échec de la connexion");
                }
            } else {
                System.out.println("Échec de l'inscription");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test : " + e.getMessage());
            e.printStackTrace();
        }
    }
} 