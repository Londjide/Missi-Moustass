package view;

import controller.AdminController;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Vue pour l'interface d'administration.
 * Cette classe suit le principe de responsabilité unique (SRP) en gérant uniquement
 * l'interface utilisateur pour l'administration.
 */
public class AdminView extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private JPanel contentPane;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAddUser;
    private JButton btnDeleteUser;
    private JButton btnResetPassword;
    private JLabel statusLabel;
    private JLabel userLabel;
    
    private final AdminController controller;
    private int selectedUserId = -1;
    
    /**
     * Constructeur qui initialise l'interface d'administration.
     * 
     * @param adminId L'ID de l'administrateur connecté
     * @throws Exception Si l'utilisateur n'est pas un administrateur
     */
    public AdminView(int adminId) throws Exception {
        this.controller = new AdminController(adminId);
        initializeUI();
        loadUsers();
    }
    
    /**
     * Initialise l'interface utilisateur.
     */
    private void initializeUI() {
        setTitle("Administration - Missié Moustass");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 650, 500);
        
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(10, 10));
        setContentPane(contentPane);
        
        // Panel pour le titre et l'utilisateur
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        // Étiquette de titre
        JLabel titleLabel = new JLabel("Gestion des Utilisateurs");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Étiquette utilisateur
        userLabel = new JLabel("Administrateur connecté: ");
        headerPanel.add(userLabel, BorderLayout.SOUTH);
        
        // Panel de boutons en haut
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Bouton pour accéder à l'enregistreur audio
        JButton recorderButton = new JButton("Enregistreur Audio");
        recorderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRecorderView();
            }
        });
        
        // Bouton de déconnexion
        JButton logoutButton = new JButton("Déconnexion");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        
        topButtonPanel.add(recorderButton);
        topButtonPanel.add(logoutButton);
        
        headerPanel.add(topButtonPanel, BorderLayout.EAST);
        contentPane.add(headerPanel, BorderLayout.NORTH);
        
        // Tableau des utilisateurs
        String[] columnNames = {"ID", "Email", "Admin"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Rendre toutes les cellules non éditables
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) { // Colonne "Admin"
                    return Boolean.class; // Afficher les checkboxes
                }
                return String.class;
            }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        
        // Gérer la sélection de ligne
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    selectedUserId = (int) tableModel.getValueAt(row, 0);
                    updateButtonStates();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        
        // Panel pour les boutons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        
        btnAddUser = new JButton("Ajouter");
        btnDeleteUser = new JButton("Supprimer");
        btnResetPassword = new JButton("Réinitialiser MdP");
        
        btnAddUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUser();
            }
        });
        
        btnDeleteUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUser();
            }
        });
        
        btnResetPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetPassword();
            }
        });
        
        buttonPanel.add(btnAddUser);
        buttonPanel.add(btnDeleteUser);
        buttonPanel.add(btnResetPassword);
        
        // Label d'état
        statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Panel pour les contrôles
        JPanel controlsPanel = new JPanel(new BorderLayout(0, 10));
        controlsPanel.add(buttonPanel, BorderLayout.CENTER);
        controlsPanel.add(statusLabel, BorderLayout.SOUTH);
        
        contentPane.add(controlsPanel, BorderLayout.SOUTH);
        
        // Initialiser l'état des boutons
        updateButtonStates();
        
        // Centrer la fenêtre
        setLocationRelativeTo(null);
    }
    
    /**
     * Met à jour l'état des boutons en fonction de l'état de l'application.
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedUserId > 0;
        
        btnAddUser.setEnabled(true);
        btnDeleteUser.setEnabled(hasSelection);
        btnResetPassword.setEnabled(hasSelection);
    }
    
    /**
     * Charge la liste des utilisateurs.
     */
    private void loadUsers() {
        try {
            // Effacer le tableau
            tableModel.setRowCount(0);
            
            // Récupérer les utilisateurs
            List<User> users = controller.getAllUsers();
            
            // Ajouter les utilisateurs au tableau
            for (User user : users) {
                Object[] rowData = {
                    user.getId(),
                    user.getEmail(),
                    user.isAdmin()
                };
                
                tableModel.addRow(rowData);
            }
            
            // Réinitialiser la sélection
            selectedUserId = -1;
            updateButtonStates();
            
            statusLabel.setText("Utilisateurs chargés avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des utilisateurs: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("Erreur lors du chargement des utilisateurs");
        }
    }
    
    /**
     * Ajoute un nouvel utilisateur.
     */
    private void addUser() {
        // Créer un panel pour le formulaire
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();
        
        JLabel passwordLabel = new JLabel("Mot de passe:");
        JPasswordField passwordField = new JPasswordField();
        
        JLabel adminLabel = new JLabel("Administrateur:");
        JCheckBox adminCheckBox = new JCheckBox();
        
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(adminLabel);
        panel.add(adminCheckBox);
        
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Ajouter un utilisateur", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            boolean isAdmin = adminCheckBox.isSelected();
            
            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Veuillez remplir tous les champs",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                int userId = controller.createUser(email, password, isAdmin);
                
                if (userId > 0) {
                    statusLabel.setText("Utilisateur créé avec succès");
                    loadUsers(); // Recharger la liste
                } else {
                    statusLabel.setText("Échec de la création de l'utilisateur");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la création de l'utilisateur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Erreur lors de la création de l'utilisateur");
            }
        }
    }
    
    /**
     * Supprime l'utilisateur sélectionné.
     */
    private void deleteUser() {
        if (selectedUserId <= 0) {
            return;
        }
        
        int confirmResult = JOptionPane.showConfirmDialog(this,
            "Êtes-vous sûr de vouloir supprimer cet utilisateur ?",
            "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
        
        if (confirmResult == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = controller.deleteUser(selectedUserId);
                
                if (deleted) {
                    statusLabel.setText("Utilisateur supprimé avec succès");
                    loadUsers(); // Recharger la liste
                } else {
                    statusLabel.setText("Échec de la suppression de l'utilisateur");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression de l'utilisateur: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Erreur lors de la suppression de l'utilisateur");
            }
        }
    }
    
    /**
     * Réinitialise le mot de passe de l'utilisateur sélectionné.
     */
    private void resetPassword() {
        if (selectedUserId <= 0) {
            return;
        }
        
        // Demander le nouveau mot de passe
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        JLabel passwordLabel = new JLabel("Nouveau mot de passe:");
        JPasswordField passwordField = new JPasswordField();
        
        JLabel confirmLabel = new JLabel("Confirmer le mot de passe:");
        JPasswordField confirmField = new JPasswordField();
        
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(confirmLabel);
        panel.add(confirmField);
        
        int result = JOptionPane.showConfirmDialog(this, panel,
                "Réinitialiser le mot de passe", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Le mot de passe ne peut pas être vide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this,
                    "Les mots de passe ne correspondent pas",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                boolean reset = controller.resetUserPassword(selectedUserId, password);
                
                if (reset) {
                    statusLabel.setText("Mot de passe réinitialisé avec succès");
                } else {
                    statusLabel.setText("Échec de la réinitialisation du mot de passe");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la réinitialisation du mot de passe: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Erreur lors de la réinitialisation du mot de passe");
            }
        }
    }
    
    /**
     * Ouvre la vue de l'enregistreur audio.
     */
    private void openRecorderView() {
        try {
            // Récupérer l'ID de l'administrateur connecté
            AudioRecorderView recorderView = new AudioRecorderView(controller.getAdminId());
            recorderView.setVisible(true);
            dispose(); // Fermer cette fenêtre
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Erreur lors de l'ouverture de l'enregistreur audio: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Déconnecte l'administrateur et retourne à l'écran de connexion.
     */
    private void logout() {
        dispose(); // Fermer cette fenêtre
        LoginView loginView = new LoginView();
        loginView.setVisible(true);
    }
} 