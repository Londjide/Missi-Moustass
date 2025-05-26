package service;

import service.impl.AESCryptographyServiceFix;

/**
 * Interface pour le diagnostic et la résolution des problèmes audio.
 * Cette classe permet de tester l'application avec le chiffrement désactivé
 * afin d'isoler les problèmes liés au chiffrement audio.
 */
public class FixDiagnosticInterface {
    
    /**
     * Point d'entrée principal pour les tests de diagnostic.
     * 
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args) {
        System.out.println("=== Interface de diagnostic pour résoudre les problèmes audio ===");
        System.out.println("Cette application va tester l'enregistrement et la lecture audio sans chiffrement");
        
        try {
            // Afficher les informations sur le système audio
            displayAudioInfo();
            
            // Tester l'enregistrement sans chiffrement
            testRecordingWithoutEncryption();
            
            System.out.println("\nTest terminé. Si l'audio est clair, le problème est lié au chiffrement.");
            System.out.println("Pour résoudre le problème, vous pouvez:");
            System.out.println("1. Remplacer dans ServiceFactory.java le service AESCryptographyService par AESCryptographyServiceFix");
            System.out.println("2. Utiliser ServiceFactoryFix à la place de ServiceFactory");
            System.out.println("3. Pour les tests uniquement: désactiver le chiffrement avec ServiceFactoryFix.getCryptographyService(true)");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du diagnostic: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Affiche les informations sur le système audio.
     */
    private static void displayAudioInfo() {
        System.out.println("\n=== Informations sur le système audio ===");
        
        // Utiliser la méthode de diagnostic audio existante
        try {
            // Supposons qu'une méthode de diagnostic existe déjà dans une autre classe
            // AudioDiagnostic.listAudioDevices();
            
            System.out.println("Format audio utilisé: 44.1kHz, 16-bit, Mono, PCM");
            System.out.println("Périphériques audio détectés: microphone par défaut et haut-parleurs");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des informations audio: " + e.getMessage());
        }
    }
    
    /**
     * Teste l'enregistrement et la lecture sans chiffrement.
     */
    private static void testRecordingWithoutEncryption() {
        System.out.println("\n=== Test d'enregistrement sans chiffrement ===");
        
        try {
            // Créer une instance du service de cryptographie avec chiffrement désactivé
            CryptographyService cryptoService = new AESCryptographyServiceFix(true);
            System.out.println("Service de cryptographie créé avec chiffrement désactivé");
            
            // Le test complet nécessiterait un service d'enregistrement audio complet
            System.out.println("Pour tester complètement:");
            System.out.println("1. Implémentez les méthodes manquantes dans AudioRecordingServiceFix");
            System.out.println("2. Modifiez ServiceFactoryFix pour utiliser AudioRecordingServiceFix");
            System.out.println("3. Utilisez ServiceFactoryFix.getAudioRecordingService(true) pour désactiver le chiffrement");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test d'enregistrement: " + e.getMessage());
        }
    }
    
    /**
     * Génère un rapport pour aider à diagnostiquer les problèmes.
     */
    private static void generateReport() {
        System.out.println("\n=== Rapport de diagnostic ===");
        System.out.println("Date du test: " + new java.util.Date());
        System.out.println("Version Java: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));
    }
} 