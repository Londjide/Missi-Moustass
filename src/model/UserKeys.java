package model;

/**
 * Classe représentant les paires de clés d'un utilisateur pour le chiffrement RSA.
 * Cette classe stocke les clés publique et privée d'un utilisateur pour permettre
 * le partage sécurisé d'enregistrements audio.
 */
public class UserKeys {
    private int userId;
    private String publicKey;
    private String privateKey;
    
    /**
     * Constructeur pour une nouvelle paire de clés.
     * 
     * @param userId L'identifiant de l'utilisateur
     * @param publicKey La clé publique encodée en Base64
     * @param privateKey La clé privée encodée en Base64
     */
    public UserKeys(int userId, String publicKey, String privateKey) {
        this.userId = userId;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
    
    /**
     * Constructeur pour une paire de clés publique uniquement (pour les autres utilisateurs).
     * 
     * @param userId L'identifiant de l'utilisateur
     * @param publicKey La clé publique encodée en Base64
     */
    public UserKeys(int userId, String publicKey) {
        this.userId = userId;
        this.publicKey = publicKey;
        this.privateKey = null;
    }
    
    // Getters et setters
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getPublicKey() {
        return publicKey;
    }
    
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    
    public String getPrivateKey() {
        return privateKey;
    }
    
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
    
    /**
     * Vérifie si cet objet contient une clé privée.
     * 
     * @return true si une clé privée est présente, false sinon
     */
    public boolean hasPrivateKey() {
        return privateKey != null && !privateKey.isEmpty();
    }
} 