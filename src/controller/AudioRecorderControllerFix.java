package controller;

import model.AudioRecording;
import service.AudioRecordingService;
import service.impl.AudioRecordingServiceFixExtended;
import service.impl.AESCryptographyServiceFix;
import util.AudioFormatManager;
import util.AudioPlayer;
import util.AudioRecorder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Version corrigée du contrôleur pour gérer les opérations d'enregistrement audio.
 * Cette classe utilise explicitement l'implémentation corrigée du service d'enregistrement audio.
 */
public class AudioRecorderControllerFix {
    
    private final AudioRecordingService audioRecordingService;
    private final AudioRecorder audioRecorder;
    private final AudioPlayer audioPlayer;
    private final int userId;
    
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    /**
     * Constructeur qui initialise le contrôleur avec un ID utilisateur spécifique.
     * 
     * @param userId L'ID de l'utilisateur connecté
     * @param disableEncryption true pour désactiver le chiffrement (pour le débogage), false sinon
     */
    public AudioRecorderControllerFix(int userId, boolean disableEncryption) {
        this.userId = userId;
        
        // Utilisation explicite de l'implémentation corrigée
        AESCryptographyServiceFix cryptoService = new AESCryptographyServiceFix(disableEncryption);
        this.audioRecordingService = new AudioRecordingServiceFixExtended(cryptoService);
        
        this.audioRecorder = new AudioRecorder();
        this.audioPlayer = new AudioPlayer();
    }
    
    /**
     * Constructeur qui initialise le contrôleur avec un ID utilisateur spécifique.
     * Par défaut, le chiffrement est activé.
     * 
     * @param userId L'ID de l'utilisateur connecté
     */
    public AudioRecorderControllerFix(int userId) {
        this(userId, false);
    }
    
    /**
     * Démarre l'enregistrement audio.
     * 
     * @throws LineUnavailableException Si la ligne audio n'est pas disponible
     */
    public void startRecording() throws LineUnavailableException {
        audioRecorder.startRecording();
    }
    
    /**
     * Arrête l'enregistrement audio et sauvegarde l'enregistrement.
     * 
     * @param name Le nom à donner à l'enregistrement
     * @return true si l'enregistrement a été sauvegardé avec succès, false sinon
     */
    public boolean stopAndSaveRecording(String name) {
        if (!audioRecorder.isRecording()) {
            return false;
        }
        
        try {
            // Arrêt de l'enregistrement et récupération des données
            byte[] audioData = audioRecorder.stopRecording();
            
            if (audioData.length == 0) {
                return false;
            }
            
            // Calcul de la durée
            AudioFormat format = audioRecorder.getAudioFormat();
            int duration = AudioFormatManager.calculateDuration(format, audioData.length);
            
            // Création de l'enregistrement avec chiffrement
            AudioRecording recording = audioRecordingService.createRecording(
                    name,
                    LocalDateTime.now(),
                    duration,
                    userId
            );
            
            // Sauvegarde dans la base de données
            int recordingId = audioRecordingService.saveRecording(recording);
            
            return recordingId > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Joue un enregistrement audio.
     * 
     * @param recordingId L'ID de l'enregistrement à jouer
     * @return true si la lecture a commencé, false sinon
     */
    public boolean playRecording(int recordingId) {
        if (audioPlayer.isPlaying()) {
            stopPlayback();
        }
        
        try {
            // Récupération de l'enregistrement
            AudioRecording recording = audioRecordingService.getRecording(recordingId);
            
            if (recording == null) {
                return false;
            }
            
            // Lecture de l'enregistrement
            audioPlayer.playAudio(recording.getAudioData(), AudioFormatManager.getDefaultAudioFormat());
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Arrête la lecture en cours.
     */
    public void stopPlayback() {
        audioPlayer.stopPlayback();
    }
    
    /**
     * Vérifie si un enregistrement est en cours.
     * 
     * @return true si un enregistrement est en cours, false sinon
     */
    public boolean isRecording() {
        return audioRecorder.isRecording();
    }
    
    /**
     * Vérifie si une lecture est en cours.
     * 
     * @return true si une lecture est en cours, false sinon
     */
    public boolean isPlaying() {
        return audioPlayer.isPlaying();
    }
    
    /**
     * Formate une date pour l'affichage.
     * 
     * @param dateTime La date à formater
     * @return La date formatée
     */
    public String formatDateForDisplay(LocalDateTime dateTime) {
        return dateTime.format(DISPLAY_FORMATTER);
    }
    
    /**
     * Formate une durée en secondes en format MM:SS.
     * 
     * @param durationInSeconds La durée en secondes
     * @return La durée formatée
     */
    public String formatDuration(int durationInSeconds) {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Teste l'enregistrement et la lecture avec une implémentation sans chiffrement.
     * Utile pour isoler les problèmes de bruit liés au chiffrement.
     * 
     * @param filename Le nom du fichier pour l'enregistrement de test
     * @return true si le test a réussi, false sinon
     */
    public boolean testWithoutEncryption(String filename) {
        try {
            AudioRecorderControllerFix testController = new AudioRecorderControllerFix(userId, true);
            
            // Créer un enregistrement sans chiffrement
            testController.startRecording();
            Thread.sleep(3000); // 3 secondes d'enregistrement
            boolean recorded = testController.stopAndSaveRecording(filename);
            
            if (!recorded) {
                return false;
            }
            
            // Attendre un peu
            Thread.sleep(1000);
            
            // Lire l'enregistrement
            boolean played = testController.playRecording(1);
            
            return played;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 