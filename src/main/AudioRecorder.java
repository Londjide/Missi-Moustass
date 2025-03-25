package main;

import com.barbichetz.audio.AudioFormatManager;
import com.barbichetz.audio.AudioPlayer;

import java.awt.EventQueue;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.SecretKey;
import javax.sound.sampled.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Application d'enregistrement audio avec chiffrement AES et vérification d'intégrité.
 * Cette application permet d'enregistrer, de sauvegarder, de lire et de gérer des enregistrements
 * audio. Les enregistrements sont chiffrés avant stockage en base de données et leur intégrité
 * est vérifiée lors du chargement.
 * 
 * <p><strong>Structure de l'application :</strong></p>
 * <ul>
 *   <li>Interface utilisateur graphique pour l'enregistrement et la lecture</li>
 *   <li>Stockage sécurisé en base de données SQLite</li>
 *   <li>Chiffrement AES pour la confidentialité des données</li>
 *   <li>Vérification d'intégrité avec SHA-256</li>
 * </ul>
 * 
 * @author Équipe Missié Moustass
 * @version 1.0
 */
public class AudioRecorder extends JFrame {

    private static final long serialVersionUID = 1L;
    /** Panneau principal de l'interface */
    private JPanel contentPane;
    /** Tableau affichant la liste des enregistrements */
    private JTable table;
    /** Modèle de données pour le tableau */
    private DefaultTableModel tableModel;
    /** Bouton pour démarrer l'enregistrement */
    private JButton btnRecord;
    /** Bouton pour arrêter l'enregistrement ou la lecture */
    private JButton btnStop;
    /** Bouton pour lire l'enregistrement sélectionné */
    private JButton btnPlay;
    /** Bouton pour supprimer l'enregistrement sélectionné */
    private JButton btnDelete;
    /** Étiquette affichant l'état actuel de l'application */
    private JLabel statusLabel;
    /** Identifiant de l'utilisateur connecté */
    private int userId;
    /** Étiquette affichant le nom de l'utilisateur connecté */
    private JLabel userLabel;
    
    /** Connexion à la base de données SQLite */
    private Connection conn;
    /** Indique si un enregistrement est en cours */
    private boolean isRecording = false;
    /** Indique si une lecture est en cours */
    private boolean isPlaying = false;
    /** Ligne audio pour l'enregistrement */
    private TargetDataLine audioLine;
    /** Format audio utilisé pour l'enregistrement et la lecture */
    private AudioFormat audioFormat;
    /** Flux de sortie pour stocker les données audio enregistrées */
    private ByteArrayOutputStream outputStream;
    /** Thread utilisé pour l'enregistrement audio */
    private Thread recordingThread;
    /** Thread utilisé pour la lecture audio */
    private Thread playingThread;
    /** Index de la ligne sélectionnée dans le tableau */
    private int selectedRow = -1;
    /** Mixeur audio sélectionné pour l'enregistrement */
    private Mixer.Info selectedMixer = null;

    /**
     * Point d'entrée principal de l'application lorsqu'elle est lancée directement.
     * 
     * @param args Arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Configuration des propriétés système pour améliorer la compatibilité audio sur macOS
        System.setProperty("javax.sound.sampled.Clip", "com.sun.media.sound.DirectAudioDeviceProvider");
        System.setProperty("javax.sound.sampled.Port", "com.sun.media.sound.PortMixerProvider");
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Vérifier si un ID utilisateur a été fourni en argument
                    if (args.length > 0) {
                        try {
                            int userId = Integer.parseInt(args[0]);
                            AudioRecorder frame = new AudioRecorder(userId);
                            frame.setVisible(true);
                            return;
                        } catch (NumberFormatException e) {
                            System.err.println("ID utilisateur invalide: " + args[0]);
                        }
                    }
                    
                    // Si aucun ID utilisateur n'a été fourni, essayer d'ouvrir l'écran de connexion
                    JOptionPane.showMessageDialog(null, 
                            "Veuillez vous connecter d'abord.", 
                            "Connexion requise", 
                            JOptionPane.INFORMATION_MESSAGE);
                    
                    try {
                        Class<?> connexionClass = Class.forName("Auth.Connexion");
                        Object connexion = connexionClass.newInstance();
                        connexionClass.getMethod("afficher").invoke(connexion);
                    } catch (Exception ex) {
                        // Si la classe Connexion n'est pas disponible, on lance directement l'application
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                                "Module de connexion non disponible. Lancement en mode test.",
                                "Mode test", 
                                JOptionPane.WARNING_MESSAGE);
                        
                        // Identifiant utilisateur par défaut pour les tests
                        int defaultUserId = 1;
                        AudioRecorder frame = new AudioRecorder(defaultUserId);
                        frame.setVisible(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Constructeur de la classe AudioRecorder.
     * Initialise la base de données, l'interface utilisateur et charge les enregistrements existants
     * pour l'utilisateur spécifié.
     * 
     * @param userId Identifiant de l'utilisateur connecté
     */
    public AudioRecorder(int userId) {
        this.userId = userId;
        // Utilisation du même format audio que dans la classe Auth.AudioRecorder
        this.audioFormat = new AudioFormat(44100, 16, 1, true, false);
        initializeDatabase();
        initializeUI();
        loadUserInfo();
        loadAudioRecordings();
    }
    
