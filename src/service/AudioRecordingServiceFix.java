package service;

import java.io.*;
import javax.sound.sampled.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.AudioRecording;

/**
 * Version améliorée du service d'enregistrement audio.
 * Ce service corrige les problèmes de bruit dans les enregistrements en
 * utilisant
 * une version améliorée du service de cryptographie.
 */
public class AudioRecordingServiceFix implements AudioRecordingService {
    private static final Logger LOGGER = Logger.getLogger(AudioRecordingServiceFix.class.getName());
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, false);

    private final CryptographyService cryptographyService;
    private SecretKey secretKey;
    private TargetDataLine line;
    private File outputFile;

    /**
     * Constructeur utilisant le service de cryptographie amélioré.
     * 
     * @param cryptographyService Le service de cryptographie à utiliser
     */
    public AudioRecordingServiceFix(CryptographyService cryptographyService) {
        this.cryptographyService = cryptographyService;
        try {
            this.secretKey = cryptographyService.generateSecretKey();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la génération de la clé secrète", e);
        }
    }

    @Override
    public void startRecording(String filename) {
        try {
            // Configuration du format audio et obtention de la ligne d'entrée audio
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
            if (!AudioSystem.isLineSupported(info)) {
                LOGGER.log(Level.SEVERE, "Format audio non supporté");
                return;
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(AUDIO_FORMAT);
            line.start();

            LOGGER.log(Level.INFO, "Début de l'enregistrement dans le fichier: {0}", filename);
            outputFile = new File(filename);

            // Démarrer l'enregistrement dans un thread séparé
            Thread recordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        int bufferSize = 4096;
                        byte[] buffer = new byte[bufferSize];
                        int bytesRead;

                        // Écrire 4 octets pour marquer un fichier valide, facilitant la détection des
                        // erreurs
                        bos.write("AREC".getBytes());

                        while (line.isOpen() && (bytesRead = line.read(buffer, 0, buffer.length)) != -1) {
                            // Chiffrer les données audio avant de les écrire
                            try {
                                byte[] encryptedData = cryptographyService.encrypt(buffer, secretKey);
                                // Écrire d'abord la taille des données chiffrées pour faciliter le
                                // déchiffrement
                                bos.write(intToByteArray(encryptedData.length));
                                bos.write(encryptedData);
                            } catch (Exception e) {
                                LOGGER.log(Level.SEVERE, "Erreur lors du chiffrement", e);
                                // En cas d'erreur, écrire les données non chiffrées pour éviter la perte
                                bos.write(intToByteArray(bytesRead));
                                bos.write(buffer, 0, bytesRead);
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de l'écriture de l'enregistrement", e);
                    } finally {
                        if (line != null && line.isOpen()) {
                            line.close();
                        }
                    }
                    LOGGER.log(Level.INFO, "Fin de l'enregistrement");
                }
            });

            recordingThread.start();
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
    public void playRecording(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                LOGGER.log(Level.SEVERE, "Le fichier d'enregistrement n'existe pas: {0}", filename);
                return;
            }

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                // Vérifier que c'est un fichier d'enregistrement valide
                byte[] header = new byte[4];
                int read = bis.read(header);
                if (read != 4 || !new String(header).equals("AREC")) {
                    LOGGER.log(Level.SEVERE, "Format de fichier d'enregistrement non valide");
                    return;
                }

                // Configuration audio pour la lecture
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
                SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);
                audioLine.open(AUDIO_FORMAT);
                audioLine.start();

                LOGGER.log(Level.INFO, "Lecture de l'enregistrement: {0}", filename);

                // Lire et déchiffrer le fichier en blocs
                byte[] sizeBytes = new byte[4];
                while (bis.read(sizeBytes) == 4) {
                    int dataSize = byteArrayToInt(sizeBytes);
                    byte[] encryptedData = new byte[dataSize];
                    int bytesRead = bis.read(encryptedData);

                    if (bytesRead != dataSize) {
                        LOGGER.log(Level.WARNING, "Données incomplètes lues: {0} au lieu de {1}",
                                new Object[] { bytesRead, dataSize });
                        continue;
                    }

                    try {
                        // Déchiffrer les données et les jouer
                        byte[] decryptedData = cryptographyService.decrypt(encryptedData, secretKey);
                        audioLine.write(decryptedData, 0, decryptedData.length);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors du déchiffrement", e);
                        // En cas d'erreur, essayer de jouer les données chiffrées pour diagnostic
                        audioLine.write(encryptedData, 0, encryptedData.length);
                    }
                }

                audioLine.drain();
                audioLine.close();
                LOGGER.log(Level.INFO, "Fin de la lecture");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la lecture de l'enregistrement", e);
        }
    }

    /**
     * Convertit un entier en tableau d'octets.
     * 
     * @param value L'entier à convertir
     * @return Le tableau d'octets correspondant
     */
    private byte[] intToByteArray(int value) {
        return new byte[] {
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }

    /**
     * Convertit un tableau d'octets en entier.
     * 
     * @param bytes Le tableau d'octets à convertir
     * @return L'entier correspondant
     */
    private int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }

    // Les méthodes suivantes sont les implémentations requises par l'interface
    // AudioRecordingService

    @Override
    public AudioRecording createRecording(String name, LocalDateTime timestamp, int duration, int userId) {
        LOGGER.log(Level.INFO, "Création d'un enregistrement: {0}", name);
        return new AudioRecording(name, timestamp, duration, null, null, null, userId);
    }

    @Override
    public AudioRecording getRecording(int id) {
        LOGGER.log(Level.INFO, "Récupération de l'enregistrement avec ID: {0}", id);
        // Implémenter la récupération d'un enregistrement depuis la base de données
        // Cette fonctionnalité n'est pas encore implémentée dans cette version
        return null;
    }

    @Override
    public int saveRecording(AudioRecording recording) {
        if (recording == null) {
            LOGGER.log(Level.WARNING, "Tentative de sauvegarde d'un enregistrement null");
            return -1;
        }

        LOGGER.log(Level.INFO, "Sauvegarde de l'enregistrement: {0}", recording.getName());
        // Implémenter la sauvegarde d'un enregistrement dans la base de données
        // Cette fonctionnalité n'est pas encore implémentée dans cette version
        return 1;
    }

    @Override
    public List<AudioRecording> getRecordings(String directory) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRecordings'");
    }

    @Override
    public boolean deleteRecording(int recordingId) throws Exception {
        // Implémentation minimale
        LOGGER.warning("Suppression non implémentée dans AudioRecordingServiceFix");
        return false;
    }

    @Override
    public boolean isRecording() {
        return line != null && line.isOpen();
    }

    @Override
    public boolean isPlaying() {
        // À adapter selon la logique de lecture
        return false;
    }

    @Override
    public void stopPlaying() {
        // Implémentation minimale
        LOGGER.warning("Arrêt de la lecture non implémenté dans AudioRecordingServiceFix");
    }
}