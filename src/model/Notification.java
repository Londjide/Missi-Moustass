package model;

import java.time.LocalDateTime;

/**
 * Modèle représentant une notification pour un utilisateur.
 * Les notifications sont générées lors du partage d'enregistrements.
 */
public class Notification {
    private int id;
    private int userId;
    private String message;
    private boolean isRead;
    private LocalDateTime timestamp;
    private int recordingId;

    /**
     * Constructeur pour une nouvelle notification (sans ID)
     */
    public Notification(int userId, String message, boolean isRead, LocalDateTime timestamp, int recordingId) {
        this.userId = userId;
        this.message = message;
        this.isRead = isRead;
        this.timestamp = timestamp;
        this.recordingId = recordingId;
    }

    /**
     * Constructeur pour une notification existante avec ID
     */
    public Notification(int id, int userId, String message, boolean isRead, LocalDateTime timestamp, int recordingId) {
        this(userId, message, isRead, timestamp, recordingId);
        this.id = id;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getRecordingId() {
        return recordingId;
    }

    public void setRecordingId(int recordingId) {
        this.recordingId = recordingId;
    }
}