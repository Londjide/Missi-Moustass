package service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Implémentation du service de cryptographie utilisant AES pour le chiffrement
 * et SHA-256 pour les hachages.
 * Cette classe suit le principe de responsabilité unique (SRP) en gérant
 * uniquement les fonctionnalités de cryptographie.
 */
public class AESCryptographyService implements CryptographyService {
    
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private static final String HASH_ALGORITHM = "SHA-256";
    
    @Override
    public SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE);
        return keyGen.generateKey();
    }
    
    @Override
    public byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }
    
    @Override
    public byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }
    
    @Override
    public String encodeKeyToBase64(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    
    @Override
    public SecretKey decodeKeyFromBase64(String keyBase64) {
        byte[] decodedKey = Base64.getDecoder().decode(keyBase64);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }
    
    @Override
    public String generateHash(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hashBytes = digest.digest(data);
        return bytesToHex(hashBytes);
    }
    
    @Override
    public boolean verifyHash(byte[] data, String expectedHash) throws Exception {
        String actualHash = generateHash(data);
        return actualHash.equals(expectedHash);
    }
    
    @Override
    public String encryptString(String data, SecretKey key) throws Exception {
        byte[] dataBytes = data.getBytes();
        byte[] encryptedBytes = encrypt(dataBytes, key);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    @Override
    public String decryptString(String encryptedData, SecretKey key) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = decrypt(encryptedBytes, key);
        return new String(decryptedBytes);
    }
    
    /**
     * Convertit un tableau d'octets en chaîne hexadécimale.
     * 
     * @param bytes Le tableau d'octets à convertir
     * @return La chaîne hexadécimale correspondante
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
} 