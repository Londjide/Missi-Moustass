package Auth;

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
 * @author Auth
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
                    // Si lancé directement, demander à l'utilisateur de se connecter
                    JOptionPane.showMessageDialog(null, 
                            "Veuillez vous connecter d'abord.", 
                            "Connexion requise", 
                            JOptionPane.INFORMATION_MESSAGE);
                    Connexion connexion = new Connexion();
                    connexion.afficher();
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
    	this.audioFormat = new AudioFormat(44100, 8, 1, true, true);
        this.userId = userId;
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
        JLabel titleLabel = new JLabel("");
        titleLabel.setFont(new Font("Helvetica Neue", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Étiquette utilisateur
        userLabel = new JLabel("Utilisateur connecté: ");
        headerPanel.add(userLabel, BorderLayout.SOUTH);
        
        // Bouton de déconnexion
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                Connexion connexion = new Connexion();
                connexion.afficher();
            }
        });
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        contentPane.add(headerPanel, BorderLayout.NORTH);
        
        // Panneau du tableau
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // Création du modèle de tableau avec colonnes
        tableModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableModel.addColumn("ID");
        tableModel.addColumn("Nom");
        tableModel.addColumn("Horodatage");
        tableModel.addColumn("Durée (sec)");
        
        
        // Création du tableau
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedRow = table.getSelectedRow();
                btnPlay.setEnabled(selectedRow != -1);
                btnDelete.setEnabled(selectedRow != -1);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(tablePanel, BorderLayout.CENTER);
        
        // Panneau de contrôle
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        // Panneau de boutons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        
        btnRecord = new JButton("Enregistrer");
        btnRecord.setBackground(new Color(240, 128, 128));
        btnRecord.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isRecording) {
                    startRecording();
                }
            }
        });
        btnStop = new JButton("Arrêter");
        btnStop.setEnabled(false);
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isRecording) {
                    stopRecording();
                } else if (isPlaying) {
                    stopPlaying();
                }
            }
        });
        
        btnPlay = new JButton("Lire");
        btnPlay.setBackground(new Color(144, 238, 144));
        btnPlay.setEnabled(false);
        btnPlay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isPlaying && selectedRow != -1) {
                    try {
                        playRecording();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        
        btnDelete = new JButton("Supprimer");
        btnDelete.setBackground(new Color(255, 165, 0));
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedRow != -1) {
                    deleteRecording();
                }
            }
        });
        
        buttonPanel.add(btnRecord);
        buttonPanel.add(btnStop);
        buttonPanel.add(btnPlay);
        buttonPanel.add(btnDelete);
        
        controlPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Étiquette d'état
        statusLabel = new JLabel("Prêt");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        controlPanel.add(statusLabel, BorderLayout.SOUTH);
        contentPane.add(controlPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Charge tous les enregistrements audio depuis la base de données et les affiche dans le tableau.
     * Pour chaque enregistrement, déchiffre les données audio et vérifie leur intégrité.
     */
    private void loadAudioRecordings() {
        try {
            // Effacement du tableau
            tableModel.setRowCount(0);
            
            
            

            // Interrogation de la base de données pour tous les enregistrements
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id, name, timestamp, duration, audio, encryption_key, audio_hash " +
                    "FROM recordings WHERE user_id = ? ORDER BY timestamp DESC");
            String sql = "SELECT id, name, timestamp, duration, audio, encryption_key, audio_hash FROM recordings WHERE user_id = ? ORDER BY timestamp DESC";
            pstmt.setInt(1, userId); // Use the userId from the current instance
            ResultSet rs = pstmt.executeQuery();
            

            // Ajout de chaque enregistrement au tableau
            while (rs.next()) {
                Object[] row = new Object[4];
                row[0] = rs.getInt("id");
                row[1] = rs.getString("name");
                row[2] = rs.getString("timestamp");
                row[3] = rs.getInt("duration");

                // Récupération des données audio chiffrées, de la clé de chiffrement et du hash stocké
                byte[] encryptedAudioData = rs.getBytes("audio");
                String keyBase64 = rs.getString("encryption_key");
                String storedHash = rs.getString("audio_hash");

                // Décodage de la clé de chiffrement et déchiffrement des données audio
                SecretKey secretKey = AES.decodeKeyFromBase64(keyBase64);
                byte[] decryptedAudioData = AES.decrypt(encryptedAudioData, secretKey);

                // Recalcul du hash SHA-256 des données audio déchiffrées
                String computedHash = computeSHA256Hash(decryptedAudioData);

                // Vérification si le hash calculé correspond au hash stocké
                if (computedHash.equals(storedHash)) {
                    System.out.println("Intégrité audio vérifiée");
                } else {
                    System.out.println("Échec de la vérification d'intégrité audio");
                }

                // Taille des données audio déchiffrées (pour information)
                System.out.println("Taille des données audio déchiffrées: " + decryptedAudioData.length);

                tableModel.addRow(row);
            }

            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Échec du chargement des enregistrements: " + e.getMessage(),
                                          "Erreur de base de données", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Démarre l'enregistrement audio.
     * Configure le format audio, ouvre la ligne audio et commence à capturer les données
     * dans un thread séparé.
     */
    private void startRecording() {
        try {
            // Configuration du format audio - optimisé pour la compatibilité macOS
            // Utilisation de stéréo (2 canaux) et big-endian (true) pour une meilleure compatibilité Mac
            audioFormat = new AudioFormat(44100, 8, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            
            // Vérification si le système prend en charge le format audio
            if (!AudioSystem.isLineSupported(info)) {
                JOptionPane.showMessageDialog(this, "Format audio non pris en charge par ce système", 
                                            "Erreur d'enregistrement", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Obtention de la ligne audio - utilisation du mixeur sélectionné si disponible
            if (selectedMixer != null) {
                Mixer mixer = AudioSystem.getMixer(selectedMixer);
                audioLine = (TargetDataLine) mixer.getLine(info);
            } else {
                audioLine = (TargetDataLine) AudioSystem.getLine(info);
            }
            
            audioLine.open(audioFormat);
            audioLine.start();
            
            // Création d'un flux pour stocker les données capturées
            outputStream = new ByteArrayOutputStream();
            isRecording = true;
            
            // Mise à jour de l'interface utilisateur
            statusLabel.setText("Enregistrement en cours...");
            btnRecord.setEnabled(false);
            btnStop.setEnabled(true);
            btnPlay.setEnabled(false);
            btnDelete.setEnabled(false);
            
            // Démarrage du thread d'enregistrement
            recordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize();
                        byte[] buffer = new byte[bufferSize];
                        int bytesRead;
                        
                        while (isRecording) {
                            bytesRead = audioLine.read(buffer, 0, buffer.length);
                            if (bytesRead > 0) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(AudioRecorder.this, 
                                                         "Erreur pendant l'enregistrement: " + e.getMessage(), 
                                                         "Erreur d'enregistrement", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }
            });
            
            recordingThread.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Impossible de démarrer l'enregistrement: " + e.getMessage() + 
                "\nEssayez de sélectionner un autre périphérique audio ou vérifiez les permissions système.", 
                "Erreur d'enregistrement", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Arrête l'enregistrement audio en cours.
     * Ferme la ligne audio, attend la fin du thread d'enregistrement,
     * demande un nom pour l'enregistrement et le sauvegarde dans la base de données.
     */
    private void stopRecording() {
        if (isRecording && audioLine != null) {
            // Arrêt de l'enregistrement
            isRecording = false;
            audioLine.stop();
            audioLine.close();
            
            try {
                // Attente de la fin du thread d'enregistrement
                recordingThread.join();
                
                // Récupération des données audio enregistrées
                byte[] audioData = outputStream.toByteArray();
                
                // Si nous avons des données enregistrées, les sauvegarder
                if (audioData.length > 0) {
                    // Calcul de la durée
                    float durationInSeconds = audioData.length / (audioFormat.getSampleRate() * audioFormat.getFrameSize());
                    
                    // Génération d'un nom pour l'enregistrement
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    String name = "Enregistrement " + timestamp;
                    
                    // Demande à l'utilisateur de confirmer ou de modifier le nom
                    name = JOptionPane.showInputDialog(this, "Entrez un nom pour cet enregistrement:", name);
                    
                    if (name != null && !name.trim().isEmpty()) {
                        // Sauvegarde de l'enregistrement dans la base de données
                        saveRecordingToDatabase(name, timestamp, (int) durationInSeconds, audioData);
                        
                        // Rechargement de la liste des enregistrements
                        loadAudioRecordings();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Mise à jour de l'interface utilisateur
            statusLabel.setText("Prêt");
            btnRecord.setEnabled(true);
            btnStop.setEnabled(false);
            btnPlay.setEnabled(selectedRow != -1);
            btnDelete.setEnabled(selectedRow != -1);
        }
    }
    
    /**
     * Sauvegarde un enregistrement audio dans la base de données.
     * Les données audio sont chiffrées avec AES et un hash SHA-256 est calculé pour garantir l'intégrité.
     * 
     * @param name Nom de l'enregistrement
     * @param timestamp Horodatage de l'enregistrement
     * @param duration Durée de l'enregistrement en secondes
     * @param audioData Données audio à sauvegarder
     */
    private void saveRecordingToDatabase(String name, String timestamp, int duration, byte[] audioData) {
        try {
            // Génération d'une clé secrète AES
            SecretKey secretKey = AES.generateSecretKey();

            // Chiffrement des données audio
            byte[] encryptedAudioData = AES.encrypt(audioData, secretKey);

            // Calcul du hash SHA-256 des données audio
            String audioHash = computeSHA256Hash(audioData);

            // Sauvegarde des données audio chiffrées, de la clé de chiffrement et du hash dans la base de données
            String sql = "INSERT INTO recordings (name, timestamp, duration, audio, encryption_key, audio_hash, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, timestamp);
            pstmt.setInt(3, duration);
            pstmt.setBytes(4, encryptedAudioData);
            pstmt.setString(5, AES.encodeKeyToBase64(secretKey)); // Stockage de la clé de chiffrement en tant que chaîne Base64
            pstmt.setString(6, audioHash); // Stockage du hash
          pstmt.setInt(7, userId);  // Ensure you pass the correct logged-in user's ID
            pstmt.executeUpdate();
            pstmt.close();
            
            statusLabel.setText("Enregistrement sauvegardé avec succès");

            // Auto-sauvegarde de l'enregistrement dans un fichier WAV
            autoSaveRecordingToFile(name, timestamp, audioData);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Échec de la sauvegarde de l'enregistrement: " + e.getMessage(), 
                                          "Erreur de base de données", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Lit l'enregistrement audio sélectionné dans le tableau.
     * Récupère les données audio chiffrées, les déchiffre et les lit dans un thread séparé.
     * 
     * @throws Exception En cas d'erreur lors de la lecture
     */
    private void playRecording() throws Exception {
        if (selectedRow == -1) return;
        
        // Récupération de l'ID de l'enregistrement depuis le tableau
        int recordingId = (int) tableModel.getValueAt(selectedRow, 0);
        
        try {
            // Récupération des données audio et de la clé de chiffrement depuis la base de données
            String sql = "SELECT audio, encryption_key FROM recordings WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, recordingId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Récupération des données audio chiffrées et de la clé de chiffrement
                byte[] encryptedAudioData = rs.getBytes("audio");
                String keyBase64 = rs.getString("encryption_key");
                
                // Déchiffrement des données audio
                SecretKey secretKey = AES.decodeKeyFromBase64(keyBase64);
                final byte[] audioData = AES.decrypt(encryptedAudioData, secretKey);
                
                // Mise à jour de l'interface utilisateur
                isPlaying = true;
                statusLabel.setText("Lecture en cours...");
                btnRecord.setEnabled(false);
                btnStop.setEnabled(true);
                btnPlay.setEnabled(false);
                btnDelete.setEnabled(false);
                
                // Démarrage du thread de lecture
                playingThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Utilisation du même format audio que lors de l'enregistrement pour la cohérence
                            AudioFormat format = audioFormat;
                            
                            // Création d'un flux d'entrée audio à partir des données audio déchiffrées
                            AudioInputStream audioStream = new AudioInputStream(
                                new ByteArrayInputStream(audioData),
                                format,
                                audioData.length / format.getFrameSize()
                            );
                            
                            // Pour la compatibilité macOS, utilisation directe de DataLine plutôt que Clip
                            DataLine.Info dataLineInfo = new DataLine.Info(
                                SourceDataLine.class, format);
                            
                            final SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                            dataLine.open(format);
                            dataLine.start();
                            
                            int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
                            byte[] buffer = new byte[bufferSize];
                            int bytesRead = 0;
                            
                            // Lecture de l'audio
                            while ((bytesRead = audioStream.read(buffer, 0, buffer.length)) != -1 && isPlaying) {
                                dataLine.write(buffer, 0, bytesRead);
                            }
                            
                            // Nettoyage
                            dataLine.drain();
                            dataLine.stop();
                            dataLine.close();
                            audioStream.close();
                            
                            SwingUtilities.invokeLater(() -> {
                                isPlaying = false;
                                statusLabel.setText("Prêt");
                                btnRecord.setEnabled(true);
                                btnStop.setEnabled(false);
                                btnPlay.setEnabled(selectedRow != -1);
                                btnDelete.setEnabled(selectedRow != -1);
                            });
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                            SwingUtilities.invokeLater(() -> {
                                isPlaying = false;
                                statusLabel.setText("Erreur de lecture");
                                btnRecord.setEnabled(true);
                                btnStop.setEnabled(false);
                                btnPlay.setEnabled(selectedRow != -1);
                                btnDelete.setEnabled(selectedRow != -1);
                                
                                JOptionPane.showMessageDialog(AudioRecorder.this, 
                                    "Erreur pendant la lecture: " + e.getMessage() + 
                                    "\nEssayez de redémarrer l'application ou de sélectionner un autre périphérique audio.", 
                                    "Erreur de lecture", JOptionPane.ERROR_MESSAGE);
                            });
                        }
                    }
                });
                
                playingThread.start();
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Échec de la récupération de l'enregistrement: " + e.getMessage(), 
                                         "Erreur de base de données", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Arrête la lecture audio en cours.
     * Met à jour l'interface utilisateur pour refléter l'arrêt de la lecture.
     */
    private void stopPlaying() {
        if (isPlaying) {
            isPlaying = false;
            
            // Mise à jour de l'interface utilisateur (au cas où l'écouteur de ligne ne se déclenche pas)
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Prêt");
                btnRecord.setEnabled(true);
                btnStop.setEnabled(false);
                btnPlay.setEnabled(selectedRow != -1);
                btnDelete.setEnabled(selectedRow != -1);
            });
        }
    }
    
    /**
     * Supprime l'enregistrement audio sélectionné dans le tableau.
     * Demande une confirmation à l'utilisateur avant la suppression.
     */
    private void deleteRecording() {
        if (selectedRow == -1) return;
        
        // Récupération de l'ID et du nom de l'enregistrement depuis le tableau
        int recordingId = (int) tableModel.getValueAt(selectedRow, 0);
        String recordingName = (String) tableModel.getValueAt(selectedRow, 1);
        
        // Demande de confirmation
        int confirm = JOptionPane.showConfirmDialog(this, 
                                                  "Êtes-vous sûr de vouloir supprimer l'enregistrement '" + recordingName + "' ?", 
                                                  "Confirmer la suppression", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Suppression de l'enregistrement de la base de données
                String sql = "DELETE FROM recordings WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, recordingId);
                pstmt.executeUpdate();
                pstmt.close();
                
                // Suppression de la ligne du tableau
                tableModel.removeRow(selectedRow);
                
                // Mise à jour de l'interface utilisateur
                selectedRow = -1;
                btnPlay.setEnabled(false);
                btnDelete.setEnabled(false);
                statusLabel.setText("Enregistrement supprimé");
                
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Échec de la suppression de l'enregistrement: " + e.getMessage(), 
                                             "Erreur de base de données", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    /**
     * Permet l'auto-sauvegarde des enregistrements dans un dossier.
     * À appeler après la sauvegarde en base de données.
     *
     * @param name Nom de l'enregistrement
     * @param timestamp Horodatage
     * @param audioData Données audio
     */
    private void autoSaveRecordingToFile(String name, String timestamp, byte[] audioData) {
        try {
            // Création du dossier d'enregistrements s'il n'existe pas
            File recordingsDir = new File("recordings");
            if (!recordingsDir.exists()) {
                recordingsDir.mkdir();
            }

            // Création d'un nom de fichier valide
            String fileName = name.replaceAll("[^a-zA-Z0-9.-]", "_") + ".wav";
            File outputFile = new File(recordingsDir, fileName);

            // Configuration du format audio
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);

            // Création d'un flux d'entrée audio à partir des données
            AudioInputStream audioStream = new AudioInputStream(
                new ByteArrayInputStream(audioData),
                format,
                audioData.length / format.getFrameSize()
            );

            // Écriture des données audio dans un fichier WAV
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, outputFile);

            System.out.println("Enregistrement auto-sauvegardé: " + outputFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Échec de l'auto-sauvegarde de l'enregistrement: " + e.getMessage());
        }
    }

    /**
     * Calcule le hash SHA-256 des données fournies.
     * 
     * @param data Les données dont on veut calculer le hash
     * @return Une chaîne hexadécimale représentant le hash SHA-256, ou null en cas d'erreur
     */
    private String computeSHA256Hash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data);

            // Conversion des bytes du hash en chaîne hexadécimale
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Libère les ressources lors de la fermeture de l'application.
     */
    @Override
    public void dispose() {
        // Nettoyage des ressources lorsque l'application est fermée
        try {
            if (isRecording) {
                stopRecording();
            }

            if (isPlaying) {
                stopPlaying();
            }

            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        super.dispose();
    
}
}