    /**
     * Initialise la connexion à la base de données SQLite.
     */
    private void initializeDatabase() {
        try {
            // Chargement du pilote JDBC SQLite
            Class.forName("org.sqlite.JDBC");
            
            // Création d'une connexion à la base de données
            conn = DriverManager.getConnection("jdbc:sqlite:users.db");

            System.out.println("Base de données initialisée avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Échec de l'initialisation de la base de données: " + e.getMessage(), 
                                         "Erreur de base de données", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Charge les informations de l'utilisateur connecté.
     */
    private void loadUserInfo() {
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT email FROM users WHERE id = ?");
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String email = rs.getString("email");
                userLabel.setText("Utilisateur connecté: " + email);
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Initialise l'interface utilisateur de l'application.
     * Configure la fenêtre principale, le tableau d'enregistrements et les boutons de contrôle.
     */
    private void initializeUI() {
        setTitle("Enregistreur Audio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 650, 500);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(10, 10));
        setContentPane(contentPane);
        
        // Panel pour le titre et l'utilisateur
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        // Étiquette de titre
        JLabel titleLabel = new JLabel("Missié Moustass - Enregistreur Audio Sécurisé");
        titleLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Étiquette utilisateur
        userLabel = new JLabel("Utilisateur connecté: ");
        
        // Créer un panneau pour l'utilisateur et le bouton de déconnexion
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(userLabel, BorderLayout.CENTER);
        
        // Bouton de déconnexion
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Fermer la fenêtre actuelle
                
                // Rediriger vers la page de connexion (si vous avez une classe Connexion)
                try {
                    Class<?> connexionClass = Class.forName("Auth.Connexion");
                    Object connexion = connexionClass.newInstance();
                    connexionClass.getMethod("afficher").invoke(connexion);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, 
                        "Déconnexion effectuée. Veuillez redémarrer l'application pour vous reconnecter.", 
                        "Déconnexion", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
            }
        });
        userPanel.add(logoutButton, BorderLayout.EAST);
        
        // Ajouter le panneau utilisateur au panneau d'en-tête
        headerPanel.add(userPanel, BorderLayout.SOUTH);
        
        contentPane.add(headerPanel, BorderLayout.NORTH);
        
        // Panel central pour le tableau des enregistrements
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        
        // Modèle de tableau pour les enregistrements
        tableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] {"ID", "Nom", "Date", "Durée"}
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Tableau des enregistrements
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        
        // Gestionnaire d'événements pour la sélection de ligne
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRow = table.getSelectedRow();
                updateButtonStates();
            }
        });
        
        // Défilement pour le tableau
        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        contentPane.add(centerPanel, BorderLayout.CENTER);
        
