package service.impl;

import service.AudioRecordingService;
import service.CryptographyService;
import model.AudioRecording;

import javax.crypto.SecretKey;
import javax.sound.sampled.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AudioRecordingServiceFix implements AudioRecordingService {
    private static final Logger LOGGER = Logger.getLogger(AudioRecordingServiceFix.class.getName());
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);
    
    private final CryptographyService cryptographyService;
    private SecretKey secretKey;
    private TargetDataLine line;
    private File outputFile;
    private boolean isPlaying = false;
    
    public AudioRecordingServiceFix(CryptographyService cryptographyService) {
        this.cryptographyService = cryptographyService;
        try {
            this.secretKey = cryptographyService.generateSecretKey();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la génération de la clé secrète", e);
        }
    }

    @Override
    public void startRecording(String fileName) {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
            if (!AudioSystem.isLineSupported(info)) {
                LOGGER.log(Level.SEVERE, "Format audio non supporté");
                return;
            }
            
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(AUDIO_FORMAT);
            line.start();
            
            LOGGER.log(Level.INFO, "Début de l'enregistrement dans le fichier: {0}", fileName);
            outputFile = new File(fileName);
            
        } catch (LineUnavailableException e) {
            LOGGER.log(Level.SEVERE, "Impossible d'accéder à la ligne audio", e);
        }
    }

    @Override
    public void stopRecording() {
        if (line != null && line.isOpen()) {
            line.stop();
            line.close();
            LOGGER.log(Level.INFO, "Enregistrement arrêté");
        }
    }

    @Override
    public void playRecording(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                LOGGER.log(Level.SEVERE, "Le fichier d'enregistrement n'existe pas: {0}", fileName);
                return;
            }
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioInputStream.getFormat());
            
            if (!AudioSystem.isLineSupported(info)) {
                LOGGER.log(Level.SEVERE, "Format audio non supporté");
                return;
            }
            
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioInputStream.getFormat());
            line.start();
            
            isPlaying = true;
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                line.write(buffer, 0, bytesRead);
            }
            
            line.drain();
            line.close();
            audioInputStream.close();
            
            isPlaying = false;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la lecture de l'enregistrement", e);
            isPlaying = false;
        }
    }

    @Override
    public void stopPlaying() {
        isPlaying = false;
    }

    @Override
    public int saveRecording(AudioRecording recording) throws Exception {
        // TODO: Implémenter la sauvegarde dans la base de données
        return 1;
    }

    @Override
    public AudioRecording getRecording(int recordingId) throws Exception {
        // TODO: Implémenter la récupération depuis la base de données
        return null;
    }

    @Override
    public AudioRecording createRecording(String name, LocalDateTime timestamp, int duration, int userId) {
        return new AudioRecording(0, name, "", timestamp, duration, userId);
    }

    @Override
    public List<AudioRecording> getRecordings(String directory) {
        return new ArrayList<>();
    }

    @Override
    public boolean deleteRecording(int recordingId) throws Exception {
        // TODO: Implémenter la suppression
        return false;
    }

    @Override
    public boolean isRecording() {
        return line != null && line.isOpen();
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }
} 