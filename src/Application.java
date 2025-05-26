import view.LoginView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import util.ServiceFactory;
import util.ServiceFactorySwapper;
import util.LogManager;
import util.LogManager.LogLevel;

/**
 * Classe principale pour démarrer l'application.
 * Cette classe suit le principe de responsabilité unique (SRP) en s'occupant uniquement
 * du démarrage de l'application.
 */
public class Application {
    
    private static final String TAG = "Application";
    
    /**
     * Point d'entrée principal de l'application.
     * 
     * @param args Arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Initialisation de la journalisation
        LogManager.initialize();
        
        // Définir le niveau de journalisation en fonction des arguments
        if (args.length > 0 && args[0].equalsIgnoreCase("--debug")) {
            LogManager.setLogLevel(LogLevel.DEBUG);
            LogManager.debug(TAG, "Mode debug activé");
        } else {
            LogManager.setLogLevel(LogLevel.INFO);
        }
        
        LogManager.info(TAG, "Démarrage de l'application");
        
        // Configuration des propriétés système pour améliorer la compatibilité audio sur macOS
        System.setProperty("javax.sound.sampled.Clip", "com.sun.media.sound.DirectAudioDeviceProvider");
        System.setProperty("javax.sound.sampled.Port", "com.sun.media.sound.PortMixerProvider");
        LogManager.debug(TAG, "Propriétés système configurées pour l'audio");
        
        // Initialisation préalable des services (pour vérifier que tout fonctionne)
        try {
            // Remplacer la factory standard par notre version corrigée
            if (args.length > 0 && args[0].equalsIgnoreCase("--fix")) {
                LogManager.info(TAG, "Utilisation des correctifs pour les services");
                ServiceFactorySwapper.getInstance();
                
                // Tentative de remplacement de ServiceFactory (peut ne pas fonctionner dans tous les environnements)
                if (ServiceFactorySwapper.replaceServiceFactory()) {
                    LogManager.info(TAG, "ServiceFactory a été remplacé par la version corrigée");
                } else {
                    LogManager.warning(TAG, "Impossible de remplacer ServiceFactory, utilisation directe de ServiceFactorySwapper");
                }
            } else {
                // Utilisation de la factory standard
                ServiceFactory.getInstance();
                LogManager.info(TAG, "Services standard initialisés avec succès");
            }
        } catch (Exception e) {
            LogManager.fatal(TAG, "Erreur lors de l'initialisation des services", e);
            System.exit(1); // Arrêter l'application en cas d'erreur critique
        }
        
        // Démarrage de l'interface graphique
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Utilisation du look and feel du système
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    LogManager.debug(TAG, "Look and feel configuré: " + UIManager.getSystemLookAndFeelClassName());
                    
                    // Affichage de l'écran de connexion
                    LoginView loginView = new LoginView();
                    loginView.setVisible(true);
                    
                    LogManager.info(TAG, "Interface graphique démarrée avec succès");
                } catch (Exception e) {
                    LogManager.error(TAG, "Erreur lors du démarrage de l'interface", e);
                }
            }
        });
    }
} 