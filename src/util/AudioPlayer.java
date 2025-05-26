package util;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Classe utilitaire pour gérer la lecture audio.
 * Cette classe suit le principe de responsabilité unique (SRP) en ne gérant
 * que les fonctionnalités liées à la lecture audio.
 */
public class AudioPlayer {
    
    private Clip clip;
    private boolean isPlaying;
    
    /**
     * Joue un enregistrement audio à partir de données brutes.
     * 
     * @param audioData Les données audio à jouer
     * @param audioFormat Le format audio des données
     * @throws LineUnavailableException Si la ligne audio n'est pas disponible
     * @throws IOException Si une erreur d'entrée/sortie se produit
     */
    public void playAudio(byte[] audioData, AudioFormat audioFormat) 
            throws LineUnavailableException, IOException {
        
        if (isPlaying) {
            stopPlayback();
        }
        
        // Création d'un flux d'entrée à partir des données audio
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream ais = new AudioInputStream(bais, audioFormat, audioData.length / audioFormat.getFrameSize());
        
        // Obtention d'une ligne audio pour la lecture
        DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
        
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Le format audio n'est pas supporté");
        }
        
        clip = (Clip) AudioSystem.getLine(info);
        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                isPlaying = false;
                clip.close();
            }
        });
        
        // Ouverture et démarrage de la lecture
        clip.open(ais);
        clip.start();
        isPlaying = true;
    }
    
    /**
     * Arrête la lecture en cours.
     */
    public void stopPlayback() {
        if (clip != null && clip.isOpen()) {
            clip.stop();
            clip.close();
        }
        isPlaying = false;
    }
    
    /**
     * Vérifie si une lecture est en cours.
     * 
     * @return true si une lecture est en cours, false sinon
     */
    public boolean isPlaying() {
        return isPlaying;
    }
} 