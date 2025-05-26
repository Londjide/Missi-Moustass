package view;

import controller.AudioRecorderController;
import model.AudioRecording;
import service.NotificationService;
import service.SQLiteDatabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.awt.Toolkit;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.awt.Desktop;

/**
 * Vue pour l'enregistreur audio.
 * Cette classe suit le principe de responsabilité unique (SRP) en gérant
 * uniquement
 * l'interface utilisateur pour l'enregistrement audio.
 */
public class AudioRecorderView extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnRecord;
    private JButton btnStop;
    private JButton btnPlay;
    private JButton btnDelete;
    private JButton btnShare;
    private JButton btnExportKey;
    private JButton btnOpenWithKey;
    private JButton btnDownload;
    private JButton btnNotifications;
    private JLabel statusLabel;
    private JLabel userLabel;
    private JLabel timerLabel;
    private Timer recordingTimer;
    private int recordingSeconds = 0;

    private final AudioRecorderController controller;
    private NotificationService notificationService;
    private int selectedRecordingId = -1;
    private int currentUserId;
    private boolean isRecording = false;
    private boolean isPlaying = false;

    /**
     * Constructeur qui initialise l'interface d'enregistrement audio.
     * 
     * @param userId L'ID de l'utilisateur connecté
     */
    public AudioRecorderView(int userId) {
        this.currentUserId = userId;
        this.controller = new AudioRecorderController(userId);
        this.notificationService = new NotificationService(new SQLiteDatabaseService());

        initializeUI();
        loadUserRecordings();

        // Afficher l'utilisateur connecté
        userLabel.setText("Utilisateur: " + userId);
    }

    /**
     * Met à jour l'ID de l'utilisateur courant et rafraîchit la vue.
     * 
     * @param newUserId Le nouvel ID de l'utilisateur
     */
    public void updateUser(int newUserId) {
        this.currentUserId = newUserId;
        controller.updateUserId(newUserId);
        loadUserRecordings();
        userLabel.setText("Utilisateur: " + newUserId);

        // Mettre à jour le nombre de notifications non lues
        int unreadCount = notificationService.countUnreadNotifications(currentUserId);
        if (unreadCount > 0) {
            btnNotifications.setText("Notifications (" + unreadCount + ")");
        } else {
            btnNotifications.setText("Notifications");
        }
    }

    /**
     * Initialise l'interface utilisateur.
     */
    private void initializeUI() {
        setTitle("Enregistreur Audio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 600, 400);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        // Panel pour le titre et l'utilisateur
        JPanel headerPanel = new JPanel(new BorderLayout());

        // Titre
        JLabel titleLabel = new JLabel("Enregistreur Audio");
        titleLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Panel pour l'utilisateur et le bouton de déconnexion
        JPanel userPanel = new JPanel(new BorderLayout());

        // Étiquette utilisateur
        userLabel = new JLabel("Utilisateur: " + currentUserId);
        userPanel.add(userLabel, BorderLayout.CENTER);

        // Panel pour les boutons de droite
        JPanel rightButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Bouton notifications
        btnNotifications = new JButton("Notifications");
        btnNotifications.addActionListener(e -> showNotifications());
        rightButtonsPanel.add(btnNotifications);

        // Bouton de déconnexion
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.addActionListener(e -> logout());
        rightButtonsPanel.add(logoutButton);

        userPanel.add(rightButtonsPanel, BorderLayout.EAST);

        headerPanel.add(userPanel, BorderLayout.SOUTH);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Panel pour le tableau
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout(0, 0));

        // Tableau des enregistrements
        String[] columnNames = { "ID", "Nom", "Date", "Durée" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedRecordingId = (int) tableModel.getValueAt(selectedRow, 0);
                } else {
                    selectedRecordingId = -1;
                }
                updateButtonStates();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        contentPane.add(tablePanel, BorderLayout.CENTER);

        // Panel pour les boutons et le timer
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout(0, 0));

        // Panel pour les boutons principaux
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        btnRecord = new JButton("Enregistrer");
        btnStop = new JButton("Arrêter");
        btnPlay = new JButton("Écouter");
        btnDelete = new JButton("Supprimer");

        buttonPanel.add(btnRecord);
        buttonPanel.add(btnStop);
        buttonPanel.add(btnPlay);
        buttonPanel.add(btnDelete);

        // Panel distinct pour les boutons de partage
        JPanel sharingPanel = new JPanel();
        sharingPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        sharingPanel.setBorder(BorderFactory.createTitledBorder("Options de partage"));

        btnShare = new JButton("Partager");
        btnExportKey = new JButton("Exporter la clé");
        btnOpenWithKey = new JButton("Ouvrir avec clé");
        btnDownload = new JButton("Télécharger");

        sharingPanel.add(btnShare);
        sharingPanel.add(btnExportKey);
        sharingPanel.add(btnOpenWithKey);
        sharingPanel.add(btnDownload);

        // Ajouter les deux panels de boutons
        JPanel allButtonsPanel = new JPanel(new BorderLayout());
        allButtonsPanel.add(buttonPanel, BorderLayout.NORTH);
        allButtonsPanel.add(sharingPanel, BorderLayout.CENTER);

        controlPanel.add(allButtonsPanel, BorderLayout.CENTER);

        // Panel pour le timer et le statut
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        timerLabel = new JLabel("00:00");
        timerLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        statusPanel.add(timerLabel);

        statusLabel = new JLabel(" ");
        statusPanel.add(statusLabel);

        controlPanel.add(statusPanel, BorderLayout.SOUTH);

        contentPane.add(controlPanel, BorderLayout.SOUTH);

        // Initialiser le timer
        recordingTimer = new Timer(1000, e -> {
            recordingSeconds++;
            updateTimerLabel();
        });

        // Ajouter les listeners pour les boutons
        btnRecord.addActionListener(e -> startRecording());
        btnStop.addActionListener(e -> stopOperation());
        btnPlay.addActionListener(e -> playRecording());
        btnDelete.addActionListener(e -> deleteRecording());
        btnShare.addActionListener(e -> shareRecording());
        btnExportKey.addActionListener(e -> exportEncryptionKey());
        btnOpenWithKey.addActionListener(e -> openRecordingWithKey());
        btnDownload.addActionListener(e -> downloadRecording());

        updateButtonStates();
    }

    /**
     * Met à jour l'état des boutons en fonction de l'état de l'application.
     * Cette méthode est simplifiée pour être plus robuste.
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedRecordingId != -1;

        // Approche directe pour éviter tout problème de synchronisation
        if (isRecording) {
            btnRecord.setEnabled(false);
            btnStop.setEnabled(true);
            btnPlay.setEnabled(false);
            btnDelete.setEnabled(false);
            btnShare.setEnabled(false);
            btnExportKey.setEnabled(false);
            btnOpenWithKey.setEnabled(false);
            btnDownload.setEnabled(false);
            statusLabel.setText("Enregistrement en cours...");
        } else if (isPlaying) {
            btnRecord.setEnabled(false);
            btnStop.setEnabled(true);
            btnPlay.setEnabled(false);
            btnDelete.setEnabled(false);
            btnShare.setEnabled(false);
            btnExportKey.setEnabled(false);
            btnOpenWithKey.setEnabled(false);
            btnDownload.setEnabled(false);
            statusLabel.setText("Lecture en cours...");
        } else {
            btnRecord.setEnabled(true);
            btnStop.setEnabled(false);
            btnPlay.setEnabled(hasSelection);
            btnDelete.setEnabled(hasSelection);
            btnShare.setEnabled(hasSelection);
            btnExportKey.setEnabled(hasSelection);
            btnOpenWithKey.setEnabled(hasSelection);
            btnDownload.setEnabled(hasSelection);
        }
    }

    /**
     * Charge la liste des enregistrements de l'utilisateur.
     */
    private void loadUserRecordings() {
        try {
            // Effacer le tableau
            tableModel.setRowCount(0);

            // Récupérer les enregistrements
            List<AudioRecording> recordings = controller.getRecordings("recordings");

            // Ajouter les enregistrements au tableau
            for (AudioRecording recording : recordings) {
                Object[] rowData = {
                        recording.getId(),
                        recording.getName(),
                        controller.formatDateForDisplay(recording.getTimestamp()),
                        controller.formatDuration(recording.getDuration())
                };

                tableModel.addRow(rowData);
            }

            // Réinitialiser la sélection
            selectedRecordingId = -1;
            updateButtonStates();

            // Mettre à jour le nombre de notifications non lues
            int unreadCount = notificationService.countUnreadNotifications(currentUserId);
            if (unreadCount > 0) {
                btnNotifications.setText("Notifications (" + unreadCount + ")");
            } else {
                btnNotifications.setText("Notifications");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des enregistrements: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Démarre l'enregistrement audio.
     */
    private void startRecording() {
        try {
            controller.startRecording();
            isRecording = true;
            recordingSeconds = 0;
            recordingTimer.start();
            updateButtonStates();
            statusLabel.setText("Enregistrement en cours...");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    /**
     * Arrête l'opération en cours (enregistrement ou lecture).
     */
    private void stopOperation() {
        try {
            if (isRecording) {
                controller.stopRecording();
                isRecording = false;
                recordingTimer.stop();

                // Réactiver tous les boutons après l'arrêt de l'enregistrement
                btnRecord.setEnabled(true);
                btnStop.setEnabled(false);
                btnPlay.setEnabled(selectedRecordingId != -1);
                btnDelete.setEnabled(selectedRecordingId != -1);
                btnShare.setEnabled(selectedRecordingId != -1);
                btnExportKey.setEnabled(selectedRecordingId != -1);
                btnOpenWithKey.setEnabled(selectedRecordingId != -1);
                btnDownload.setEnabled(selectedRecordingId != -1);

                statusLabel.setText("Enregistrement terminé");
                loadUserRecordings();
            } else if (isPlaying) {
                controller.stopPlaying();
                isPlaying = false;

                // Réactiver tous les boutons après l'arrêt de la lecture
                btnRecord.setEnabled(true);
                btnStop.setEnabled(false);
                btnPlay.setEnabled(selectedRecordingId != -1);
                btnDelete.setEnabled(selectedRecordingId != -1);
                btnShare.setEnabled(selectedRecordingId != -1);
                btnExportKey.setEnabled(selectedRecordingId != -1);
                btnOpenWithKey.setEnabled(selectedRecordingId != -1);
                btnDownload.setEnabled(selectedRecordingId != -1);

                statusLabel.setText("Lecture arrêtée");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Erreur: " + e.getMessage());
        }
    }

    /**
     * Joue l'enregistrement sélectionné.
     */
    private void playRecording() {
        if (selectedRecordingId != -1) {
            try {
                // Désactiver tous les boutons sauf Arrêter pendant la lecture
                btnRecord.setEnabled(false);
                btnStop.setEnabled(true);
                btnPlay.setEnabled(false);
                btnDelete.setEnabled(false);
                btnShare.setEnabled(false);
                btnExportKey.setEnabled(false);
                btnOpenWithKey.setEnabled(false);
                btnDownload.setEnabled(false);

                isPlaying = true;
                statusLabel.setText("Lecture en cours...");

                // Créer un thread séparé pour la lecture
                new Thread(() -> {
                    try {
                        // Utiliser directement l'ID de l'enregistrement
                        controller.playRecording(selectedRecordingId);

                        // Une fois la lecture terminée, réactiver tous les boutons
                        SwingUtilities.invokeLater(() -> {
                            isPlaying = false;
                            btnRecord.setEnabled(true);
                            btnStop.setEnabled(false);
                            btnPlay.setEnabled(selectedRecordingId != -1);
                            btnDelete.setEnabled(selectedRecordingId != -1);
                            btnShare.setEnabled(selectedRecordingId != -1);
                            btnExportKey.setEnabled(selectedRecordingId != -1);
                            btnOpenWithKey.setEnabled(selectedRecordingId != -1);
                            btnDownload.setEnabled(selectedRecordingId != -1);
                            statusLabel.setText("Lecture terminée");
                        });
                    } catch (Exception e) {
                        // En cas d'erreur, réactiver tous les boutons
                        SwingUtilities.invokeLater(() -> {
                            isPlaying = false;
                            btnRecord.setEnabled(true);
                            btnStop.setEnabled(false);
                            btnPlay.setEnabled(selectedRecordingId != -1);
                            btnDelete.setEnabled(selectedRecordingId != -1);
                            btnShare.setEnabled(selectedRecordingId != -1);
                            btnExportKey.setEnabled(selectedRecordingId != -1);
                            btnOpenWithKey.setEnabled(selectedRecordingId != -1);
                            btnDownload.setEnabled(selectedRecordingId != -1);
                            statusLabel.setText("Erreur de lecture: " + e.getMessage());
                        });
                        e.printStackTrace();
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
                isPlaying = false;
                statusLabel.setText("Erreur de lecture: " + e.getMessage());
                updateButtonStates();
            }
        }
    }

    /**
     * Supprime l'enregistrement sélectionné.
     */
    private void deleteRecording() {
        if (selectedRecordingId == -1) {
            return;
        }

        int confirmResult = JOptionPane.showConfirmDialog(this,
                "Êtes-vous sûr de vouloir supprimer cet enregistrement ?",
                "Confirmation de suppression", JOptionPane.YES_NO_OPTION);

        if (confirmResult == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = controller.deleteRecording(selectedRecordingId);

                if (deleted) {
                    statusLabel.setText("Enregistrement supprimé avec succès.");
                    loadUserRecordings(); // Recharger la liste
                } else {
                    statusLabel.setText("Échec de la suppression de l'enregistrement.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la suppression de l'enregistrement: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Ouvre la boîte de dialogue pour partager l'enregistrement sélectionné.
     */
    private void shareRecording() {
        if (selectedRecordingId == -1) {
            return;
        }

        // Récupérer la liste des emails des utilisateurs
        List<String> userEmails = controller.getUserEmails();

        if (userEmails.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Aucun utilisateur disponible pour le partage.",
                    "Partage impossible", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Créer la boîte de dialogue de partage
        JDialog shareDialog = new JDialog(this, "Partager l'enregistrement", true);
        shareDialog.setLayout(new BorderLayout(10, 10));
        shareDialog.setSize(400, 200);
        shareDialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel selectLabel = new JLabel("Sélectionnez l'utilisateur destinataire:");
        JComboBox<String> userComboBox = new JComboBox<>(userEmails.toArray(new String[0]));

        formPanel.add(selectLabel, BorderLayout.NORTH);
        formPanel.add(userComboBox, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton confirmButton = new JButton("Partager");
        JButton cancelButton = new JButton("Annuler");

        confirmButton.addActionListener(e -> {
            try {
                String targetEmail = (String) userComboBox.getSelectedItem();
                if (targetEmail != null && !targetEmail.isEmpty()) {
                    // Afficher un indicateur de progression
                    shareDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    confirmButton.setEnabled(false);

                    // Ajouter un délai pour que l'interface se mette à jour
                    SwingUtilities.invokeLater(() -> {
                        try {
                            boolean success = controller.shareRecording(selectedRecordingId, targetEmail);
                            shareDialog.setCursor(Cursor.getDefaultCursor());
                            confirmButton.setEnabled(true);

                            if (success) {
                                JOptionPane.showMessageDialog(shareDialog,
                                        "Enregistrement partagé avec succès avec " + targetEmail,
                                        "Partage réussi", JOptionPane.INFORMATION_MESSAGE);
                                shareDialog.dispose();
                            } else {
                                // Vérifier si c'est un problème d'email qui contient des caractères spéciaux
                                if (targetEmail.contains("@missie.mu")) {
                                    // Suggérer une solution
                                    int retry = JOptionPane.showConfirmDialog(shareDialog,
                                            "Problème lors du partage avec " + targetEmail + ".\n" +
                                                    "Ce domaine peut causer des problèmes. Voulez-vous essayer avec une version normalisée de l'email?",
                                            "Problème avec le domaine email",
                                            JOptionPane.YES_NO_OPTION);

                                    if (retry == JOptionPane.YES_OPTION) {
                                        // Normaliser l'email et réessayer
                                        String normalizedEmail = targetEmail.toLowerCase().trim();
                                        boolean retrySuccess = controller.shareRecording(selectedRecordingId,
                                                normalizedEmail);

                                        if (retrySuccess) {
                                            JOptionPane.showMessageDialog(shareDialog,
                                                    "Enregistrement partagé avec succès avec la version normalisée: "
                                                            + normalizedEmail,
                                                    "Partage réussi", JOptionPane.INFORMATION_MESSAGE);
                                            shareDialog.dispose();
                                            return;
                                        }
                                    }
                                }

                                JOptionPane.showMessageDialog(shareDialog,
                                        "Échec du partage de l'enregistrement avec " + targetEmail +
                                                ".\nVérifiez les logs pour plus de détails.",
                                        "Erreur de partage", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            shareDialog.setCursor(Cursor.getDefaultCursor());
                            confirmButton.setEnabled(true);
                            JOptionPane.showMessageDialog(shareDialog,
                                    "Exception lors du partage: " + ex.getMessage(),
                                    "Erreur grave", JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(shareDialog,
                        "Erreur lors du partage: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        cancelButton.addActionListener(e -> shareDialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        shareDialog.add(formPanel, BorderLayout.CENTER);
        shareDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Afficher un message explicatif sur le partage
        JTextArea infoArea = new JTextArea(
                "Le partage utilise le chiffrement à clé publique (RSA) pour sécuriser " +
                        "la transmission des enregistrements. L'enregistrement reste chiffré et " +
                        "seul le destinataire pourra le déchiffrer avec sa clé privée.");
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(shareDialog.getBackground());
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        shareDialog.add(infoArea, BorderLayout.NORTH);

        shareDialog.setVisible(true);
    }

    /**
     * Exporte la clé d'un enregistrement.
     */
    private void exportEncryptionKey() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un enregistrement.",
                    "Aucun enregistrement sélectionné", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int recordingId = (int) tableModel.getValueAt(selectedRow, 0);

            // Récupérer la clé et les informations de partage
            Map<String, String> sharingInfo = controller.exportEncryptionKey(recordingId);

            // Créer un texte de partage
            String sharingText = "Pour lire cet enregistrement audio, vous aurez besoin de:\n\n" +
                    "1. Clé de déchiffrement: " + sharingInfo.get("key") + "\n" +
                    "2. Fichier: " + sharingInfo.get("fileName") + "\n" +
                    "3. Notre application AudioRecorder\n\n" +
                    "Utilisez l'option 'Ouvrir avec clé' dans l'application.";

            // Afficher une boîte de dialogue avec les informations de partage
            JTextArea textArea = new JTextArea(sharingText);
            textArea.setEditable(false);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 200));

            // Ajouter un bouton pour copier dans le presse-papiers
            JButton copyButton = new JButton("Copier dans le presse-papiers");
            copyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    StringSelection stringSelection = new StringSelection(sharingText);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                    JOptionPane.showMessageDialog(AudioRecorderView.this,
                            "Les informations ont été copiées dans le presse-papiers.",
                            "Copie réussie", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(copyButton, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(AudioRecorderView.this, panel,
                    "Informations de partage manuel", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(AudioRecorderView.this,
                    "Erreur lors de l'exportation de la clé: " + ex.getMessage(),
                    "Erreur d'exportation", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Ouvre un enregistrement avec une clé.
     */
    private void openRecordingWithKey() {
        // Créer un formulaire pour saisir le chemin du fichier et la clé
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField filePathField = new JTextField();
        JButton browseButton = new JButton("Parcourir...");
        JTextField keyField = new JTextField();

        JPanel filePathPanel = new JPanel(new BorderLayout());
        filePathPanel.add(filePathField, BorderLayout.CENTER);
        filePathPanel.add(browseButton, BorderLayout.EAST);

        formPanel.add(new JLabel("Chemin du fichier:"));
        formPanel.add(filePathPanel);
        formPanel.add(new JLabel("Clé de déchiffrement:"));
        formPanel.add(keyField);

        // Gestionnaire d'événement pour le bouton Parcourir
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers audio chiffrés (*.enc)", "enc"));

                if (fileChooser.showOpenDialog(AudioRecorderView.this) == JFileChooser.APPROVE_OPTION) {
                    filePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        // Afficher le formulaire
        int result = JOptionPane.showConfirmDialog(AudioRecorderView.this, formPanel,
                "Ouvrir un fichier avec une clé", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Si l'utilisateur a validé le formulaire
        if (result == JOptionPane.OK_OPTION) {
            String filePath = filePathField.getText().trim();
            String key = keyField.getText().trim();

            if (filePath.isEmpty() || key.isEmpty()) {
                JOptionPane.showMessageDialog(AudioRecorderView.this,
                        "Veuillez remplir tous les champs.",
                        "Champs manquants", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Lire le fichier avec la clé fournie
            try {
                controller.playRecordingWithKey(filePath, key);
                statusLabel.setText("Lecture en cours...");
                updateButtonStates();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(AudioRecorderView.this,
                        "Erreur lors de la lecture: " + ex.getMessage(),
                        "Erreur de lecture", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Télécharge l'enregistrement sélectionné.
     */
    private void downloadRecording() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un enregistrement.",
                    "Aucun enregistrement sélectionné", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int recordingId = (int) tableModel.getValueAt(selectedRow, 0);
            String fileName = (String) tableModel.getValueAt(selectedRow, 1);

            // Proposer à l'utilisateur de choisir l'emplacement de destination
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Enregistrer l'enregistrement");

            // Suggérer un nom de fichier basé sur le nom de l'enregistrement
            String suggestedName = fileName.replace("[Partagé par ", "").replace("]", "").trim();
            suggestedName = suggestedName.replaceAll("[\\\\/:*?\"<>|]", "_"); // Remplacer les caractères invalides
            suggestedName += ".enc";

            fileChooser.setSelectedFile(new File(suggestedName));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers audio chiffrés (*.enc)", "enc"));

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String destinationPath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!destinationPath.endsWith(".enc")) {
                    destinationPath += ".enc";
                }

                // Afficher un curseur d'attente
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Télécharger l'enregistrement
                boolean success = controller.exportRecording(recordingId, destinationPath);

                // Restaurer le curseur
                setCursor(Cursor.getDefaultCursor());

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Enregistrement téléchargé avec succès vers:\n" + destinationPath,
                            "Téléchargement réussi", JOptionPane.INFORMATION_MESSAGE);

                    // Demander si l'utilisateur veut ouvrir le dossier contenant le fichier
                    int openFolder = JOptionPane.showConfirmDialog(this,
                            "Voulez-vous ouvrir le dossier contenant le fichier?",
                            "Ouvrir le dossier", JOptionPane.YES_NO_OPTION);

                    if (openFolder == JOptionPane.YES_OPTION) {
                        try {
                            File folder = new File(destinationPath).getParentFile();
                            Desktop.getDesktop().open(folder);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(this,
                                    "Impossible d'ouvrir le dossier: " + ex.getMessage(),
                                    "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Erreur lors du téléchargement de l'enregistrement.",
                            "Erreur de téléchargement", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du téléchargement: " + ex.getMessage(),
                    "Erreur de téléchargement", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Déconnecte l'utilisateur et retourne à l'écran de connexion.
     */
    private void logout() {
        // Arrêter toute opération en cours
        if (controller.isRecording()) {
            try {
                controller.stopRecording();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (controller.isPlaying()) {
            try {
                controller.stopPlaying();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Afficher une confirmation
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Êtes-vous sûr de vouloir vous déconnecter?",
                "Confirmation de déconnexion",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Fermer cette fenêtre
            setVisible(false);

            // Nettoyer les ressources
            // Ne pas appeler dispose() pour permettre la réutilisation de la vue

            // Ouvrir l'écran de connexion
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        }
    }

    /**
     * Affiche la fenêtre des notifications.
     */
    private void showNotifications() {
        // Créer une nouvelle fenêtre pour les notifications
        JDialog notificationDialog = new JDialog(this, "Notifications", false);
        notificationDialog.setSize(500, 400);
        notificationDialog.setLocationRelativeTo(this);

        // Créer et ajouter le panneau de notifications
        NotificationPanel notificationPanel = new NotificationPanel(currentUserId, notificationService, controller);
        notificationDialog.add(notificationPanel);

        // Afficher la fenêtre
        notificationDialog.setVisible(true);
    }

    private void updateTimerLabel() {
        int minutes = recordingSeconds / 60;
        int seconds = recordingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }
}