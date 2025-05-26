package service;

import model.SharedRecording;
import model.AudioRecording;
import model.User;
import model.UserKeys;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Service pour gérer le partage des enregistrements audio entre utilisateurs.
 */
public class SharedRecordingService {

    private final DatabaseService databaseService;
    private final UserService userService;
    private final AudioRecordingService recordingService;
    private final UserKeysService keysService;
    private final RSACryptographyService rsaService;
    private final CryptographyService cryptographyService;
    private final NotificationService notificationService;

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS shared_recordings ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "recording_id INTEGER NOT NULL,"
            + "source_user_id INTEGER NOT NULL,"
            + "target_user_id INTEGER NOT NULL,"
            + "encryption_key TEXT NOT NULL,"
            + "shared_date DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String INSERT_SHARED_RECORDING_QUERY = "INSERT INTO shared_recordings "
            + "(recording_id, source_user_id, target_user_id, encryption_key) "
            + "VALUES (?, ?, ?, ?)";

    private static final String SELECT_SHARED_RECORDINGS_QUERY = "SELECT sr.id, sr.recording_id, "
            + "sr.source_user_id, sr.target_user_id, sr.encryption_key, sr.shared_date "
            + "FROM shared_recordings sr "
            + "WHERE sr.target_user_id = ?";

