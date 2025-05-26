package service.impl;

import service.CryptographyService;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CryptographyServiceFix implements CryptographyService {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final SecretKey DEFAULT_KEY;
    private static final Logger LOGGER = Logger.getLogger(CryptographyServiceFix.class.getName());

    static {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(256);
            DEFAULT_KEY = keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation de la clé par défaut", e);
        }
    }

    @Override
    public SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256);
        return keyGen.generateKey();
    }

    @Override
    public byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key != null ? key : DEFAULT_KEY);
        return cipher.doFinal(data);
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key != null ? key : DEFAULT_KEY);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            // Journaliser l'erreur et la propager
            LOGGER.log(Level.SEVERE, "Erreur lors du déchiffrement: " + e.getMessage(), e);
            throw new SecurityException("Erreur de déchiffrement: clé incorrecte ou données corrompues", e);
        }
    }

    @Override
    public String encryptString(String data, SecretKey key) throws Exception {
        byte[] encryptedData = encrypt(data.getBytes(), key);
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    @Override
    public String decryptString(String encryptedData, SecretKey key) throws Exception {
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = decrypt(decodedData, key);
        return new String(decryptedData);
    }

    @Override
    public String generateHash(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean verifyHash(byte[] data, String hash) throws Exception {
        String calculatedHash = generateHash(data);
        return calculatedHash.equals(hash);
    }

    @Override
    public String encodeKeyToBase64(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    @Override
    public SecretKey decodeKeyFromBase64(String encodedKey) {
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}