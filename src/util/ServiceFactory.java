package util;

import service.*;
import service.impl.*;

/**
 * Factory pour créer et gérer les instances des services.
 * Cette classe suit le principe de responsabilité unique (SRP) en s'occupant uniquement
 * de la création et de la gestion des services.
 * Elle utilise également le pattern Singleton pour garantir qu'il n'y a qu'une seule
 * instance de chaque service.
 */
public class ServiceFactory {
    
    private static ServiceFactory instance;
    
    private final DatabaseService databaseService;
    private final CryptographyService cryptographyService;
    private final AudioRecordingService audioRecordingService;
    private final UserService userService;
    private final RSACryptographyService rsaCryptographyService;
    private final UserKeysService userKeysService;
    private final SharedRecordingService sharedRecordingService;
    
    /**
     * Constructeur privé pour empêcher l'instanciation directe.
     * Initialise tous les services nécessaires.
     */
    private ServiceFactory() {
        // Création des services de base
        databaseService = new service.impl.SQLiteDatabaseService();
        cryptographyService = new service.impl.AESCryptographyServiceFix(false);
        rsaCryptographyService = new RSACryptographyService();
        
        // Création des services qui dépendent d'autres services
        audioRecordingService = new service.impl.AudioRecordingServiceFixExtended(cryptographyService);
        userService = new service.impl.UserServiceImplFix(databaseService, cryptographyService);
        userKeysService = new UserKeysService(databaseService, rsaCryptographyService);
        
        // Service de partage des enregistrements
        sharedRecordingService = new SharedRecordingService(
            databaseService,
            userService,
            audioRecordingService,
            userKeysService,
            rsaCryptographyService
        );
        
        // Initialisation de la base de données
        try {
            databaseService.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtient l'instance unique de la factory (Singleton).
     * 
     * @return L'instance de la factory
     */
    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }
    
    /**
     * Obtient le service de base de données.
     * 
     * @return Le service de base de données
     */
    public DatabaseService getDatabaseService() {
        return databaseService;
    }
    
    /**
     * Obtient le service de cryptographie.
     * 
     * @return Le service de cryptographie
     */
    public CryptographyService getCryptographyService() {
        return cryptographyService;
    }
    
    /**
     * Obtient le service de gestion des enregistrements audio.
     * 
     * @return Le service de gestion des enregistrements audio
     */
    public AudioRecordingService getAudioRecordingService() {
        return audioRecordingService;
    }
    
    /**
     * Obtient le service de gestion des utilisateurs.
     * 
     * @return Le service de gestion des utilisateurs
     */
    public UserService getUserService() {
        return userService;
    }
    
    /**
     * Obtient le service de cryptographie RSA.
     * 
     * @return Le service de cryptographie RSA
     */
    public RSACryptographyService getRSACryptographyService() {
        return rsaCryptographyService;
    }
    
    /**
     * Obtient le service de gestion des clés utilisateur.
     * 
     * @return Le service de gestion des clés utilisateur
     */
    public UserKeysService getUserKeysService() {
        return userKeysService;
    }
    
    /**
     * Obtient le service de partage des enregistrements.
     * 
     * @return Le service de partage des enregistrements
     */
    public SharedRecordingService getSharedRecordingService() {
        return sharedRecordingService;
    }
} 