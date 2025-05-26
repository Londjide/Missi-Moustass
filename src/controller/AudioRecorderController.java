package controller;

import model.AudioRecording;
import service.AudioRecordingService;
import service.CryptographyService;
import service.UserService;
import service.UserKeysService;
import service.RSACryptographyService;
import service.SharedRecordingService;
import service.impl.AudioRecordingServiceFixExtended;
import service.impl.CryptographyServiceFix;
import service.impl.UserServiceImplFix;
import model.User;
import model.UserKeys;
import util.ServiceFactory;

import javax.sound.sampled.AudioFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Contrôleur pour gérer les opérations d'enregistrement audio.
 * Cette classe suit le principe de responsabilité unique (SRP) en gérant
 * uniquement
 * la logique de contrôle pour les enregistrements audio.
 */
public class AudioRecorderController {

    private static final Logger LOGGER = Logger.getLogger(AudioRecorderController.class.getName());

    private int userId;
    private final AudioRecordingService audioRecordingService;
    private PlaybackListener playbackListener;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private final UserService userService;
    private final SharedRecordingService sharedRecordingService;
    private final UserKeysService userKeysService;
    private final RSACryptographyService rsaCryptographyService;

    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Constructeur qui initialise le contrôleur avec un ID utilisateur spécifique.
     * 
     * @param userId L'ID de l'utilisateur connecté
     */
    public AudioRecorderController(int userId) {
        this.userId = userId;
        this.audioRecordingService = new AudioRecordingServiceFixExtended(new CryptographyServiceFix());
        this.userService = ServiceFactory.getInstance().getUserService();
        this.rsaCryptographyService = new RSACryptographyService();
        this.userKeysService = new UserKeysService(ServiceFactory.getInstance().getDatabaseService(),
                rsaCryptographyService);
        this.sharedRecordingService = new SharedRecordingService(
                ServiceFactory.getInstance().getDatabaseService(),
                userService,
                audioRecordingService,
                userKeysService,
                rsaCryptographyService);
        updateUserId(userId);
    }

    /**
     * Met à jour l'ID de l'utilisateur courant.
     * 
     * @param newUserId Le nouvel ID de l'utilisateur
     */
    public void updateUserId(int newUserId) {
        this.userId = newUserId;
        if (this.audioRecordingService instanceof AudioRecordingServiceFixExtended) {
            ((AudioRecordingServiceFixExtended) this.audioRecordingService).setCurrentUserId(newUserId);
            LOGGER.log(Level.INFO, "ID utilisateur mis à jour: {0}", newUserId);
        }
    }

    /**
     * Récupère l'email de l'utilisateur connecté
     * 
     * @return L'email de l'utilisateur
     */
    public String getCurrentUserEmail() {
        try {
            UserService userService = ServiceFactory.getInstance().getUserService();
            User user = userService.getUserById(userId);
            return user != null ? user.getEmail() : "Utilisateur inconnu";
        } catch (Exception e) {
            return "Utilisateur inconnu";
        }
    }

