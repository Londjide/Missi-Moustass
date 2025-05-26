package service;

import service.impl.AESCryptographyServiceFix;
import service.impl.AudioRecordingServiceFixExtended;
import service.impl.SQLiteDatabaseService;
import service.impl.UserServiceImplFix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory complète pour créer les instances des services améliorés.
 * Cette version n'a plus de dépendances à l'ancienne ServiceFactory.
 */
public class ServiceFactoryFixComplete {
    private static final Logger LOGGER = Logger.getLogger(ServiceFactoryFixComplete.class.getName());
    
    private static final String DB_URL = "jdbc:sqlite:database.db";
    
    private static UserService userService;
    private static CryptographyService cryptographyService;
    private static AudioRecordingService audioRecordingService;
    
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
     * Obtient l'instance du service utilisateur.
     * Utilise l'implémentation corrigée qui récupère correctement tous les utilisateurs.
     * 
     * @return Le service utilisateur
     */
    public static UserService getUserService() {
        if (userService == null) {
            userService = new UserServiceImplFix(new SQLiteDatabaseService(), getCryptographyService());
        }
        return userService;
    }
    
    /**
     * Obtient l'instance du service de cryptographie.
     * Utilise l'implémentation améliorée qui permet de désactiver le chiffrement pour le débogage.
     * 
     * @param disableEncryption true pour désactiver le chiffrement (pour le débogage), false sinon
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
     * Obtient l'instance du service de cryptographie avec le chiffrement activé par défaut.
     * 
     * @return Le service de cryptographie
     */
    public static CryptographyService getCryptographyService() {
        return getCryptographyService(false);
    }
    
    /**
     * Obtient l'instance du service d'enregistrement audio.
     * 
     * @param disableEncryption true pour désactiver le chiffrement (pour le débogage), false sinon
     * @return Le service d'enregistrement audio
     */
    public static AudioRecordingService getAudioRecordingService(boolean disableEncryption) {
        if (audioRecordingService == null) {
            try {
                // Utiliser le service de cryptographie avec l'option de désactivation du chiffrement
                CryptographyService crypto = getCryptographyService(disableEncryption);
                
                LOGGER.log(Level.INFO, "Initialisation du service d'enregistrement audio avec chiffrement {0}", 
                        disableEncryption ? "désactivé" : "activé");
                
                audioRecordingService = new AudioRecordingServiceFixExtended(crypto);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation du service d'enregistrement audio", e);
            }
        }
        return audioRecordingService;
    }
    
    /**
     * Obtient l'instance du service d'enregistrement audio avec le chiffrement activé par défaut.
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
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Table users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT UNIQUE NOT NULL," +
                    "password_hash TEXT NOT NULL," +
                    "salt TEXT NOT NULL," +
                    "is_admin INTEGER NOT NULL DEFAULT 0" +
                    ")");
            
            // Table recordings
            stmt.execute("CREATE TABLE IF NOT EXISTS recordings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "timestamp TEXT NOT NULL," +
                    "duration INTEGER NOT NULL," +
                    "file_path TEXT NOT NULL," +
                    "encryption_key TEXT," +
                    "integrity_hash TEXT," +
                    "user_id INTEGER NOT NULL," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ")");
            
            LOGGER.log(Level.INFO, "Tables créées avec succès");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création des tables", e);
        }
    }
} 