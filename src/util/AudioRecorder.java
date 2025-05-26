package util;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Classe utilitaire pour gérer l'enregistrement audio.
 * Cette classe suit le principe de responsabilité unique (SRP) en ne gérant
 * que les fonctionnalités liées à l'enregistrement audio.
 */
public class AudioRecorder {
    
    private TargetDataLine line;
    private AudioFormat format;
    private Thread recordingThread;
    private boolean isRecording;
    private ByteArrayOutputStream outputStream;
    
    /**
     * Constructeur qui initialise l'enregistreur avec un format audio par défaut.
     */
    public AudioRecorder() {
        this.format = AudioFormatManager.getDefaultAudioFormat();
        this.outputStream = new ByteArrayOutputStream();
    }
    
    /**
     * Constructeur qui initialise l'enregistreur avec un format audio spécifique.
     * 
     * @param format Le format audio à utiliser
     */
    public AudioRecorder(AudioFormat format) {
        this.format = format;
        this.outputStream = new ByteArrayOutputStream();
    }
    
    /**
     * Démarre l'enregistrement audio.
     * 
     * @throws LineUnavailableException Si la ligne audio n'est pas disponible
     */
    public void startRecording() throws LineUnavailableException {
        if (isRecording) {
            return;
        }
        
        // Réinitialisation du flux de sortie
        outputStream = new ByteArrayOutputStream();
        
        // Obtention d'une ligne audio pour l'enregistrement
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Le format audio n'est pas supporté pour l'enregistrement");
        }
        
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        
        isRecording = true;
        
        // Démarrage du thread d'enregistrement
        recordingThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while (isRecording) {
                bytesRead = line.read(buffer, 0, buffer.length);
                
                if (bytesRead > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        });
        
        recordingThread.start();
    }
    
    /**
     * Arrête l'enregistrement audio.
     * 
     * @return Les données audio enregistrées
     * @throws InterruptedException Si le thread est interrompu
     */
    public byte[] stopRecording() throws InterruptedException {
        if (!isRecording) {
            return outputStream.toByteArray();
        }
        
        isRecording = false;
        
        line.stop();
        line.close();
        
        // Attente de la fin du thread d'enregistrement
        recordingThread.join();
        
        return outputStream.toByteArray();
    }
    
    /**
     * Vérifie si un enregistrement est en cours.
     * 
     * @return true si un enregistrement est en cours, false sinon
     */
    public boolean isRecording() {
        return isRecording;
    }
    
    /**
     * Retourne le format audio utilisé pour l'enregistrement.
     * 
     * @return Le format audio
     */
    public AudioFormat getAudioFormat() {
        return format;
    }
} 