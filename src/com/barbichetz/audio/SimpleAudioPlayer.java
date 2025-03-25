package com.barbichetz.audio;

import javax.sound.sampled.*;
import java.io.File;

/**
 * Lecteur audio simplifié pour éviter les problèmes de compilation
 */
public class SimpleAudioPlayer {
    
    /**
     * Joue un fichier audio
     * @param audioFile Fichier audio à lire
     * @return true si la lecture a réussi, false sinon
     */
    public static boolean playAudioFile(File audioFile) {
        try {
            // Obtenir le flux audio
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            
            // Obtenir le format audio
            AudioFormat format = audioInputStream.getFormat();
            
            // Si le format est null, utiliser le format par défaut
            if (format == null) {
                format = AudioFormatManager.getDefaultFormat();
            }
            
            // Ajuster la vitesse de lecture si nécessaire
            if (Math.abs(format.getSampleRate() - AudioFormatManager.SAMPLE_RATE) > 0.1) {
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    AudioFormatManager.SAMPLE_RATE,
                    format.getSampleSizeInBits(),
                    format.getChannels(),
                    format.getFrameSize(),
                    AudioFormatManager.SAMPLE_RATE,
                    format.isBigEndian()
                );
                audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
                format = targetFormat;
            }
            
            // Configurer la ligne de sortie audio
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
            
            // Lire le fichier audio
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                line.write(buffer, 0, bytesRead);
            }
            
            // Fermer les ressources
            line.drain();
            line.stop();
            line.close();
            audioInputStream.close();
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
} 