package com.barbichetz.audio;

import javax.sound.sampled.*;   
import java.io.File;
import java.io.IOException;

/**
 * Gestionnaire de lecture audio
 * Responsable de la lecture des fichiers audio enregistrés
 */
public class AudioPlayer {
    private AudioFormat format;
    private AudioInputStream audioInputStream;
    private SourceDataLine line;
    
    /**
     * Prépare la lecture d'un fichier audio
     * @param audioFile Fichier audio à lire
     * @throws IOException En cas d'erreur d'entrée/sortie
     * @throws UnsupportedAudioFileException Si le format audio n'est pas supporté
     * @throws LineUnavailableException Si la ligne audio n'est pas disponible
     */
    public void prepare(File audioFile) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        // Obtenir le flux audio à partir du fichier
        audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        
        // Obtenir le format audio du fichier
        format = audioInputStream.getFormat();
        
        // Si le format est null pour une raison quelconque, utiliser le format par défaut
        if (format == null) {
            format = AudioFormatManager.getDefaultFormat();
        }
        
        // Ajuster la vitesse de lecture si nécessaire
        audioInputStream = adjustPlaybackSpeed(audioInputStream);
        
        // Configurer la ligne de sortie audio
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
    }
    
    /**
     * Lit le fichier audio
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    public void play() throws IOException {
        if (line == null || format == null) {
            throw new IllegalStateException("La préparation de la lecture n'a pas été effectuée");
        }
        
        line.start();
        
        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        
        while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
            line.write(buffer, 0, bytesRead);
        }
        
        line.drain();
        line.stop();
        line.close();
        audioInputStream.close();
    }

    /**
     * Ajuste la vitesse de lecture en fonction du format source et cible
     * @param sourceFormat Format d'origine
     * @param targetFormat Format cible (pour la lecture)
     * @return Flux audio converti
     */
    private AudioInputStream adjustPlaybackSpeed(AudioInputStream sourceStream) {
        AudioFormat sourceFormat = sourceStream.getFormat();
        
        // Vérifier si la vitesse d'échantillonnage est différente de celle par défaut
        if (Math.abs(sourceFormat.getSampleRate() - AudioFormatManager.SAMPLE_RATE) > 0.1) {
            // Créer un format cible avec la bonne vitesse d'échantillonnage
            AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                AudioFormatManager.SAMPLE_RATE,
                sourceFormat.getSampleSizeInBits(),
                sourceFormat.getChannels(),
                sourceFormat.getFrameSize(),
                AudioFormatManager.SAMPLE_RATE,
                sourceFormat.isBigEndian()
            );
            
            // Convertir le flux audio
            return AudioSystem.getAudioInputStream(targetFormat, sourceStream);
        }
        
        return sourceStream;
    }
}