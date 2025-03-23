package com.barbichetz.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

/**
 * Classe gérant le chiffrement et déchiffrement des messages vocaux
 */
public class MessageEncryption {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private final SecretKey key;
    private final SecureRandom secureRandom;
    
    public MessageEncryption() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        this.key = keyGen.generateKey();
        this.secureRandom = new SecureRandom();
    }
    
    public byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(data);
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return combined;
    }
    
    public byte[] decrypt(byte[] encryptedData) throws Exception {
        byte[] iv = new byte[16];
        byte[] encrypted = new byte[encryptedData.length - 16];
        System.arraycopy(encryptedData, 0, iv, 0, iv.length);
        System.arraycopy(encryptedData, iv.length, encrypted, 0, encrypted.length);
        
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(encrypted);
    }
} 