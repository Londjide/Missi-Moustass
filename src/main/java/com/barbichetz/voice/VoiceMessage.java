package com.barbichetz.voice;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe représentant un message vocal dans le système
 */
public class VoiceMessage {
    private final UUID id;
    private final byte[] audioData;
    private final LocalDateTime timestamp;
    private final String sender;
    private final String recipient;
    
    public VoiceMessage(byte[] audioData, String sender, String recipient) {
        this.id = UUID.randomUUID();
        this.audioData = audioData;
        this.timestamp = LocalDateTime.now();
        this.sender = sender;
        this.recipient = recipient;
    }
    
    // Getters
    public UUID getId() { return id; }
    public byte[] getAudioData() { return audioData; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
} 