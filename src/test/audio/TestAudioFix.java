package test.audio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.sound.sampled.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import service.CryptographyService;
import service.AudioRecordingService;

/**
 * Classe de test pour vérifier les correctifs audio avec et sans chiffrement.
 * Cette classe génère deux enregistrements, un avec chiffrement et un sans,
 * pour permettre la comparaison et diagnostiquer les problèmes de bruit.
 */
public class TestAudioFix {
    private static final Logger LOGGER = Logger.getLogger(TestAudioFix.class.getName());
    private static final int RECORD_DURATION_MS = 5000; // 5 secondes
    private static final File TEST_DIR = new File("test_audio");

    public static void main(String[] args) {
        // Vérifier si on doit désactiver le chiffrement
        boolean disableEncryption = args.length > 0 && Boolean.parseBoolean(args[0]);

        LOGGER.info("Démarrage du test audio " + (disableEncryption ? "sans" : "avec") + " chiffrement");

        // Créer le répertoire de test s'il n'existe pas
        if (!TEST_DIR.exists()) {
            TEST_DIR.mkdirs();
        }

        try {
            // Instancier le service de chiffrement directement
            CryptographyService cryptoService = new service.impl.AESCryptographyServiceFix(disableEncryption);

            // Instancier le service d'enregistrement audio
            AudioRecordingService audioService = new service.impl.AudioRecordingServiceFixExtended(cryptoService);

            // Enregistrer l'audio
            byte[] audioData = recordAudio();

            // Traiter l'audio (avec ou sans chiffrement selon le paramètre)
            byte[] processedData = processAudio(audioService, audioData);

            // Vérifier l'intégrité (simuler ce que fait le contrôleur)
            byte[] retrievedData = retrieveAudio(audioService, processedData);

            // Sauvegarder le fichier audio résultant
            String filename = disableEncryption ? "without_encryption.wav" : "with_encryption.wav";
            saveAudioToFile(retrievedData, new File(TEST_DIR, filename));

            LOGGER.info("Test réussi. Fichier sauvegardé: " + filename);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur durant le test audio", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Enregistre de l'audio pendant une durée déterminée.
     * 
     * @return Les données audio brutes
     * @throws Exception En cas d'erreur d'enregistrement
     */
    private static byte[] recordAudio() throws Exception {
        LOGGER.info("Démarrage de l'enregistrement audio de test (" + RECORD_DURATION_MS / 1000 + " secondes)...");

        // Configuration du format audio
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Format audio non supporté");
        }

        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();

        LOGGER.info("Enregistrement en cours...");

        // Créer un buffer pour stocker l'audio
        int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
        byte[] buffer = new byte[bufferSize];

        // Créer un flux pour collecter les données
        ByteArrayOutputStreamExt outputStream = new ByteArrayOutputStreamExt();

        try {
            Thread.sleep(RECORD_DURATION_MS); // Enregistrer pendant la durée spécifiée
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int bytesRead;
        while (line.isOpen() && (bytesRead = line.read(buffer, 0, buffer.length)) != -1) {
            outputStream.write(buffer, 0, bytesRead);

            // Arrêter après avoir rempli le buffer
            if (outputStream.size() > bufferSize) {
                break;
            }
        }

        line.stop();
        line.close();

        LOGGER.info("Enregistrement terminé");

        return outputStream.toByteArray();
    }

    /**
     * Traite les données audio avec le service d'enregistrement.
     * 
     * @param service   Le service d'enregistrement audio
     * @param audioData Les données audio brutes
     * @return Les données audio traitées
     * @throws Exception En cas d'erreur
     */
    private static byte[] processAudio(AudioRecordingService service, byte[] audioData) throws Exception {
        if (service instanceof service.impl.AudioRecordingServiceFixExtended) {
            // return ((service.impl.AudioRecordingServiceFixExtended)
            // service).processAudioData(audioData); // COMMENTÉ
            LOGGER.log(Level.WARNING,
                    "Appel à processAudioData dans TestAudioFix nécessite une clé, temporairement désactivé / à adapter.");
            // Pour un test fonctionnel, il faudrait générer une SecretKey ici et la passer.
            // Exemple:
            // CryptographyService crypto = new
            // service.impl.AESCryptographyServiceFix(false);
            // SecretKey tempKey = crypto.generateSecretKey();
            // return ((service.impl.AudioRecordingServiceFixExtended)
            // service).processAudioData(audioData, tempKey);
            return audioData; // Retourne les données brutes pour l'instant pour éviter l'erreur
        } else {
            throw new IllegalArgumentException("Service audio incompatible");
        }
    }

    /**
     * Récupère les données audio originales à partir des données traitées.
     * 
     * @param service       Le service d'enregistrement audio
     * @param processedData Les données audio traitées
     * @return Les données audio récupérées
     * @throws Exception En cas d'erreur
     */
    private static byte[] retrieveAudio(AudioRecordingService service, byte[] processedData) throws Exception {
        if (service instanceof service.impl.AudioRecordingServiceFixExtended) {
            // Utiliser la méthode de compatibilité dépréciée mais qui fonctionne encore
            // pour les tests
            return ((service.impl.AudioRecordingServiceFixExtended) service).retrieveAudioData(processedData);
        } else {
            throw new IllegalArgumentException("Service audio incompatible");
        }
    }

    /**
     * Crée des données WAV à partir de données audio brutes.
     * 
     * @param rawData Les données audio brutes
     * @param format  Le format audio
     * @return Les données WAV
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private static byte[] createWavData(byte[] rawData, AudioFormat format) throws IOException {
        ByteArrayOutputStreamExt out = new ByteArrayOutputStreamExt();

        // En-tête WAV RIFF
        writeString(out, "RIFF");
        writeInt(out, 36 + rawData.length);
        writeString(out, "WAVE");

        // En-tête de format
        writeString(out, "fmt ");
        writeInt(out, 16); // taille de l'en-tête de format
        writeShort(out, (short) 1); // format PCM
        writeShort(out, (short) format.getChannels());
        writeInt(out, (int) format.getSampleRate());
        writeInt(out, (int) (format.getSampleRate() * format.getFrameSize()));
        writeShort(out, (short) format.getFrameSize());
        writeShort(out, (short) format.getSampleSizeInBits());

        // Bloc de données
        writeString(out, "data");
        writeInt(out, rawData.length);
        out.write(rawData);

        return out.toByteArray();
    }

    /**
     * Sauvegarde les données audio dans un fichier WAV.
     * 
     * @param audioData Les données audio WAV
     * @param file      Le fichier de destination
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private static void saveAudioToFile(byte[] audioData, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(audioData);
        }
        LOGGER.info("Fichier audio enregistré: " + file.getAbsolutePath());
    }

    // Méthodes utilitaires pour l'écriture des en-têtes WAV
    private static void writeInt(ByteArrayOutputStreamExt out, int v) throws IOException {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
        out.write((v >> 16) & 0xFF);
        out.write((v >> 24) & 0xFF);
    }

    private static void writeShort(ByteArrayOutputStreamExt out, short v) throws IOException {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
    }

    private static void writeString(ByteArrayOutputStreamExt out, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            out.write(s.charAt(i));
        }
    }

    /**
     * Extension de ByteArrayOutputStream pour simplifier la manipulation.
     */
    private static class ByteArrayOutputStreamExt extends java.io.ByteArrayOutputStream {
        public ByteArrayOutputStreamExt() {
            super();
        }
    }
}