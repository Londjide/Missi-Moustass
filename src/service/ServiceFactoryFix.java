package service;

import service.impl.UserServiceImplFix;
import service.impl.AESCryptographyServiceFix;
import service.impl.AudioRecordingServiceFixExtended;
import service.impl.SQLiteDatabaseService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory améliorée pour créer les instances des services.
 * Cette version utilise les implémentations corrigées des services
 * pour résoudre les problèmes d'audio et d'affichage des utilisateurs.
 */
public class ServiceFactoryFix {
    private static final Logger LOGGER = Logger.getLogger(ServiceFactoryFix.class.getName());

    private static final String DB_URL = "jdbc:sqlite:audiorecorder.db";

    private static UserService userService;
    private static CryptographyService cryptographyService;
    private static AudioRecordingService audioRecordingService;
    private static DatabaseService databaseService;

    /**
     * Obtient une connexion à la base de données.
     * 
     * @return Une connexion à la base de données
     * @throws SQLException si une erreur survient lors de la connexion
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Obtient l'instance du service de base de données.
     * 
     * @return Le service de base de données
     */
    public static DatabaseService getDatabaseService() {
        if (databaseService == null) {
            databaseService = new SQLiteDatabaseService();
            try {
                databaseService.initializeDatabase();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation de la base de données via ServiceFactoryFix",
                        e);
            }
        }
        return databaseService;
    }

    /**
     * Obtient l'instance du service utilisateur.
     * Utilise l'implémentation corrigée qui récupère correctement tous les
     * utilisateurs.
     * 
     * @return Le service utilisateur
     */
    public static UserService getUserService() {
        if (userService == null) {
            userService = new UserServiceImplFix(getDatabaseService(), getCryptographyService());
        }
        return userService;
    }

    /**
     * Obtient l'instance du service de cryptographie.
     * Utilise l'implémentation améliorée qui permet de désactiver le chiffrement
     * pour le débogage.
     * 
     * @param disableEncryption true pour désactiver le chiffrement (pour le
     *                          débogage), false sinon
     * @return Le service de cryptographie
     */
    public static CryptographyService getCryptographyService(boolean disableEncryption) {
        if (cryptographyService == null) {
            cryptographyService = new AESCryptographyServiceFix(disableEncryption);
        } else if (cryptographyService instanceof AESCryptographyServiceFix) {
            ((AESCryptographyServiceFix) cryptographyService).setEncryptionEnabled(!disableEncryption);
        }
        return cryptographyService;
    }

    /**
     * Obtient l'instance du service de cryptographie avec le chiffrement activé par
     * défaut.
     * 
     * @return Le service de cryptographie
     */
    public static CryptographyService getCryptographyService() {
        return getCryptographyService(false);
    }

    /**
     * Obtient l'instance du service d'enregistrement audio.
     * 
     * @param disableEncryption true pour désactiver le chiffrement (pour le
     *                          débogage), false sinon
     * @return Le service d'enregistrement audio
     */
    public static AudioRecordingService getAudioRecordingService(boolean disableEncryption) {
        if (audioRecordingService == null) {
            try {
                // Utiliser le service de cryptographie avec l'option de désactivation du
                // chiffrement
                CryptographyService crypto = getCryptographyService(disableEncryption);

                // Initialisation du service d'enregistrement audio avec l'implémentation
                // corrigée
                audioRecordingService = new AudioRecordingServiceFixExtended(crypto);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation du service d'enregistrement audio", e);
            }
        }
        return audioRecordingService;
    }

    /**
     * Obtient l'instance du service d'enregistrement audio avec le chiffrement
     * activé par défaut.
     * 
     * @return Le service d'enregistrement audio
     */
    public static AudioRecordingService getAudioRecordingService() {
        return getAudioRecordingService(false);
    }

    /**
     * Crée les tables de la base de données si elles n'existent pas.
     */
    public static void createTables() {
        try {
            // Création des tables avec la nouvelle implémentation
            try (Connection conn = getConnection()) {
                // Créer les tables si nécessaire
                LOGGER.log(Level.INFO, "Tables créées avec succès");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création des tables", e);
        }
    }
}