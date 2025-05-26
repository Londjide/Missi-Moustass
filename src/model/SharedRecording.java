package model;

import java.time.LocalDateTime;

/**
 * Classe représentant un enregistrement audio partagé entre utilisateurs.
 * Cette classe stocke les informations sur un enregistrement partagé,
 * y compris les identifiants des utilisateurs source et cible.
 */
public class SharedRecording {
    private int id;
    private int recordingId;
    private int sourceUserId;
    private int targetUserId;
    private LocalDateTime sharedDate;
    private String encryptionKey;

    /**
     * Constructeur pour un nouvel enregistrement partagé.
     * 
     * @param recordingId   L'identifiant de l'enregistrement
     * @param sourceUserId  L'identifiant de l'utilisateur qui partage
     * @param targetUserId  L'identifiant de l'utilisateur destinataire
     * @param encryptionKey La clé de l'enregistrement chiffrée avec la clé publique
     *                      du destinataire
     */
    public SharedRecording(int recordingId, int sourceUserId, int targetUserId, String encryptionKey) {
        this.recordingId = recordingId;
        this.sourceUserId = sourceUserId;
        this.targetUserId = targetUserId;
        this.sharedDate = LocalDateTime.now();
        this.encryptionKey = encryptionKey;
    }

    /**
     * Constructeur complet pour un enregistrement partagé existant.
     * 
     * @param id            L'identifiant unique du partage
     * @param recordingId   L'identifiant de l'enregistrement
     * @param sourceUserId  L'identifiant de l'utilisateur qui partage
     * @param targetUserId  L'identifiant de l'utilisateur destinataire
     * @param sharedDate    La date du partage
     * @param encryptionKey La clé de l'enregistrement chiffrée avec la clé publique
     *                      du destinataire
     */
    public SharedRecording(int id, int recordingId, int sourceUserId, int targetUserId,
            LocalDateTime sharedDate, String encryptionKey) {
        this.id = id;
        this.recordingId = recordingId;
        this.sourceUserId = sourceUserId;
        this.targetUserId = targetUserId;
        this.sharedDate = sharedDate;
        this.encryptionKey = encryptionKey;
    }

    // Getters et setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(int recordingId) {
        this.recordingId = recordingId;
    }

    public int getSourceUserId() {
        return sourceUserId;
    }

    public void setSourceUserId(int sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public int getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(int targetUserId) {
        this.targetUserId = targetUserId;
    }

    public LocalDateTime getSharedDate() {
        return sharedDate;
    }

    public void setSharedDate(LocalDateTime sharedDate) {
        this.sharedDate = sharedDate;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
}