package util;

import service.*;
import service.impl.AESCryptographyServiceFix;
import service.impl.UserServiceImplFix;
import service.impl.AudioRecordingServiceFixExtended;
import service.impl.SQLiteDatabaseService;

/**
 * Classe utilitaire pour faciliter le remplacement de ServiceFactory par ServiceFactoryFixComplete.
 * Cette classe imite l'interface de ServiceFactory mais utilise les implémentations corrigées.
 */
public class ServiceFactorySwapper {
    
    private static ServiceFactorySwapper instance;
    
    private final UserService userService;
    private final CryptographyService cryptographyService;
    private final AudioRecordingService audioRecordingService;
    
    /**
     * Constructeur privé pour empêcher l'instanciation directe.
     * Initialise tous les services avec les versions corrigées.
     */
    private ServiceFactorySwapper() {
        // Création des services de base avec les implémentations corrigées
        cryptographyService = new AESCryptographyServiceFix(false);
        
        // Création des services qui dépendent d'autres services
        userService = new UserServiceImplFix(new SQLiteDatabaseService(), cryptographyService);
        audioRecordingService = new AudioRecordingServiceFixExtended(cryptographyService);
        
        // Initialisation de la base de données (si nécessaire)
        try {
            service.ServiceFactoryFixComplete.createTables();
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
    public static synchronized ServiceFactorySwapper getInstance() {
        if (instance == null) {
            instance = new ServiceFactorySwapper();
        }
        return instance;
    }
    
    /**
     * Remplace l'instance de ServiceFactory par cette implémentation.
     * Note: Cette méthode utilise la réflexion et n'est pas garantie de fonctionner dans tous les environnements.
     * 
     * @return true si le remplacement a réussi, false sinon
     */
    public static boolean replaceServiceFactory() {
        try {
            ServiceFactorySwapper swapper = getInstance();
            
            java.lang.reflect.Field instanceField = ServiceFactory.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            
            // Création d'un proxy qui délègue les appels à notre swapper
            java.lang.reflect.Proxy proxy = (java.lang.reflect.Proxy) java.lang.reflect.Proxy.newProxyInstance(
                ServiceFactory.class.getClassLoader(),
                new Class<?>[] { ServiceFactory.class },
                new java.lang.reflect.InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        
                        if (methodName.equals("getUserService")) {
                            return swapper.getUserService();
                        } else if (methodName.equals("getCryptographyService")) {
                            return swapper.getCryptographyService();
                        } else if (methodName.equals("getAudioRecordingService")) {
                            return swapper.getAudioRecordingService();
                        }
                        
                        // Méthode non gérée, retourner null
                        return null;
                    }
                }
            );
            
            // Remplacement de l'instance existante par notre proxy
            instanceField.set(null, proxy);
            
            System.out.println("ServiceFactory a été remplacé avec succès par ServiceFactorySwapper");
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors du remplacement de ServiceFactory: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
} 