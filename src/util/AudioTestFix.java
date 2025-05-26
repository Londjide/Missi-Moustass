package util;

import service.impl.AESCryptographyServiceFix;
import service.CryptographyService;

import javax.crypto.SecretKey;
import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Classe de test pour comparer les enregistrements avec et sans chiffrement.
 * Cette classe permet d'isoler le problème de bruit dans les enregistrements audio.
 */
public class AudioTestFix {
    
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);
    private static final String OUTPUT_DIR = "recordings";
    private static final String TEST_PREFIX = "Test_";
    
    /**
     * Point d'entrée principal.
     * 
     * @param args Les arguments de la ligne de commande
     */
    public static void main(String[] args) {
        System.out.println("=== Test de comparaison des enregistrements audio ===");
        
        try {
            // Créer le service de cryptographie avec le chiffrement désactivé
            CryptographyService cryptoOff = new AESCryptographyServiceFix(false);
            
            // Créer le service de cryptographie avec le chiffrement activé
            CryptographyService cryptoOn = new AESCryptographyServiceFix(true);
            
            // Générer une clé secrète pour le test
            SecretKey key = cryptoOn.generateSecretKey();
            
            // Tester l'enregistrement avec différentes configurations
            testRecording(cryptoOff, key, false);
            testRecording(cryptoOn, key, true);
            
            // Comparer les fichiers d'enregistrement
            compareFiles();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Teste l'enregistrement audio avec un service de cryptographie spécifique.
     * 
     * @param crypto Le service de cryptographie à utiliser
     * @param key La clé secrète à utiliser
     * @param encrypted Si l'enregistrement doit être chiffré
     */
    private static void testRecording(CryptographyService crypto, SecretKey key, boolean encrypted) {
        String suffix = encrypted ? "chiffre" : "nonchiffre";
        String filename = OUTPUT_DIR + File.separator + TEST_PREFIX + 
                          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss")) + 
                          "_" + suffix + ".wav";
        
        System.out.println("\nTest d'enregistrement " + (encrypted ? "avec" : "sans") + " chiffrement");
        System.out.println("Fichier: " + filename);
        
        try {
            // Créer un fichier de test avec du contenu audio
            generateTestFile(filename, crypto, key, encrypted);
            
            System.out.println("Fichier de test créé: " + filename);
            System.out.println("Taille du fichier: " + new File(filename).length() + " octets");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test d'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Génère un fichier de test avec du contenu audio.
     * 
     * @param filename Le nom du fichier à créer
     * @param crypto Le service de cryptographie à utiliser
     * @param key La clé secrète à utiliser
     * @param encrypt Si le contenu doit être chiffré
     * @throws Exception Si une erreur survient lors de la génération
     */
    private static void generateTestFile(String filename, CryptographyService crypto, SecretKey key, boolean encrypt) throws Exception {
        // Générer des données audio de test (une onde sinusoïdale)
        byte[] audioData = generateSineWave(2, 440);
        
        // Créer le répertoire de sortie si nécessaire
        Files.createDirectories(Paths.get(OUTPUT_DIR));
        
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename))) {
            // Écrire l'en-tête du fichier WAV
            writeWavHeader(bos, audioData.length);
            
            // Si le chiffrement est activé, chiffrer les données
            if (encrypt) {
                byte[] encryptedData = crypto.encrypt(audioData, key);
                bos.write(encryptedData);
            } else {
                bos.write(audioData);
            }
        }
    }
    
    /**
     * Génère une onde sinusoïdale pour les tests.
     * 
     * @param durationSeconds La durée en secondes
     * @param frequency La fréquence en Hz
     * @return Les données audio générées
     */
    private static byte[] generateSineWave(int durationSeconds, double frequency) {
        int sampleRate = (int) AUDIO_FORMAT.getSampleRate();
        int numSamples = durationSeconds * sampleRate;
        byte[] buffer = new byte[numSamples * 2]; // 16 bits = 2 bytes per sample
        
        double amplitude = 32760; // Just below maximum for 16-bit audio
        
        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            double angle = 2.0 * Math.PI * frequency * time;
            short sample = (short) (amplitude * Math.sin(angle));
            
            // Write the sample to the buffer (little endian)
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        return buffer;
    }
    
    /**
     * Écrit l'en-tête d'un fichier WAV.
     * 
     * @param bos Le flux de sortie
     * @param dataLength La longueur des données audio
     * @throws IOException Si une erreur survient lors de l'écriture
     */
    private static void writeWavHeader(BufferedOutputStream bos, int dataLength) throws IOException {
        // RIFF header
        bos.write("RIFF".getBytes());
        bos.write(intToLittleEndian(36 + dataLength)); // File size - 8
        bos.write("WAVE".getBytes());
        
        // Format chunk
        bos.write("fmt ".getBytes());
        bos.write(intToLittleEndian(16)); // Chunk size
        bos.write(shortToLittleEndian((short) 1)); // Audio format (PCM)
        bos.write(shortToLittleEndian((short) 1)); // Channels
        bos.write(intToLittleEndian((int) AUDIO_FORMAT.getSampleRate())); // Sample rate
        bos.write(intToLittleEndian((int) (AUDIO_FORMAT.getSampleRate() * AUDIO_FORMAT.getFrameSize()))); // Byte rate
        bos.write(shortToLittleEndian((short) AUDIO_FORMAT.getFrameSize())); // Block align
        bos.write(shortToLittleEndian((short) AUDIO_FORMAT.getSampleSizeInBits())); // Bits per sample
        
        // Data chunk
        bos.write("data".getBytes());
        bos.write(intToLittleEndian(dataLength)); // Chunk size
    }
    
    /**
     * Convertit un entier en tableau d'octets (little endian).
     * 
     * @param value L'entier à convertir
     * @return Le tableau d'octets
     */
    private static byte[] intToLittleEndian(int value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF),
            (byte) ((value >> 16) & 0xFF),
            (byte) ((value >> 24) & 0xFF)
        };
    }
    
    /**
     * Convertit un short en tableau d'octets (little endian).
     * 
     * @param value Le short à convertir
     * @return Le tableau d'octets
     */
    private static byte[] shortToLittleEndian(short value) {
        return new byte[] {
            (byte) (value & 0xFF),
            (byte) ((value >> 8) & 0xFF)
        };
    }
    
    /**
     * Compare les fichiers d'enregistrement générés.
     */
    private static void compareFiles() {
        System.out.println("\n=== Comparaison des fichiers ===");
        File dir = new File(OUTPUT_DIR);
        File[] files = dir.listFiles((d, name) -> name.startsWith(TEST_PREFIX));
        
        if (files == null || files.length < 2) {
            System.out.println("Pas assez de fichiers de test pour comparer.");
            return;
        }
        
        for (File file : files) {
            System.out.println("Fichier: " + file.getName() + ", Taille: " + file.length() + " octets");
        }
        
        System.out.println("\nRésultat du test:");
        System.out.println("Si la différence de taille est importante, le chiffrement modifie significativement les données.");
        System.out.println("Pour résoudre le problème de bruit, utilisez AESCryptographyServiceFix avec chiffrement désactivé.");
    }
} 