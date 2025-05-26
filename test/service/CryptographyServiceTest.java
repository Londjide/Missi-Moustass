package test.service;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import service.CryptographyService;
import service.AESCryptographyService;
import javax.crypto.SecretKey;

/**
 * Tests unitaires pour le service de cryptographie.
 */
public class CryptographyServiceTest {
    
    private CryptographyService cryptographyService;
    
    @Before
    public void setUp() {
        // Initialisation du service avant chaque test
        cryptographyService = new AESCryptographyService();
    }
    
    @Test
    public void testGenerateSecretKey() throws Exception {
        // Test de génération de clé
        SecretKey key = cryptographyService.generateSecretKey();
        
        // Vérifier que la clé n'est pas nulle
        assertNotNull("La clé générée ne devrait pas être nulle", key);
        
        // Vérifier que l'algorithme est bien AES
        assertEquals("L'algorithme devrait être AES", "AES", key.getAlgorithm());
    }
    
    @Test
    public void testEncryptAndDecrypt() throws Exception {
        // Données de test
        String originalText = "Texte à chiffrer";
        byte[] originalData = originalText.getBytes();
        
        // Générer une clé
        SecretKey key = cryptographyService.generateSecretKey();
        
        // Chiffrer les données
        byte[] encryptedData = cryptographyService.encrypt(originalData, key);
        
        // Vérifier que les données chiffrées sont différentes des données originales
        assertFalse("Les données chiffrées devraient être différentes des données originales",
                    areArraysEqual(originalData, encryptedData));
        
        // Déchiffrer les données
        byte[] decryptedData = cryptographyService.decrypt(encryptedData, key);
        
        // Vérifier que les données déchiffrées sont identiques aux données originales
        assertTrue("Les données déchiffrées devraient être identiques aux données originales",
                  areArraysEqual(originalData, decryptedData));
        
        // Vérifier le texte déchiffré
        String decryptedText = new String(decryptedData);
        assertEquals("Le texte déchiffré devrait être identique au texte original",
                    originalText, decryptedText);
    }
    
    @Test
    public void testEncodeAndDecodeKey() throws Exception {
        // Générer une clé
        SecretKey originalKey = cryptographyService.generateSecretKey();
        
        // Convertir la clé en Base64
        String keyBase64 = cryptographyService.encodeKeyToBase64(originalKey);
        
        // Vérifier que la chaîne Base64 n'est pas vide
        assertNotNull("La chaîne Base64 ne devrait pas être nulle", keyBase64);
        assertFalse("La chaîne Base64 ne devrait pas être vide", keyBase64.isEmpty());
        
        // Reconvertir la chaîne Base64 en clé
        SecretKey decodedKey = cryptographyService.decodeKeyFromBase64(keyBase64);
        
        // Vérifier que la clé décodée n'est pas nulle
        assertNotNull("La clé décodée ne devrait pas être nulle", decodedKey);
        
        // Vérifier que la clé décodée a le même format que la clé originale
        assertEquals("L'algorithme de la clé décodée devrait être identique à celui de la clé originale",
                    originalKey.getAlgorithm(), decodedKey.getAlgorithm());
        
        // Vérifier que les clés ont le même contenu
        assertTrue("Les clés devraient avoir le même contenu",
                  areArraysEqual(originalKey.getEncoded(), decodedKey.getEncoded()));
    }
    
    @Test
    public void testGenerateAndVerifyHash() throws Exception {
        // Données de test
        byte[] data = "Données à hacher".getBytes();
        
        // Générer le hachage
        String hash = cryptographyService.generateHash(data);
        
        // Vérifier que le hachage n'est pas vide
        assertNotNull("Le hachage ne devrait pas être nul", hash);
        assertFalse("Le hachage ne devrait pas être vide", hash.isEmpty());
        
        // Vérifier que le hachage est correct
        boolean verificationResult = cryptographyService.verifyHash(data, hash);
        assertTrue("La vérification du hachage devrait réussir", verificationResult);
        
        // Vérifier qu'un hachage incorrect est détecté
        boolean incorrectVerification = cryptographyService.verifyHash(
            "Données différentes".getBytes(), hash);
        assertFalse("La vérification d'un hachage incorrect devrait échouer", incorrectVerification);
    }
    
    /**
     * Méthode utilitaire pour comparer deux tableaux d'octets.
     */
    private boolean areArraysEqual(byte[] array1, byte[] array2) {
        if (array1.length != array2.length) {
            return false;
        }
        
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        
        return true;
    }
} 