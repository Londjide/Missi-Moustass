package model;

import java.time.LocalDateTime;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * Classe représentant un enregistrement audio.
 * Cette classe stocke les métadonnées associées à un fichier audio enregistré.
 */
public class AudioRecording {
    private int id;
    private String name;
    private String filePath;
    private LocalDateTime timestamp;
    private int duration; // en secondes
    private int userId;
    private byte[] audioData;
    private String encryptionKey;
    private String integrityHash;
    
    /**
     * Constructeur pour un nouvel enregistrement.
     * 
     * @param name      Le nom de l'enregistrement
     * @param filePath  Le chemin vers le fichier
     * @param timestamp La date et l'heure de l'enregistrement
     */
    public AudioRecording(String name, String filePath, LocalDateTime timestamp) {
        this.name = name;
        this.filePath = filePath;
        this.timestamp = timestamp;
        this.duration = 0; // durée inconnue par défaut
    }
    
    /**
     * Constructeur complet pour un enregistrement existant.
     * 
     * @param id        L'identifiant unique de l'enregistrement
     * @param name      Le nom de l'enregistrement
     * @param filePath  Le chemin vers le fichier
     * @param timestamp La date et l'heure de l'enregistrement
     * @param duration  La durée de l'enregistrement en secondes
     * @param userId    L'identifiant de l'utilisateur propriétaire
     */
    public AudioRecording(int id, String name, String filePath, LocalDateTime timestamp, int duration, int userId) {
        this.id = id;
        this.name = name;
        this.filePath = filePath;
        this.timestamp = timestamp;
        this.duration = duration;
        this.userId = userId;
        // La clé sera définie plus tard via setEncryptionKey
    }
    
    /**
     * Constructeur pour un enregistrement avec données chiffrées.
     * 
     * @param name          Le nom de l'enregistrement
     * @param timestamp     La date et l'heure de l'enregistrement
     * @param duration      La durée de l'enregistrement en secondes
     * @param audioData     Les données audio chiffrées
     * @param encryptionKey La clé de chiffrement en Base64
     * @param integrityHash Le hash d'intégrité
     * @param userId        L'identifiant de l'utilisateur propriétaire
     */
    public AudioRecording(String name, LocalDateTime timestamp, int duration, 
                         byte[] audioData, String encryptionKey, String integrityHash, int userId) {
        this.name = name;
        this.timestamp = timestamp;
        this.duration = duration;
        this.audioData = audioData;
        this.encryptionKey = encryptionKey;
        this.integrityHash = integrityHash;
        this.userId = userId;
    }
    
    // Getters et setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public int getUserId() {
        return userId;
    }
    
    /**
     * Modifie l'ID de l'utilisateur propriétaire.
     * Cette méthode est utilisée uniquement pour les enregistrements partagés.
     * 
     * @param userId Le nouvel ID utilisateur
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public byte[] getAudioData() {
        return audioData;
    }
    
    public void setAudioData(byte[] audioData) {
        this.audioData = audioData;
    }
    
    public String getEncryptionKey() {
        return encryptionKey;
    }
    
    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
    
    public String getIntegrityHash() {
        return integrityHash;
    }
    
    public void setIntegrityHash(String integrityHash) {
        this.integrityHash = integrityHash;
    }
    
    @Override
    public String toString() {
        return name + " (" + timestamp + ")";
    }
} 