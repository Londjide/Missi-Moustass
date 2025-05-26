package service;

import service.UserService;
import service.impl.UserServiceImplFix;
import service.impl.AudioRecordingServiceFixExtended;
import service.AudioRecordingService;
import service.CryptographyService;
import service.DatabaseService;

/**
 * Classe permettant de remplacer les implémentations des services standards
 * par des versions corrigées.
 */
public class ServiceFactorySwapper {
    private static ServiceFactorySwapper instance;
    
    private UserService userService;
    private AudioRecordingService audioRecordingService;
    private DatabaseService databaseService;
    private CryptographyService cryptographyService;
    
    private ServiceFactorySwapper() {
        // Ce constructeur sera complété ultérieurement
    }
    
    public static ServiceFactorySwapper getInstance() {
        if (instance == null) {
            instance = new ServiceFactorySwapper();
        }
        return instance;
    }
    
    public void initialize(DatabaseService dbService, CryptographyService cryptoService) {
        this.databaseService = dbService;
        this.cryptographyService = cryptoService;
        
        // Initialiser les implémentations corrigées
        this.userService = new UserServiceImplFix(databaseService, cryptographyService);
        this.audioRecordingService = new AudioRecordingServiceFixExtended(cryptographyService);
    }
    
    public UserService getUserService() {
        return userService;
    }
    
    public AudioRecordingService getAudioRecordingService() {
        return audioRecordingService;
    }
    
    public DatabaseService getDatabaseService() {
        return databaseService;
    }
    
    public CryptographyService getCryptographyService() {
        return cryptographyService;
    }
    
    public static boolean replaceServiceFactory() {
        return true;
    }
} 