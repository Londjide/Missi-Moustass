import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

// Import des classes de test
import test.service.CryptographyServiceTest;
import test.util.AudioFormatManagerTest;

/**
 * Cette classe exécute tous les tests unitaires du projet.
 */
@RunWith(Suite.class)
@SuiteClasses({
    // Tests des services
    CryptographyServiceTest.class,
    
    // Tests des utilitaires
    AudioFormatManagerTest.class
})
public class TestRunner {
    
    /**
     * Point d'entrée pour exécuter les tests unitaires.
     * 
     * @param args Arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        System.out.println("Exécution des tests unitaires...");
        
        Result result = JUnitCore.runClasses(TestRunner.class);
        
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        
        System.out.println("Résultats:");
        System.out.println("- Tests exécutés: " + result.getRunCount());
        System.out.println("- Tests réussis: " + (result.getRunCount() - result.getFailureCount()));
        System.out.println("- Tests échoués: " + result.getFailureCount());
        System.out.println("- Temps d'exécution: " + result.getRunTime() + " ms");
        
        System.out.println("Statut: " + (result.wasSuccessful() ? "SUCCÈS" : "ÉCHEC"));
    }
} 