package com.barbichetz.audio;

import javax.sound.sampled.AudioFormat;

/**
 * Gestionnaire des formats audio
 * Responsable de créer et maintenir les formats audio utilisés dans l'application
 */
public class AudioFormatManager {
    // Constantes pour les paramètres audio
    public static final float SAMPLE_RATE = 44100.0F;
    public static final int SAMPLE_SIZE = 16;
    public static final int CHANNELS = 1;
    public static final boolean SIGNED = true;
    public static final boolean BIG_ENDIAN = false;
    
    private static AudioFormat defaultFormat;
    
    /**
     * Initialise et retourne le format audio par défaut
     * @return Format audio configuré pour l'enregistrement et la lecture
     */
    public static AudioFormat getDefaultFormat() {
        if (defaultFormat == null) {
            defaultFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, SIGNED, BIG_ENDIAN);
        }
        return defaultFormat;
    }
    
    /**
     * Crée un format audio avec les paramètres spécifiés
     * @param sampleRate Taux d'échantillonnage en Hz
     * @param sampleSize Taille des échantillons en bits
     * @param channels Nombre de canaux (1=mono, 2=stéréo)
     * @param signed Échantillons signés ou non
     * @param bigEndian Ordre des octets
     * @return Format audio configuré
     */
    public static AudioFormat createFormat(float sampleRate, int sampleSize, int channels, 
                                          boolean signed, boolean bigEndian) {
        return new AudioFormat(sampleRate, sampleSize, channels, signed, bigEndian);
    }
}
