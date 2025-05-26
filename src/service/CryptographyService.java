package service;

import javax.crypto.SecretKey;

/**
 * Interface pour le service de cryptographie.
 * Ce service est responsable de toutes les opérations de cryptographie de l'application,
 * y compris le chiffrement, le déchiffrement et la génération de hachages.
 */
public interface CryptographyService {
    
    /**
     * Génère une nouvelle clé secrète pour le chiffrement.
     * 
     * @return Une nouvelle clé secrète
     * @throws Exception Si une erreur survient lors de la génération
     */
    SecretKey generateSecretKey() throws Exception;
    
    /**
     * Chiffre des données avec une clé secrète.
     * 
     * @param data Les données à chiffrer
     * @param key La clé secrète à utiliser
     * @return Les données chiffrées
     * @throws Exception Si une erreur survient lors du chiffrement
     */
    byte[] encrypt(byte[] data, SecretKey key) throws Exception;
    
    /**
     * Déchiffre des données avec une clé secrète.
     * 
     * @param encryptedData Les données chiffrées
     * @param key La clé secrète à utiliser
     * @return Les données déchiffrées
     * @throws Exception Si une erreur survient lors du déchiffrement
     */
    byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception;
    
    /**
     * Encode une clé secrète en chaîne Base64.
     * 
     * @param key La clé secrète à encoder
     * @return La chaîne Base64 représentant la clé
     */
    String encodeKeyToBase64(SecretKey key);
    
    /**
     * Décode une chaîne Base64 en clé secrète.
     * 
     * @param keyBase64 La chaîne Base64 représentant la clé
     * @return La clé secrète décodée
     */
    SecretKey decodeKeyFromBase64(String keyBase64);
    
    /**
     * Génère un hachage à partir de données.
     * 
     * @param data Les données à hacher
     * @return Le hachage sous forme de chaîne
     * @throws Exception Si une erreur survient lors du hachage
     */
    String generateHash(byte[] data) throws Exception;
    
    /**
     * Vérifie si un hachage correspond aux données.
     * 
     * @param data Les données à vérifier
     * @param expectedHash Le hachage attendu
     * @return true si le hachage correspond, false sinon
     * @throws Exception Si une erreur survient lors de la vérification
     */
    boolean verifyHash(byte[] data, String expectedHash) throws Exception;
    
    /**
     * Chiffre une chaîne de texte avec une clé secrète.
     * 
     * @param data Le texte à chiffrer
     * @param key La clé secrète à utiliser
     * @return Le texte chiffré
     * @throws Exception Si une erreur survient lors du chiffrement
     */
    String encryptString(String data, SecretKey key) throws Exception;
    
    /**
     * Déchiffre une chaîne de texte avec une clé secrète.
     * 
     * @param encryptedData Le texte chiffré
     * @param key La clé secrète à utiliser
     * @return Le texte déchiffré
     * @throws Exception Si une erreur survient lors du déchiffrement
     */
    String decryptString(String encryptedData, SecretKey key) throws Exception;
} 