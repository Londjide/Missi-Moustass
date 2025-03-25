package Auth;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class AudioRecorderTest {
    
    @Test
    public void testDatabaseInitialization() {
        AudioRecorder recorder = new AudioRecorder();
        assertTrue("La base de données devrait être initialisée", new File("mac1.db").exists());
    }
    
    @Test
    public void testEncryption() {
        try {
            byte[] testData = "Test message".getBytes();
            AES aes = new AES();
            byte[] encrypted = aes.encrypt(testData);
            byte[] decrypted = aes.decrypt(encrypted);
            assertArrayEquals("Les données déchiffrées devraient correspondre aux données originales", testData, decrypted);
        } catch (Exception e) {
            fail("Le test de chiffrement a échoué: " + e.getMessage());
        }
    }
    
    @Test
    public void testSHAIntegrity() {
        try {
            String testMessage = "Test message";
            String hash1 = SHA.calculateSHA(testMessage);
            String hash2 = SHA.calculateSHA(testMessage);
            assertEquals("Les hashs devraient être identiques pour le même message", hash1, hash2);
        } catch (Exception e) {
            fail("Le test d'intégrité SHA a échoué: " + e.getMessage());
        }
    }
} 