        // Panel pour les boutons de contrôle
        JPanel controlPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        
        // Bouton d'enregistrement
        btnRecord = new JButton("Enregistrer");
        btnRecord.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startRecording();
            }
        });
        controlPanel.add(btnRecord);
        
        // Bouton d'arrêt
        btnStop = new JButton("Arrêter");
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isRecording) {
                    stopRecording();
                } else if (isPlaying) {
                    stopPlayback();
                }
            }
        });
        btnStop.setEnabled(false);
        controlPanel.add(btnStop);
        
        // Bouton de lecture
        btnPlay = new JButton("Lire");
        btnPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedRow >= 0) {
                    int recordingId = (int) tableModel.getValueAt(selectedRow, 0);
                    playRecording(recordingId);
                }
            }
        });
        btnPlay.setEnabled(false);
        controlPanel.add(btnPlay);
        
        // Bouton de suppression
        btnDelete = new JButton("Supprimer");
        btnDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedRow >= 0) {
                    int recordingId = (int) tableModel.getValueAt(selectedRow, 0);
                    deleteRecording(recordingId);
                }
            }
        });
        btnDelete.setEnabled(false);
        controlPanel.add(btnDelete);
        
        contentPane.add(controlPanel, BorderLayout.SOUTH);
        
        // Étiquette de statut
        statusLabel = new JLabel("Prêt");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(statusLabel, BorderLayout.PAGE_END);
    }
    
    /**
     * Charge les enregistrements audio de l'utilisateur depuis la base de données.
     */
    private void loadAudioRecordings() {
        try {
            // Effacer le tableau existant
            while (tableModel.getRowCount() > 0) {
                tableModel.removeRow(0);
            }
            
            // Requête pour récupérer les enregistrements de l'utilisateur
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id, name, timestamp, duration FROM recordings WHERE user_id = ? ORDER BY timestamp DESC"
            );
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            // Ajouter chaque enregistrement au tableau
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String timestamp = rs.getString("timestamp");
                int duration = rs.getInt("duration");
                
                // Convertir la durée en format hh:mm:ss
                String durationStr = String.format("%d:%02d", duration / 60, duration % 60);
                
                // Ajouter une ligne au tableau
                tableModel.addRow(new Object[] {id, name, timestamp, durationStr});
            }
            
            rs.close();
            pstmt.close();
            
            // Réinitialiser la sélection
            selectedRow = -1;
            updateButtonStates();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des enregistrements: " + e.getMessage(), 
                                         "Erreur de base de données", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Démarre l'enregistrement audio.
     */
    private void startRecording() {
        try {
            // Configuration du format et de la ligne audio pour l'enregistrement
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            
            if (!AudioSystem.isLineSupported(info)) {
                JOptionPane.showMessageDialog(this, "Format audio non supporté", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Ouvrir la ligne audio
            audioLine = (TargetDataLine) AudioSystem.getLine(info);
            audioLine.open(audioFormat);
            audioLine.start();
            
            // Créer un flux de sortie pour les données audio
            outputStream = new ByteArrayOutputStream();
            
            // Mise à jour de l'état
            isRecording = true;
            statusLabel.setText("Enregistrement en cours...");
            updateButtonStates();
            
            // Démarrer un thread pour l'enregistrement
            recordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        
                        while (isRecording) {
                            bytesRead = audioLine.read(buffer, 0, buffer.length);
                            if (bytesRead > 0) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            
            recordingThread.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de l'accès au périphérique audio: " + e.getMessage(), 
                                         "Erreur d'entrée audio", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Arrête l'enregistrement en cours et sauvegarde les données.
     */
    private void stopRecording() {
        if (!isRecording || audioLine == null) {
            return;
        }
        
        // Arrêter l'enregistrement
        isRecording = false;
        
        // Attendre la fin du thread d'enregistrement
        try {
            recordingThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Fermer la ligne audio
        audioLine.stop();
        audioLine.close();
        
        // Récupérer les données audio
        byte[] audioData = outputStream.toByteArray();
        
        // Fermer le flux de sortie
        try {
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Si aucune donnée n'a été enregistrée, annuler
        if (audioData.length == 0) {
            statusLabel.setText("Aucune donnée audio enregistrée");
            updateButtonStates();
            return;
        }
        
        // Demander un nom pour l'enregistrement
        String recordingName = JOptionPane.showInputDialog(this, 
                "Entrez un nom pour l'enregistrement:", 
                "Enregistrement terminé", 
                JOptionPane.QUESTION_MESSAGE);
        
        if (recordingName == null || recordingName.trim().isEmpty()) {
            recordingName = "Enregistrement " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        
        // Chiffrer les données audio
        try {
            // Génération d'une clé de chiffrement
            SecretKey key = Auth.AES.generateSecretKey();
            String keyString = Auth.AES.encodeKeyToBase64(key);
            
            // Chiffrement des données
            byte[] encryptedData = Auth.AES.encrypt(audioData, key);
            
            // Calcul du hash SHA pour l'intégrité
            String audioHash = Auth.SHA.generateSHA256(audioData);
            
            // Calcul de la durée approximative (44.1 kHz, 16 bits, mono)
            int durationSeconds = (int) (audioData.length / (audioFormat.getSampleRate() * audioFormat.getFrameSize()));
            
            // Enregistrement dans la base de données
            saveRecordingToDatabase(recordingName, encryptedData, keyString, audioHash, durationSeconds);
            
            statusLabel.setText("Enregistrement sauvegardé avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors du chiffrement et de l'enregistrement: " + e.getMessage(), 
                                         "Erreur", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Erreur lors de la sauvegarde de l'enregistrement");
        }
        
        updateButtonStates();
    }
    
    /**
     * Enregistre les données audio dans la base de données.
     * 
     * @param name Nom de l'enregistrement
     * @param audioData Données audio chiffrées
     * @param encryptionKey Clé de chiffrement encodée en Base64
     * @param audioHash Hash SHA des données audio originales
     * @param duration Durée de l'enregistrement en secondes
     * @throws SQLException En cas d'erreur SQL
     */
    private void saveRecordingToDatabase(String name, byte[] audioData, String encryptionKey, String audioHash, int duration) 
            throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
            "INSERT INTO recordings (name, timestamp, duration, audio, encryption_key, audio_hash, user_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)"
        );
        
        pstmt.setString(1, name);
        pstmt.setString(2, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        pstmt.setInt(3, duration);
        pstmt.setBytes(4, audioData);
        pstmt.setString(5, encryptionKey);
        pstmt.setString(6, audioHash);
        pstmt.setInt(7, userId);
        
        pstmt.executeUpdate();
        pstmt.close();
        
        // Recharger la liste des enregistrements
        loadAudioRecordings();
    }
    
    /**
     * Récupère les données audio chiffrées depuis la base de données
     * @param recordingId Identifiant de l'enregistrement
     * @return Données audio chiffrées ou null si non trouvées
     */
    private byte[] getRecordingFromDatabase(int recordingId) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT audio, encryption_key FROM recordings WHERE id = ?"
            );
            pstmt.setInt(1, recordingId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                byte[] encryptedData = rs.getBytes("audio");
                return encryptedData;
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère le hash d'un enregistrement depuis la base de données
     * @param recordingId Identifiant de l'enregistrement
     * @return Hash de l'enregistrement ou chaine vide si non trouvé
     */
    private String getHashFromDatabase(int recordingId) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT audio_hash FROM recordings WHERE id = ?"
            );
            pstmt.setInt(1, recordingId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String hash = rs.getString("audio_hash");
                return hash;
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Joue un enregistrement audio
     * @param recordingId Identifiant de l'enregistrement à jouer
     */
    public void playRecording(int recordingId) {
        try {
            // Récupérer l'enregistrement depuis la base de données
            byte[] encryptedData = getRecordingFromDatabase(recordingId);
            
            if (encryptedData == null) {
                JOptionPane.showMessageDialog(this, 
                    "Enregistrement non trouvé.", 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Récupérer la clé de chiffrement
            String keyString = getEncryptionKeyFromDatabase(recordingId);
            SecretKey key = Auth.AES.decodeKeyFromBase64(keyString);
            
            // Décrypter les données audio avec AES
            byte[] audioData = Auth.AES.decrypt(encryptedData, key);
            
            // Vérifier l'intégrité avec SHA
            String audioHash = Auth.SHA.generateSHA256(audioData);
            String storedHash = getHashFromDatabase(recordingId);
            
            if (!audioHash.equals(storedHash)) {
                JOptionPane.showMessageDialog(this, 
                    "L'intégrité de l'enregistrement est compromise. Le fichier a été modifié.",
                    "Erreur d'intégrité", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Mettre à jour l'interface
            isPlaying = true;
            statusLabel.setText("Lecture en cours...");
            updateButtonStates();
            
            // Écrire les données audio dans un fichier temporaire
            File tempFile = File.createTempFile("audio_", ".wav");
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                // Écrire l'en-tête WAV
                writeWavHeader(out, audioData.length);
                out.write(audioData);
            }
            
            // Utiliser notre SimpleAudioPlayer pour la lecture dans un thread séparé
            playingThread = new Thread(() -> {
                boolean success = com.barbichetz.audio.SimpleAudioPlayer.playAudioFile(tempFile);
                
                // Mettre à jour l'interface après la lecture
                SwingUtilities.invokeLater(() -> {
                    isPlaying = false;
                    
                    if (!success) {
                        statusLabel.setText("Erreur de lecture");
                        JOptionPane.showMessageDialog(AudioRecorder.this,
                            "Erreur lors de la lecture audio.",
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    } else {
                        statusLabel.setText("Prêt");
                    }
                    
                    updateButtonStates();
                    
                    // Supprimer le fichier temporaire
                    tempFile.delete();
                });
            });
            
            playingThread.start();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Erreur: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
            
            isPlaying = false;
            statusLabel.setText("Erreur de lecture");
            updateButtonStates();
        }
    }

    /**
     * Récupère la clé de chiffrement depuis la base de données
     * @param recordingId Identifiant de l'enregistrement
     * @return Clé de chiffrement ou null si non trouvée
     */
    private String getEncryptionKeyFromDatabase(int recordingId) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT encryption_key FROM recordings WHERE id = ?"
            );
            pstmt.setInt(1, recordingId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String key = rs.getString("encryption_key");
                return key;
            }
            
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Écrit l'en-tête WAV dans un flux de sortie.
     * 
     * @param out Flux de sortie
     * @param audioDataLength Longueur des données audio
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    private void writeWavHeader(OutputStream out, int audioDataLength) throws IOException {
        // Format d'en-tête WAV
        byte[] header = new byte[44];
        
        long totalDataLen = audioDataLength + 36;
        int sampleRate = (int) audioFormat.getSampleRate();
        int channels = audioFormat.getChannels();
        int bitsPerSample = audioFormat.getSampleSizeInBits();
        long byteRate = sampleRate * channels * bitsPerSample / 8;
        
        // "RIFF" chunk
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        
        // "WAVE" format
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        
        // "fmt " sub-chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // Taille du sous-bloc fmt (16 pour PCM)
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;   // Format audio (1 pour PCM)
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        
        // Taux d'échantillonnage
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        
        // Débit binaire moyen
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        
        // Block align
        header[32] = (byte) (channels * bitsPerSample / 8);
        header[33] = 0;
        
        // Bits par échantillon
        header[34] = (byte) bitsPerSample;
        header[35] = 0;
        
        // "data" sub-chunk
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (audioDataLength & 0xff);
        header[41] = (byte) ((audioDataLength >> 8) & 0xff);
        header[42] = (byte) ((audioDataLength >> 16) & 0xff);
        header[43] = (byte) ((audioDataLength >> 24) & 0xff);
        
        // Écrire l'en-tête
        out.write(header);
    }
    
    /**
     * Arrête la lecture audio en cours.
     */
    private void stopPlayback() {
        if (!isPlaying) {
            return;
        }
        
        isPlaying = false;
        
        // Interrompre le thread de lecture
        if (playingThread != null && playingThread.isAlive()) {
            playingThread.interrupt();
            try {
                playingThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        statusLabel.setText("Lecture arrêtée");
        updateButtonStates();
    }
    
    /**
     * Supprime un enregistrement de la base de données.
     * 
     * @param recordingId Identifiant de l'enregistrement à supprimer
     */
    private void deleteRecording(int recordingId) {
        // Demander confirmation
        int choice = JOptionPane.showConfirmDialog(this, 
                "Êtes-vous sûr de vouloir supprimer cet enregistrement ?", 
                "Confirmation de suppression", 
                JOptionPane.YES_NO_OPTION);
        
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            // Supprimer l'enregistrement
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM recordings WHERE id = ?");
            pstmt.setInt(1, recordingId);
            pstmt.executeUpdate();
            pstmt.close();
            
            // Recharger la liste des enregistrements
            loadAudioRecordings();
            
            statusLabel.setText("Enregistrement supprimé");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression: " + e.getMessage(), 
                                         "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Met à jour l'état des boutons en fonction de l'état actuel.
     */
    private void updateButtonStates() {
        btnRecord.setEnabled(!isRecording && !isPlaying);
        btnStop.setEnabled(isRecording || isPlaying);
        btnPlay.setEnabled(!isRecording && !isPlaying && selectedRow >= 0);
        btnDelete.setEnabled(!isRecording && !isPlaying && selectedRow >= 0);
    }
} 