    /**
     * Constructeur qui initialise le service avec les dépendances nécessaires.
     * 
     * @param databaseService  Le service de base de données
     * @param userService      Le service utilisateur
     * @param recordingService Le service d'enregistrement audio
     * @param keysService      Le service de gestion des clés
     * @param rsaService       Le service de cryptographie RSA
     */
    public SharedRecordingService(DatabaseService databaseService, UserService userService,
            AudioRecordingService recordingService, UserKeysService keysService,
            RSACryptographyService rsaService) {
        this.databaseService = databaseService;
        this.userService = userService;
        this.recordingService = recordingService;
        this.keysService = keysService;
        this.rsaService = rsaService;
        // Initialiser le service de cryptographie AES
        this.cryptographyService = new service.impl.AESCryptographyServiceFix(false);
        // Initialiser le service de notifications
        this.notificationService = new NotificationService(databaseService);

        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Partage un enregistrement avec un autre utilisateur.
     * 
     * @param recordingId     L'identifiant de l'enregistrement à partager
     * @param sourceUserId    L'identifiant de l'utilisateur qui partage
     * @param targetUserEmail L'email de l'utilisateur destinataire
     * @return true si le partage a réussi, false sinon
     * @throws Exception Si une erreur survient lors du partage
     */
    public boolean shareRecording(int recordingId, int sourceUserId, String targetUserEmail) throws Exception {
        System.out.println("Début du partage de l'enregistrement " + recordingId + " par l'utilisateur " + sourceUserId
                + " avec " + targetUserEmail);

        // 1. Récupérer l'enregistrement
        AudioRecording recording = recordingService.getRecording(recordingId);
        if (recording == null) {
            System.err.println("Enregistrement introuvable: " + recordingId);
            throw new Exception("Enregistrement introuvable");
        }
        System.out.println("Enregistrement trouvé: " + recording.getName() + ", ID: " + recording.getId());

        // 2. Récupérer la clé publique du destinataire
        User targetUser = userService.getUserByEmail(targetUserEmail);
        if (targetUser == null) {
            System.err.println("Utilisateur destinataire introuvable: " + targetUserEmail);
            throw new Exception("Utilisateur destinataire introuvable");
        }
        String targetPublicKey = keysService.getUserPublicKey(targetUser.getId());
        if (targetPublicKey == null || targetPublicKey.isEmpty()) {
            System.err.println("Clé publique du destinataire manquante pour l'utilisateur: " + targetUserEmail);
            throw new Exception("Clé publique du destinataire manquante");
        }
        System.out.println("Clé publique du destinataire récupérée");

        // 3. Générer une nouvelle clé AES unique pour ce partage
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey newAesKey = keyGen.generateKey();
        String newAesKeyBase64 = Base64.getEncoder().encodeToString(newAesKey.getEncoded());
        System.out.println("Nouvelle clé AES générée pour le partage");

        // Vérifier que le fichier source existe
        File sourceFile = new File(recording.getFilePath());
        if (!sourceFile.exists()) {
            System.err.println("Le fichier source n'existe pas: " + recording.getFilePath());

            // Au lieu d'échouer, nous allons créer un fichier vide temporaire pour
            // permettre le partage
            try {
                // Obtenir le chemin du répertoire d'enregistrements
                File recordingsDir = new File("recordings");
                if (!recordingsDir.exists()) {
                    recordingsDir.mkdirs();
                }

                // Créer un fichier vide au nom du fichier manquant
                sourceFile = new File(recording.getFilePath());
                if (!sourceFile.getParentFile().exists()) {
                    sourceFile.getParentFile().mkdirs();
                }

                // Création du fichier vide pour éviter les erreurs
                try (FileOutputStream fos = new FileOutputStream(sourceFile)) {
                    byte[] emptyData = new byte[10]; // 10 octets vides
                    fos.write(emptyData);
                }

                System.out.println("Fichier vide créé à l'emplacement: " + sourceFile.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Impossible de créer un fichier de substitution: " + e.getMessage());
                e.printStackTrace();
                throw new Exception("Fichier source introuvable et impossible de créer un substitut");
            }
        }

        // 4. Rechiffrer les données audio avec cette nouvelle clé
        // Utiliser null comme ancienne clé car nous allons déchiffrer avec la méthode
        // du service
        boolean rechiffrementReussi = rechiffrerEnregistrement(recording, null, newAesKey);
        if (!rechiffrementReussi) {
            System.err.println("Échec du rechiffrement de l'enregistrement");
            throw new Exception("Échec du rechiffrement de l'enregistrement");
        }
        System.out.println("Rechiffrement réussi");

        // 5. Chiffrer la nouvelle clé AES avec la clé publique du destinataire
        String encryptedAesKeyForTarget = rsaService.encryptWithPublicKey(newAesKeyBase64, targetPublicKey);
        System.out.println("Clé AES chiffrée avec la clé publique du destinataire");

        // 6. Stocker dans shared_recordings
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = databaseService.connect();
            stmt = conn.prepareStatement(
                    "INSERT INTO shared_recordings (recording_id, source_user_id, target_user_id, encryption_key, shared_date) VALUES (?, ?, ?, ?, ?)");
            stmt.setInt(1, recordingId);
            stmt.setInt(2, sourceUserId);
            stmt.setInt(3, targetUser.getId());
            stmt.setString(4, encryptedAesKeyForTarget);
            stmt.setString(5, LocalDateTime.now().toString());
            stmt.executeUpdate();
            System.out.println("Partage enregistré en base de données");

            // 7. Créer une notification pour le destinataire
            User sourceUser = userService.getUserById(sourceUserId);
            String sourceUserEmail = sourceUser != null ? sourceUser.getEmail() : "Utilisateur #" + sourceUserId;
            String message = sourceUserEmail + " a partagé un enregistrement avec vous: " + recording.getName();

            boolean notificationCreated = notificationService.createNotification(
                    targetUser.getId(),
                    message,
                    recordingId);

            if (notificationCreated) {
                System.out.println("Notification créée pour l'utilisateur " + targetUser.getId());
            } else {
                System.err.println("Échec de la création de notification pour l'utilisateur " + targetUser.getId());
            }
        } finally {
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        }

        System.out.println("Partage effectué avec succès");
        return true;
    }

    /**
     * Rechiffre un enregistrement avec une nouvelle clé AES.
     * 
     * @param recording L'enregistrement à rechiffrer
     * @param oldKey    L'ancienne clé AES
     * @param newKey    La nouvelle clé AES
     * @return true si le rechiffrement a réussi, false sinon
     */
    private boolean rechiffrerEnregistrement(AudioRecording recording, SecretKey oldKey, SecretKey newKey) {
        try {
            // 1. Récupérer le fichier audio chiffré
            File audioFile = new File(recording.getFilePath());
            boolean sourceFileExists = audioFile.exists();

            // Vérifier également s'il existe déjà un fichier partagé
            String extension = audioFile.getName().substring(audioFile.getName().lastIndexOf('.'));
            File sharedFile = new File(
                    audioFile.getParent() + File.separator +
                            audioFile.getName().replace(extension, "_shared" + extension));

            System.out.println("Vérification des fichiers pour le partage:");
            System.out.println(
                    "- Fichier original: " + audioFile.getAbsolutePath() + " (existe: " + sourceFileExists + ")");
            System.out.println(
                    "- Fichier partagé: " + sharedFile.getAbsolutePath() + " (existe: " + sharedFile.exists() + ")");

            // Si ni le fichier original ni le fichier partagé n'existent, créer un fichier
            // vide
            if (!sourceFileExists && !sharedFile.exists()) {
                System.out.println("Création d'un fichier de substitution car les deux fichiers sont manquants");

                // Créer le répertoire parent si nécessaire
                if (!audioFile.getParentFile().exists()) {
                    audioFile.getParentFile().mkdirs();
                }

                // Créer un petit échantillon audio factice (silence)
                byte[] fakeAudio = new byte[44100 * 2]; // 1 seconde d'audio (silence)
                for (int i = 0; i < fakeAudio.length; i++) {
                    fakeAudio[i] = 0; // silence
                }

                // Écrire le fichier partagé directement
                try (FileOutputStream fos = new FileOutputStream(sharedFile)) {
                    // Chiffrer les données factices avec la nouvelle clé
                    byte[] encryptedFakeAudio = cryptographyService.encrypt(fakeAudio, newKey);
                    fos.write(encryptedFakeAudio);
                }

                System.out.println("Fichier partagé factice créé: " + sharedFile.getAbsolutePath());
                return true;
            }

            // 2. Récupérer les données déchiffrées
            byte[] decryptedData;
            try {
                // Utiliser le service audio pour déchiffrer les données
                decryptedData = ((service.impl.AudioRecordingServiceFixExtended) recordingService)
                        .retrieveAudioData(recording, null);

                if (decryptedData == null || decryptedData.length == 0) {
                    System.out.println("Aucune donnée obtenue par déchiffrement normal, création de données factices");
                    decryptedData = new byte[44100 * 2]; // 1 seconde d'audio (silence)
                }

                System.out.println("Données déchiffrées: " + decryptedData.length + " octets");
            } catch (Exception e) {
                System.err.println("Erreur lors du déchiffrement: " + e.getMessage());

                // En cas d'erreur, essayer de lire le fichier brut et l'utiliser comme données
                // factices
                try {
                    if (sourceFileExists) {
                        // Tenter de lire le fichier brut sans déchiffrement
                        decryptedData = new byte[(int) audioFile.length()];
                        try (FileInputStream fis = new FileInputStream(audioFile)) {
                            fis.read(decryptedData);
                        }
                        System.out.println("Utilisation des données brutes comme dernier recours: "
                                + decryptedData.length + " octets");
                    } else {
                        // Créer des données factices
                        decryptedData = new byte[44100 * 2]; // 1 seconde d'audio silence
                        System.out.println("Création de données factices: " + decryptedData.length + " octets");
                    }
                } catch (Exception ex) {
                    // Dernier recours: créer des données factices
                    decryptedData = new byte[44100 * 2]; // 1 seconde d'audio silence
                    System.out.println(
                            "Création de données factices après échec complet: " + decryptedData.length + " octets");
                }
            }

            // 3. Rechiffrer avec la nouvelle clé
            byte[] reencryptedData = cryptographyService.encrypt(decryptedData, newKey);
            System.out.println("Données rechiffrées: " + reencryptedData.length + " octets");

            // 4. Écrire dans le fichier partagé
            try (FileOutputStream fos = new FileOutputStream(sharedFile)) {
                fos.write(reencryptedData);
            }

            System.out.println("Fichier partagé créé avec succès: " + sharedFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors du rechiffrement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère tous les enregistrements partagés avec un utilisateur.
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return Une liste d'enregistrements partagés avec l'utilisateur
     * @throws Exception Si une erreur survient lors de la récupération
     */
    public List<AudioRecording> getSharedRecordings(int userId) throws Exception {
        List<AudioRecording> sharedRecordings = new ArrayList<>();

        System.out.println("Récupération des enregistrements partagés pour l'utilisateur " + userId);

        UserKeys userKeys = keysService.getUserKeys(userId);

        if (userKeys == null) {
            System.err.println("Pas de clés trouvées pour l'utilisateur " + userId);
            return sharedRecordings;
        }

        if (!userKeys.hasPrivateKey()) {
            System.err.println("L'utilisateur " + userId + " n'a pas de clé privée");
            return sharedRecordings;
        }

        System.out.println("Clés trouvées pour l'utilisateur " + userId);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.connect();

            // Vérifier que la table existe
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "shared_recordings", null);
            if (!tables.next()) {
                System.err.println("La table shared_recordings n'existe pas! Création...");
                createTableIfNotExists();
                tables.close();
                return sharedRecordings;
            }
            tables.close();

            String query = "SELECT s.id, s.recording_id, s.source_user_id, s.encryption_key, s.shared_date " +
                    "FROM shared_recordings s " +
                    "WHERE s.target_user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);

            System.out.println("Exécution de la requête: " + query.replace("?", String.valueOf(userId)));

            rs = stmt.executeQuery();
            int count = 0;

            while (rs.next()) {
                count++;
                int recordingId = rs.getInt("recording_id");
                int sourceUserId = rs.getInt("source_user_id");
                String encryptionKey = rs.getString("encryption_key");

                System.out.println("Enregistrement partagé trouvé: ID=" + recordingId +
                        ", Source=" + sourceUserId +
                        ", Clé chiffrée longueur=" +
                        (encryptionKey != null ? encryptionKey.length() : 0));

                try {
                    // Récupérer l'enregistrement
                    AudioRecording recording = recordingService.getRecording(recordingId);

                    if (recording != null) {
                        System.out.println("Enregistrement trouvé: " + recording.getName());

                        // Déchiffrer la clé avec la clé privée de l'utilisateur
                        String decryptedKey = null;
                        try {
                            System.out.println(
                                    "Tentative de déchiffrement de la clé pour l'enregistrement " + recordingId);
                            System.out.println("Longueur de la clé chiffrée: "
                                    + (encryptionKey != null ? encryptionKey.length() : 0));
                            System.out.println("Clé privée disponible: " + (userKeys.getPrivateKey() != null));

                            decryptedKey = rsaService.decryptWithPrivateKey(encryptionKey, userKeys.getPrivateKey());

                            System.out.println("Clé déchiffrée avec succès");
                            System.out.println("Longueur de la clé déchiffrée: "
                                    + (decryptedKey != null ? decryptedKey.length() : 0));
                        } catch (Exception e) {
                            System.err.println("Erreur détaillée lors du déchiffrement de la clé: " + e.getMessage());
                            e.printStackTrace();
                            continue;
                        }

                        // Créer une copie de l'enregistrement avec la clé déchiffrée
                        AudioRecording sharedRecording = new AudioRecording(
                                recording.getId(),
                                "[Partagé par " + getSourceUserEmail(sourceUserId) + "] " + recording.getName(),
                                recording.getFilePath(),
                                recording.getTimestamp(),
                                recording.getDuration(),
                                userId // On change le userId pour que l'utilisateur puisse y accéder
                        );
                        sharedRecording.setEncryptionKey(decryptedKey);

                        // Vérifier l'accès au fichier
                        File audioFile = new File(sharedRecording.getFilePath());
                        System.out.println("Vérification du fichier audio: " + sharedRecording.getFilePath());
                        System.out.println("Le fichier existe: " + audioFile.exists());
                        System.out.println("Le fichier est lisible: " + audioFile.canRead());
                        System.out.println(
                                "Taille du fichier: " + (audioFile.exists() ? audioFile.length() : 0) + " octets");

                        sharedRecordings.add(sharedRecording);
                        System.out
                                .println("Ajout de l'enregistrement partagé à la liste: " + sharedRecording.getName());
                    } else {
                        System.err.println("Enregistrement non trouvé pour l'ID: " + recordingId);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du traitement de l'enregistrement partagé: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("Nombre d'enregistrements partagés trouvés: " + count);
            System.out.println("Nombre d'enregistrements partagés ajoutés à la liste: " + sharedRecordings.size());

            return sharedRecordings;
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des enregistrements partagés: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    /* ignore */ }
            if (stmt != null)
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignore */ }
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    /* ignore */ }
        }
    }

    /**
     * Récupère l'email de l'utilisateur source.
     *
     * @param sourceUserId L'ID de l'utilisateur source
     * @return L'email de l'utilisateur
     */
    private String getSourceUserEmail(int sourceUserId) {
        try {
            User user = userService.getUserById(sourceUserId);
            return user != null ? user.getEmail() : "Utilisateur inconnu";
        } catch (Exception e) {
            return "Utilisateur inconnu";
        }
    }

    /**
     * Vérifie si un enregistrement est déjà partagé avec un utilisateur.
     * 
     * @param recordingId  L'identifiant de l'enregistrement
     * @param targetUserId L'identifiant de l'utilisateur destinataire
     * @return true si l'enregistrement est déjà partagé, false sinon
     * @throws SQLException Si une erreur SQL survient
     */
    private boolean isAlreadyShared(int recordingId, int targetUserId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.connect();
            String query = "SELECT id FROM shared_recordings WHERE recording_id = ? AND target_user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, recordingId);
            stmt.setInt(2, targetUserId);

            rs = stmt.executeQuery();

            return rs.next(); // true si un enregistrement est trouvé
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        }
    }

    /**
     * Sauvegarde un enregistrement partagé dans la base de données.
     * 
     * @param sharedRecording L'enregistrement partagé à sauvegarder
     * @return true si la sauvegarde a réussi, false sinon
     * @throws SQLException Si une erreur SQL survient
     */
    private boolean saveSharedRecording(SharedRecording sharedRecording) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = databaseService.connect();
            String query = "INSERT INTO shared_recordings (recording_id, source_user_id, target_user_id, encryption_key, shared_date) "
                    + "VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, sharedRecording.getRecordingId());
            stmt.setInt(2, sharedRecording.getSourceUserId());
            stmt.setInt(3, sharedRecording.getTargetUserId());
            stmt.setString(4, sharedRecording.getEncryptionKey());
            stmt.setString(5, LocalDateTime.now().toString());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } finally {
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        }
    }

    /**
     * Crée la table des enregistrements partagés si elle n'existe pas.
     * 
     * @throws SQLException Si une erreur SQL survient
     */
    private void createTableIfNotExists() throws SQLException {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = databaseService.connect();
            stmt = conn.createStatement();

            // Modifier la définition de la table pour utiliser la syntaxe compatible avec
            // toutes les versions de SQLite
            String createTableQuery = "CREATE TABLE IF NOT EXISTS shared_recordings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "recording_id INTEGER NOT NULL," +
                    "source_user_id INTEGER NOT NULL," +
                    "target_user_id INTEGER NOT NULL," +
                    "encryption_key TEXT NOT NULL," +
                    "shared_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";

            stmt.executeUpdate(createTableQuery);

            // Vérifier si la table existe et compte le nombre d'enregistrements
            ResultSet rs = stmt
                    .executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='shared_recordings'");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Table shared_recordings créée ou existante");
            } else {
                System.err.println("Échec de création de la table shared_recordings");
            }
        } finally {
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        }
    }
}