    /**
     * Démarre l'enregistrement audio.
     */
    public void startRecording() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "recording_" + userId + "_" + timestamp;
            audioRecordingService.startRecording(fileName);
            isRecording = true;
            LOGGER.log(Level.INFO, "Démarrage de l'enregistrement: {0}", fileName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du démarrage de l'enregistrement", e);
            throw new RuntimeException("Erreur lors du démarrage de l'enregistrement", e);
        }
    }

    /**
     * Arrête l'enregistrement audio et sauvegarde l'enregistrement.
     * 
     * @param name Le nom à donner à l'enregistrement
     * @return true si l'enregistrement a été sauvegardé avec succès, false sinon
     * @throws Exception Si une erreur survient lors de l'arrêt ou de la sauvegarde
     */
    public boolean stopAndSaveRecording(String name) throws Exception {
        if (!audioRecordingService.isRecording()) {
            return false;
        }

        // Arrêt de l'enregistrement
        audioRecordingService.stopRecording();

        // Création de l'enregistrement avec chiffrement
        AudioRecording recording = audioRecordingService.createRecording(
                name,
                LocalDateTime.now(),
                0, // La durée sera calculée par le service
                userId);

        // Sauvegarde dans la base de données
        int recordingId = audioRecordingService.saveRecording(recording);

        return recordingId > 0;
    }

    /**
     * Joue un enregistrement audio.
     * 
     * @param recordingId L'ID de l'enregistrement à jouer
     * @return true si la lecture a commencé, false sinon
     * @throws Exception Si une erreur survient lors de la lecture
     */
    public boolean playRecording(int recordingId) throws Exception {
        try {
            if (audioRecordingService.isPlaying()) {
                stopPlayback();
            }

            System.out.println("Demande de lecture de l'enregistrement #" + recordingId);

            // Récupération de l'enregistrement
            AudioRecording recording = audioRecordingService.getRecording(recordingId);

            if (recording == null) {
                System.err.println("Enregistrement #" + recordingId + " non trouvé");
                return false;
            }

            System.out.println("Enregistrement #" + recordingId + " trouvé: " + recording.getName());
            System.out.println("Chemin du fichier: " + recording.getFilePath());
            System.out.println("Clé de déchiffrement disponible: " + (recording.getEncryptionKey() != null));

            // Vérifier le fichier avant la lecture
            File audioFile = new File(recording.getFilePath());
            if (!audioFile.exists()) {
                System.err.println("ERREUR: Le fichier audio n'existe pas: " + recording.getFilePath());
                return false;
            }

            // Utiliser la version améliorée avec l'ID
            if (audioRecordingService instanceof AudioRecordingServiceFixExtended) {
                // Appel direct à la méthode prenant un ID d'enregistrement
                ((AudioRecordingServiceFixExtended) audioRecordingService).playRecording(recordingId);
            } else {
                // Fallback sur l'ancienne méthode si nécessaire
                audioRecordingService.playRecording(recording.getFilePath());
            }

            isPlaying = true;
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
            isPlaying = false;
            return false;
        }
    }

    /**
     * Arrête la lecture en cours.
     */
    public void stopPlayback() {
        audioRecordingService.stopPlaying();
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
     * Vérifie si une lecture est en cours.
     * 
     * @return true si une lecture est en cours, false sinon
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Formate une date pour l'affichage.
     * 
     * @param dateTime La date à formater
     * @return La date formatée
     */
    public String formatDateForDisplay(LocalDateTime dateTime) {
        return dateTime.format(DISPLAY_FORMATTER);
    }

    /**
     * Formate une durée en secondes en format MM:SS.
     * 
     * @param durationInSeconds La durée en secondes
     * @return La durée formatée
     */
    public String formatDuration(int durationInSeconds) {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Récupère la liste des enregistrements, y compris ceux partagés avec
     * l'utilisateur courant.
     * 
     * @param directory Le répertoire contenant les enregistrements
     * @return La liste des enregistrements
     */
    public List<AudioRecording> getRecordings(String directory) {
        List<AudioRecording> recordings = audioRecordingService.getRecordings(directory);

        try {
            // Ajouter les enregistrements partagés
            List<AudioRecording> sharedRecordings = sharedRecordingService.getSharedRecordings(userId);
            LOGGER.log(Level.INFO, "Enregistrements partagés récupérés: {0}", sharedRecordings.size());

            // Assurer que les userId correspondent à l'utilisateur courant pour l'accès
            for (AudioRecording shared : sharedRecordings) {
                // Forcer l'userId pour garantir l'accès
                if (shared.getUserId() != userId) {
                    LOGGER.log(Level.INFO, "Adaptation de l'userId pour l'enregistrement partagé {0}: de {1} à {2}",
                            new Object[] { shared.getId(), shared.getUserId(), userId });
                    shared.setUserId(userId);
                }
            }

            recordings.addAll(sharedRecordings);
            LOGGER.log(Level.INFO, "Nombre total d'enregistrements (personnels + partagés): {0}", recordings.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des enregistrements partagés", e);
            e.printStackTrace();
        }

        return recordings;
    }

    /**
     * Supprime un enregistrement audio.
     * 
     * @param recordingId L'ID de l'enregistrement à supprimer
     * @return true si l'enregistrement a été supprimé, false sinon
     */
    public boolean deleteRecording(int recordingId) {
        try {
            return audioRecordingService.deleteRecording(recordingId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de l'enregistrement", e);
            return false;
        }
    }

    public void stopPlaying() {
        try {
            audioRecordingService.stopPlaying();
            isPlaying = false;
            LOGGER.log(Level.INFO, "Arrêt de la lecture");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'arrêt de la lecture", e);
            throw new RuntimeException("Erreur lors de l'arrêt de la lecture", e);
        }
    }

    public void playRecording(String fileName) {
        try {
            audioRecordingService.playRecording(fileName);
            isPlaying = true;
            LOGGER.log(Level.INFO, "Lecture de l'enregistrement: {0}", fileName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la lecture de l'enregistrement", e);
            throw new RuntimeException("Erreur lors de la lecture de l'enregistrement", e);
        }
    }

    /**
     * Arrête l'enregistrement en cours.
     */
    public void stopRecording() {
        try {
            audioRecordingService.stopRecording();
            isRecording = false;
            LOGGER.log(Level.INFO, "Arrêt de l'enregistrement");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'arrêt de l'enregistrement", e);
            throw new RuntimeException("Erreur lors de l'arrêt de l'enregistrement", e);
        }
    }

    public void setPlaybackListener(PlaybackListener listener) {
        this.playbackListener = listener;
        if (audioRecordingService instanceof AudioRecordingServiceFixExtended) {
            ((AudioRecordingServiceFixExtended) audioRecordingService).setPlaybackListener(
                    () -> {
                        isPlaying = false;
                        if (playbackListener != null) {
                            playbackListener.onPlaybackFinished();
                        }
                    });
        }
    }

    public interface PlaybackListener {
        void onPlaybackFinished();
    }

    /**
     * Partage un enregistrement avec un autre utilisateur.
     * 
     * @param recordingId     L'ID de l'enregistrement à partager
     * @param targetUserEmail L'email de l'utilisateur destinataire
     * @return true si le partage a réussi, false sinon
     */
    public boolean shareRecording(int recordingId, String targetUserEmail) {
        try {
            LOGGER.log(Level.INFO, "Tentative de partage de l'enregistrement {0} avec {1}",
                    new Object[] { recordingId, targetUserEmail });

            // Vérifier si les services nécessaires sont disponibles
            if (sharedRecordingService == null) {
                LOGGER.log(Level.SEVERE, "SharedRecordingService n'est pas initialisé");
                return false;
            }

            if (targetUserEmail == null || targetUserEmail.isEmpty()) {
                LOGGER.log(Level.WARNING, "Email de l'utilisateur cible invalide");
                return false;
            }

            // Vérifier que l'enregistrement existe
            AudioRecording recording = null;
            try {
                recording = audioRecordingService.getRecording(recordingId);
                if (recording != null) {
                    LOGGER.log(Level.INFO, "Enregistrement trouvé: {0}, ID: {1}, UserId: {2}",
                            new Object[] { recording.getName(), recording.getId(), recording.getUserId() });
                    LOGGER.log(Level.INFO, "EncryptionKey présente: {0}",
                            recording.getEncryptionKey() != null);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de l'enregistrement", e);
                return false;
            }

            if (recording == null) {
                LOGGER.log(Level.WARNING, "Enregistrement non trouvé: {0}", recordingId);
                return false;
            }

            // Vérifier l'accès
            if (recording.getUserId() != userId) {
                LOGGER.log(Level.WARNING, "L'enregistrement n'appartient pas à l'utilisateur actuel. "
                        + "UserId de l'enregistrement: {0}, UserId actuel: {1}",
                        new Object[] { recording.getUserId(), userId });
                return false;
            }

            // Si l'email contient "@missie.mu" ou un autre domaine spécifique qui cause des
            // problèmes
            if (targetUserEmail.contains("@missie.mu")) {
                // Normaliser l'email pour éviter les problèmes avec les caractères spéciaux
                String normalizedEmail = targetUserEmail.toLowerCase().trim();
                LOGGER.log(Level.INFO, "Email normalisé pour le domaine missie.mu: {0}", normalizedEmail);

                // Tenter le partage avec l'email normalisé
                return sharedRecordingService.shareRecording(recordingId, userId, normalizedEmail);
            }

            // Tenter le partage
            return sharedRecordingService.shareRecording(recordingId, userId, targetUserEmail);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du partage de l'enregistrement", e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère la liste des emails des utilisateurs disponibles pour le partage.
     * 
     * @return La liste des emails des utilisateurs
     */
    public List<String> getUserEmails() {
        List<String> emails = new ArrayList<>();
        try {
            List<User> users = userService.getAllUsers();
            for (User user : users) {
                // Ne pas inclure l'utilisateur courant
                if (user.getId() != userId) {
                    emails.add(user.getEmail());
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des emails des utilisateurs", e);
        }
        return emails;
    }

    /**
     * Génère ou récupère les clés RSA de l'utilisateur courant.
     * 
     * @return Les clés de l'utilisateur, ou null en cas d'erreur
     */
    public UserKeys getUserKeys() {
        try {
            return userKeysService.getUserKeys(userId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des clés de l'utilisateur", e);
            return null;
        }
    }

    /**
     * Exporte la clé de chiffrement d'un enregistrement.
     * 
     * @param recordingId L'ID de l'enregistrement
     * @return Les informations de partage (clé, chemin du fichier, etc.)
     * @throws Exception Si une erreur se produit
     */
    public Map<String, String> exportEncryptionKey(int recordingId) throws Exception {
        return ((AudioRecordingServiceFixExtended) audioRecordingService).exportEncryptionKey(recordingId);
    }

    /**
     * Lit un enregistrement avec une clé fournie par l'utilisateur.
     * 
     * @param filePath Le chemin du fichier
     * @param key      La clé fournie par l'utilisateur
     * @throws Exception Si une erreur se produit
     */
    public void playRecordingWithKey(String filePath, String key) throws Exception {
        // Vérifier les paramètres
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide");
        }
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("La clé de déchiffrement ne peut pas être vide");
        }

        LOGGER.log(Level.INFO, "Tentative de lecture du fichier {0} avec une clé fournie", filePath);

        // Vérifier que le fichier existe
        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            // Si le fichier n'a pas d'extension .enc, essayer de l'ajouter
            if (!filePath.toLowerCase().endsWith(".enc")) {
                audioFile = new File(filePath + ".enc");
                if (!audioFile.exists()) {
                    // Vérifier s'il existe dans le répertoire recordings/
                    audioFile = new File("recordings/" + filePath + ".enc");
                    if (!audioFile.exists()) {
                        throw new FileNotFoundException("Fichier audio introuvable: " + filePath);
                    }
                }
            } else {
                // Vérifier s'il existe dans le répertoire recordings/
                String fileName = new File(filePath).getName();
                audioFile = new File("recordings/" + fileName);
                if (!audioFile.exists()) {
                    throw new FileNotFoundException("Fichier audio introuvable: " + filePath);
                }
            }
        }

        LOGGER.log(Level.INFO, "Fichier trouvé: {0}", audioFile.getAbsolutePath());

        // Utiliser un service audio pour lire le fichier avec la clé fournie
        if (this.audioRecordingService instanceof AudioRecordingServiceFixExtended) {
            ((AudioRecordingServiceFixExtended) this.audioRecordingService)
                    .playRecordingWithKey(audioFile.getAbsolutePath(), key);
            isPlaying = true;
        } else {
            throw new UnsupportedOperationException("Le service audio ne supporte pas la lecture avec clé");
        }
    }

    /**
     * Exporte un enregistrement vers un emplacement spécifié.
     * 
     * @param recordingId     L'ID de l'enregistrement à exporter
     * @param destinationPath Le chemin de destination
     * @return true si l'exportation a réussi, false sinon
     * @throws Exception Si une erreur se produit
     */
    public boolean exportRecording(int recordingId, String destinationPath) throws Exception {
        try {
            return ((AudioRecordingServiceFixExtended) audioRecordingService).exportRecording(recordingId,
                    destinationPath);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'exportation de l'enregistrement", e);
            throw e;
        }
    }
}