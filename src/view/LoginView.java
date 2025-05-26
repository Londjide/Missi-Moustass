package view;

import controller.AuthController;
import model.User;
import util.RecentUsersManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Vue pour l'écran de connexion.
 * Cette classe suit le principe de responsabilité unique (SRP) en gérant uniquement
 * l'interface utilisateur pour la connexion.
 */
public class LoginView extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel;
    private JComboBox<UserItem> recentUsersComboBox;
    
    private final AuthController authController;
    
    private static AudioRecorderView recorderView;
    
    /**
     * Constructeur qui initialise l'interface de connexion.
     */
    public LoginView() {
        this.authController = new AuthController();
        initializeUI();
        loadRecentUsers();
    }
    
    /**
     * Initialise l'interface utilisateur.
     */
    private void initializeUI() {
        setTitle("Connexion - Missié Moustass");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 400, 300);
        
        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPane.setLayout(new BorderLayout(10, 10));
        setContentPane(contentPane);
        
        // Titre
        JLabel titleLabel = new JLabel("Messagerie Vocale Sécurisée");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(titleLabel, BorderLayout.NORTH);
        
        // Panneau principal
        JPanel formPanel = new JPanel(new GridLayout(7, 1, 10, 10));
        
        // Utilisateurs récents
        JPanel recentUsersPanel = new JPanel(new BorderLayout(5, 0));
        JLabel recentUsersLabel = new JLabel("Connexion rapide:");
        recentUsersComboBox = new JComboBox<>();
        recentUsersComboBox.setRenderer(new UserItemRenderer());
        recentUsersComboBox.addActionListener(e -> {
            if (recentUsersComboBox.getSelectedIndex() > 0) { // Skip "Sélectionner un utilisateur..."
                UserItem selectedItem = (UserItem) recentUsersComboBox.getSelectedItem();
                if (selectedItem != null) {
                    emailField.setText(selectedItem.getUser().getEmail());
                    // Mettre le focus sur le champ du mot de passe
                    passwordField.requestFocusInWindow();
                }
            }
        });
        recentUsersPanel.add(recentUsersLabel, BorderLayout.WEST);
        recentUsersPanel.add(recentUsersComboBox, BorderLayout.CENTER);
        formPanel.add(recentUsersPanel);
        
        // Séparateur
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        formPanel.add(separator);
        
        // Email
        JPanel emailPanel = new JPanel(new BorderLayout(5, 0));
        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();
        emailPanel.add(emailLabel, BorderLayout.WEST);
        emailPanel.add(emailField, BorderLayout.CENTER);
        formPanel.add(emailPanel);
        
        // Mot de passe
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordField = new JPasswordField();
        passwordPanel.add(passwordLabel, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        formPanel.add(passwordPanel);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        loginButton = new JButton("Connexion");
        registerButton = new JButton("Inscription");
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegisterView();
            }
        });
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        formPanel.add(buttonPanel);
        
        // Message d'état
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(statusLabel);
        
        contentPane.add(formPanel, BorderLayout.CENTER);
        
        // Droits d'auteur
        JLabel copyrightLabel = new JLabel("© 2023 Équipe Missié Moustass");
        copyrightLabel.setHorizontalAlignment(SwingConstants.CENTER);
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        contentPane.add(copyrightLabel, BorderLayout.SOUTH);
        
        // Centrer la fenêtre
        setLocationRelativeTo(null);
    }
    
    /**
     * Charge la liste des utilisateurs récents.
     */
    private void loadRecentUsers() {
        recentUsersComboBox.removeAllItems();
        recentUsersComboBox.addItem(new UserItem(null, "Sélectionner un utilisateur..."));
        
        List<User> recentUsers = RecentUsersManager.getInstance().getRecentUsers();
        for (User user : recentUsers) {
            recentUsersComboBox.addItem(new UserItem(user, user.getEmail()));
        }
    }
    
    /**
     * Gère la tentative de connexion.
     */
    private void handleLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs");
            return;
        }
        
        try {
            int userId = authController.login(email, password);
            
            if (userId > 0) {
                // Connexion réussie
                // Ajouter l'utilisateur à la liste des utilisateurs récents
                User user = authController.getUserById(userId);
                if (user != null) {
                    RecentUsersManager.getInstance().addRecentUser(user);
                }
                
                openMainView(userId);
            } else {
                // Échec de l'authentification
                statusLabel.setText("Email ou mot de passe incorrect");
                passwordField.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Erreur de connexion: " + e.getMessage());
        }
    }
    
    /**
     * Ouvre la vue principale après une connexion réussie.
     * 
     * @param userId L'ID de l'utilisateur connecté
     */
    private void openMainView(int userId) {
        try {
            boolean isAdmin = authController.isAdmin(userId);
            
            if (isAdmin) {
                // Ouvrir la vue d'administration
                AdminView adminView = new AdminView(userId);
                adminView.setVisible(true);
            } else {
                // Réutiliser ou créer la vue d'enregistrement audio
                if (recorderView == null) {
                    recorderView = new AudioRecorderView(userId);
                } else {
                    recorderView.updateUser(userId);
                }
                recorderView.setVisible(true);
            }
            
            dispose(); // Fermer la fenêtre de connexion
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Erreur lors de l'ouverture de l'application: " + e.getMessage());
        }
    }
    
    /**
     * Ouvre la vue d'inscription.
     */
    private void openRegisterView() {
        RegisterView registerView = new RegisterView();
        registerView.setVisible(true);
    }
    
    /**
     * Point d'entrée pour démarrer l'application.
     * 
     * @param args Arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        // Configuration des propriétés système pour améliorer la compatibilité audio sur macOS
        System.setProperty("javax.sound.sampled.Clip", "com.sun.media.sound.DirectAudioDeviceProvider");
        System.setProperty("javax.sound.sampled.Port", "com.sun.media.sound.PortMixerProvider");
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
            }
        });
    }
    
    /**
     * Classe interne pour représenter un élément utilisateur dans la liste déroulante.
     */
    private static class UserItem {
        private final User user;
        private final String displayText;
        
        public UserItem(User user, String displayText) {
            this.user = user;
            this.displayText = displayText;
        }
        
        public User getUser() {
            return user;
        }
        
        @Override
        public String toString() {
            return displayText;
        }
    }
    
    /**
     * Classe interne pour personnaliser l'affichage des éléments utilisateurs dans la liste déroulante.
     */
    private static class UserItemRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof UserItem) {
                UserItem item = (UserItem) value;
                setText(item.toString());
                
                // Ajouter une icône d'utilisateur (optionnel)
                setIcon(UIManager.getIcon("FileView.computerIcon"));
            }
            
            return this;
        }
    }
} 