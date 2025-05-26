package util;

import javax.sound.sampled.AudioFormat;

/**
 * Classe utilitaire pour gérer le format audio.
 * Cette classe suit le principe de responsabilité unique (SRP) en ne gérant
 * que les fonctionnalités liées au format audio.
 */
public class AudioFormatManager {
    
    // Valeurs par défaut pour le format audio
    private static final float SAMPLE_RATE = 44100.0F;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    
    /**
     * Crée un format audio avec les paramètres par défaut (mono, 16 bits, 44100 Hz).
     * 
     * @return Un format audio prêt à l'utilisation
     */
    public static AudioFormat getDefaultAudioFormat() {
        return new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
    }
    
    /**
     * Calcule la durée en secondes d'un enregistrement audio.
     * 
     * @param audioFormat Le format audio utilisé
     * @param audioDataLength La taille des données audio en octets
     * @return La durée en secondes
     */
    public static int calculateDuration(AudioFormat audioFormat, int audioDataLength) {
        // Calcul du nombre d'échantillons
        long samples = audioDataLength / ((audioFormat.getSampleSizeInBits() / 8) * audioFormat.getChannels());
        
        // Calcul de la durée en secondes
        return (int) (samples / audioFormat.getSampleRate());
    }
    
    /**
     * Écrit un en-tête WAV dans un flux de sortie.
     * 
     * @param out Le flux de sortie
     * @param audioDataLength La taille des données audio en octets
     * @param audioFormat Le format audio utilisé
     * @throws Exception Si une erreur survient lors de l'écriture
     */
    public static void writeWavHeader(java.io.OutputStream out, int audioDataLength, AudioFormat audioFormat) 
            throws Exception {
        // Format de l'en-tête WAV
        int bytesPerSample = audioFormat.getSampleSizeInBits() / 8;
        int bytesPerSecond = (int) (audioFormat.getSampleRate() * bytesPerSample * audioFormat.getChannels());
        
        // Écrit l'identifiant RIFF
        writeString(out, "RIFF");
        
        // Écrit la taille du fichier moins 8 octets
        writeInt(out, 36 + audioDataLength);
        
        // Écrit le format WAVE
        writeString(out, "WAVE");
        
        // Écrit le marqueur fmt
        writeString(out, "fmt ");
        
        // Écrit la taille du chunk format (16 octets)
        writeInt(out, 16);
        
        // Écrit le format audio (1 pour PCM)
        writeShort(out, (short) 1);
        
        // Écrit le nombre de canaux
        writeShort(out, (short) audioFormat.getChannels());
        
        // Écrit la fréquence d'échantillonnage
        writeInt(out, (int) audioFormat.getSampleRate());
        
        // Écrit le débit binaire
        writeInt(out, bytesPerSecond);
        
        // Écrit l'alignement des blocs
        writeShort(out, (short) (bytesPerSample * audioFormat.getChannels()));
        
        // Écrit le nombre de bits par échantillon
        writeShort(out, (short) audioFormat.getSampleSizeInBits());
        
        // Écrit le marqueur data
        writeString(out, "data");
        
        // Écrit la taille des données audio
        writeInt(out, audioDataLength);
    }
    
    /**
     * Écrit une chaîne de caractères dans un flux de sortie.
     */
    private static void writeString(java.io.OutputStream out, String s) throws Exception {
        for (int i = 0; i < s.length(); i++) {
            out.write(s.charAt(i));
        }
    }
    
    /**
     * Écrit un entier 32 bits dans un flux de sortie (little-endian).
     */
    private static void writeInt(java.io.OutputStream out, int val) throws Exception {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
        out.write((val >> 16) & 0xFF);
        out.write((val >> 24) & 0xFF);
    }
    
    /**
     * Écrit un entier 16 bits dans un flux de sortie (little-endian).
     */
    private static void writeShort(java.io.OutputStream out, short val) throws Exception {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
    }
}