package service.impl;

import service.CryptographyService;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class AESCryptographyServiceFix implements CryptographyService {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int KEY_SIZE = 256;

    private boolean encryptionEnabled = true;

    public AESCryptographyServiceFix(boolean disableEncryption) {
        this.encryptionEnabled = !disableEncryption;
    }

    public void setEncryptionEnabled(boolean enabled) {
        this.encryptionEnabled = enabled;
    }

    @Override
    public SecretKey generateSecretKey() throws Exception {
        if (!encryptionEnabled) {
            return null;
        }
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE);
        return keyGen.generateKey();
    }

    @Override
    public byte[] encrypt(byte[] data, SecretKey key) throws Exception {
        if (!encryptionEnabled || key == null) {
            return data;
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
        if (!encryptionEnabled || key == null) {
            return encryptedData;
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }

    @Override
    public String encodeKeyToBase64(SecretKey key) {
        if (key == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    @Override
    public SecretKey decodeKeyFromBase64(String keyBase64) {
        if (keyBase64 == null) {
            return null;
        }
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    @Override
    public String generateHash(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hash = digest.digest(data);
        return Base64.getEncoder().encodeToString(hash);
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
}