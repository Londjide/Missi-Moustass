package view;

import controller.AuthController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Vue pour l'écran d'inscription.
 */
public class RegisterView extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    private final AuthController authController;
    
    /**
     * Constructeur qui initialise l'interface d'inscription.
     */
    public RegisterView() {
        this.authController = new AuthController();
        initializeUI();
    }
    
    /**
     * Initialise l'interface utilisateur.
     */
    private void initializeUI() {
        setTitle("Inscription - Missié Moustass");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 400, 300);
        
        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPane.setLayout(new BorderLayout(10, 10));
        setContentPane(contentPane);
        
        // Titre
        JLabel titleLabel = new JLabel("Inscription");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(titleLabel, BorderLayout.NORTH);
        
        // Panneau principal
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        
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
        
        // Confirmation du mot de passe
        JPanel confirmPanel = new JPanel(new BorderLayout(5, 0));
        JLabel confirmLabel = new JLabel("Confirmer:");
        confirmPasswordField = new JPasswordField();
        confirmPanel.add(confirmLabel, BorderLayout.WEST);
        confirmPanel.add(confirmPasswordField, BorderLayout.CENTER);
        formPanel.add(confirmPanel);
        
        // Message d'aide pour le mot de passe
        JLabel helpLabel = new JLabel("<html>Le mot de passe doit contenir au moins :<br>" +
                "- 12 caractères<br>" +
                "- Une majuscule<br>" +
                "- Une minuscule<br>" +
                "- Un chiffre<br>" +
                "- Un caractère spécial (!@#$%^&*()-_=+<>?)</html>");
        helpLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        formPanel.add(helpLabel);
        
        // Boutons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        registerButton = new JButton("S'inscrire");
        cancelButton = new JButton("Annuler");
        
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRegister();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        formPanel.add(buttonPanel);
        
        // Message d'état
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(statusLabel);
        
        contentPane.add(formPanel, BorderLayout.CENTER);
        
        // Centrer la fenêtre
        setLocationRelativeTo(null);
    }
    
    /**
     * Gère la tentative d'inscription.
     */
    private void handleRegister() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Les mots de passe ne correspondent pas");
            passwordField.setText("");
            confirmPasswordField.setText("");
            return;
        }
        
        if (!authController.isValidEmail(email)) {
            statusLabel.setText("Format d'email invalide");
            return;
        }
        
        if (!authController.isStrongPassword(password)) {
            statusLabel.setText("Le mot de passe ne respecte pas les critères de sécurité");
            return;
        }
        
        try {
            int userId = authController.register(email, password, false);
            
            if (userId > 0) {
                // Inscription réussie
                JOptionPane.showMessageDialog(this,
                    "Inscription réussie ! Vous pouvez maintenant vous connecter.",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                statusLabel.setText("Échec de l'inscription");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("existe déjà")) {
                statusLabel.setText("Un compte existe déjà avec cet email");
            } else {
                e.printStackTrace();
                statusLabel.setText("Erreur lors de l'inscription: " + e.getMessage());
            }
        }
    }
} 