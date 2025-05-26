package service;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Service de cryptographie RSA pour le partage sécurisé d'enregistrements
 * audio.
 * Cette classe permet de générer des paires de clés RSA, et de
 * chiffrer/déchiffrer
 * des données avec ces clés.
 */
public class RSACryptographyService {

    private static final String RSA_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    /**
     * Génère une nouvelle paire de clés RSA.
     * 
     * @return Un tableau contenant la clé publique et la clé privée encodées en
     *         Base64
     * @throws Exception Si une erreur survient lors de la génération
     */
    public String[] generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGen.initialize(KEY_SIZE);
        KeyPair keyPair = keyGen.generateKeyPair();

        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        return new String[] { publicKey, privateKey };
    }

    /**
     * Chiffre des données avec une clé publique RSA.
     * 
     * @param data            Les données à chiffrer
     * @param publicKeyBase64 La clé publique encodée en Base64
     * @return Les données chiffrées encodées en Base64
     * @throws Exception Si une erreur survient lors du chiffrement
     */
    public String encryptWithPublicKey(String data, String publicKeyBase64) throws Exception {
        // Décodage de la clé publique
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        // Chiffrement des données
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Déchiffre des données avec une clé privée RSA.
     * 
     * @param encryptedData    Les données chiffrées encodées en Base64
     * @param privateKeyBase64 La clé privée encodée en Base64
     * @return Les données déchiffrées
     * @throws Exception Si une erreur survient lors du déchiffrement
     */
    public String decryptWithPrivateKey(String encryptedData, String privateKeyBase64) throws Exception {
        try {
            // Vérifier les entrées
            if (encryptedData == null || privateKeyBase64 == null) {
                System.err.println("Données chiffrées ou clé privée null");
                return null;
            }

            System.out.println("Déchiffrement avec clé privée: longueur des données = " + encryptedData.length());

            // Décodage de la clé privée
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // Déchiffrement des données
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] encryptedBytes;
            try {
                encryptedBytes = Base64.getDecoder().decode(encryptedData);
            } catch (IllegalArgumentException e) {
                System.err.println("Format de données chiffrées invalide: " + e.getMessage());
                return null;
            }

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes);
        } catch (Exception e) {
            System.err.println("Erreur lors du déchiffrement avec clé privée: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}