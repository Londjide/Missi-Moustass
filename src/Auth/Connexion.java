package Auth;

import java.awt.EventQueue;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;

@SuppressWarnings("unused")
public class Connexion extends JFrame {

    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox checkAdmin;
    private JCheckBox checkUser;
    
    /**
     * Vérifie la validité d'une adresse email.
     *
     * @param email L'adresse email à vérifier.
     * @return true si l'email est valide, false sinon.
     */
    static boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    
    /**
     * Vérifie la validité d'un mot de passe.
     *
     * @param password Le mot de passe à vérifier.
     * @return true si le mot de passe est valide, false sinon.
     */
    static boolean isValidPassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+<>?]).{12,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
    
    /**
     * Hache un mot de passe en SHA-256.
     *
     * @param password Le mot de passe à hacher.
     * @return Le mot de passe haché sous forme de chaîne hexadécimale.
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Ensure database tables exist
                    initializeDatabase();
                    
                    Connexion window = new Connexion();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public Connexion() {
        initialize();
    }
    
    /**
     * Initialize the database if it doesn't exist
     */
    private static void initializeDatabase() {
        Connection conn = null;
        try {
            conn = connect();
            
            // Create users table if it doesn't exist
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "email TEXT NOT NULL UNIQUE, " +
                         "password TEXT NOT NULL, " +
                         "is_admin BOOLEAN NOT NULL DEFAULT 0)";
            stmt.executeUpdate(sql);
            
            // Create recordings table if it doesn't exist
            sql = "CREATE TABLE IF NOT EXISTS recordings (" +
                  "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                  "name TEXT NOT NULL, " +
                  "timestamp TEXT NOT NULL, " +
                  "duration INTEGER, " +
                  "audio BLOB NOT NULL, " +
                  "encryption_key TEXT NOT NULL, " +
                  "audio_hash TEXT NOT NULL, " +
                  "user_id INTEGER NOT NULL, " +
                  "FOREIGN KEY (user_id) REFERENCES users(id))";
            stmt.executeUpdate(sql);
            
            stmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Établit une connexion à la base de données SQLite.
     * 
     * @return Connection Connexion à la base de données.
     */
    private static final String DB_URL = "jdbc:sqlite:users.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Connexion a SQLite etablie.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 603, 405);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        
        usernameField = new JTextField();
        usernameField.setBounds(317, 73, 130, 26);
        frame.getContentPane().add(usernameField);
        usernameField.setColumns(10);
        
        JButton btnNewButton = new JButton("Valider");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /**
                 * Handles the login button action.
                 * 
                 * @param e The action event.
                 */
                String email = usernameField.getText();
                String password = new String(passwordField.getPassword());
                
                if (email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Email and Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate email format
                if (!isValidEmail(email)) {
                    JOptionPane.showMessageDialog(frame, "Invalid email format.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate password strength
                if (!isValidPassword(password)) {
                    JOptionPane.showMessageDialog(frame, "Password must be at least 12 characters long, contain a mix of uppercase, lowercase, digits, and special characters.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Check if either admin or user is selected
                if (!checkAdmin.isSelected() && !checkUser.isSelected()) {
                    JOptionPane.showMessageDialog(frame, "Please select either Admin or User.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Ensure only one option is selected
                if (checkAdmin.isSelected() && checkUser.isSelected()) {
                    JOptionPane.showMessageDialog(frame, "Please select either Admin or User, not both.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String hashedPassword = hashPassword(password);
                
                try (Connection conn = connect();
                     PreparedStatement pstmt = conn.prepareStatement(
                         "SELECT * FROM users WHERE email = ? AND password = ?")) {
                    
                    pstmt.setString(1, email);
                    pstmt.setString(2, hashedPassword);
                    
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        int userId = rs.getInt("id");
                        boolean isAdmin = rs.getBoolean("is_admin");
                        
                        // Verify user permissions match selected role
                        if (checkAdmin.isSelected() && !isAdmin) {
                            JOptionPane.showMessageDialog(frame, "You do not have admin privileges.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        
                        if (checkUser.isSelected() && isAdmin) {
                            // Allow admin to login as regular user
                            // Or you could restrict this with:
                            // JOptionPane.showMessageDialog(frame, "Please login using admin option.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                            // return;
                        }
                        
                        JOptionPane.showMessageDialog(frame, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Route to appropriate screen based on selection
                        frame.dispose();
                        
                        if (checkAdmin.isSelected()) {
                            Admin admin = new Admin();
                            admin.afficher();
                        } else { // User is selected
                            AudioRecorder audioRecorder = new AudioRecorder(userId);
                            audioRecorder.setVisible(true);
                        }
                        
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid email or password.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "An error occurred while authenticating the user.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnNewButton.setBounds(434, 326, 117, 29);
        frame.getContentPane().add(btnNewButton);
        
        JLabel lblNewLabel = new JLabel("Login");
        lblNewLabel.setBounds(202, 73, 61, 16);
        frame.getContentPane().add(lblNewLabel);
        
        JLabel lblNewLabel_1 = new JLabel("Mot de passe");
        lblNewLabel_1.setBounds(159, 138, 93, 16);
        frame.getContentPane().add(lblNewLabel_1);
        
        JButton btnAnnuler = new JButton("Inscription");
        btnAnnuler.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Inscription inscription = new Inscription();
                frame.dispose(); // Fermer la fenêtre de connexion
                inscription.afficher(); // Utiliser la nouvelle méthode
            }
        });
        btnAnnuler.setBounds(16, 256, 117, 29);
        frame.getContentPane().add(btnAnnuler);
        
        JLabel lblNewLabel_2 = new JLabel("Connectez-vous");
        lblNewLabel_2.setBounds(251, 16, 210, 16);
        frame.getContentPane().add(lblNewLabel_2);
        
        passwordField = new JPasswordField();
        passwordField.setBounds(317, 138, 130, 26);
        frame.getContentPane().add(passwordField);
        
        JLabel lblNewLabel_3 = new JLabel("Vous n'avez pas d'identifiant ? Inscrivez-vous ");
        lblNewLabel_3.setBounds(6, 228, 304, 16);
        frame.getContentPane().add(lblNewLabel_3);
        
        JLabel lblNewLabel_3_1 = new JLabel("Vous avez oubliez votre mot de passe? Reinitialiser le!");
        lblNewLabel_3_1.setBounds(6, 298, 351, 16);
        frame.getContentPane().add(lblNewLabel_3_1);
        
        JButton btnReinitialisation = new JButton("Reinitialisation");
        btnReinitialisation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Reset reset = new Reset();
                frame.dispose(); // Fermer la fenêtre de reinitialisation
                reset.afficher(); // Utiliser la nouvelle méthode
            }
        });
        btnReinitialisation.setBounds(16, 326, 138, 29);
        frame.getContentPane().add(btnReinitialisation);
        
        checkAdmin = new JCheckBox("Admin");
        checkAdmin.setBounds(365, 257, 128, 23);
        frame.getContentPane().add(checkAdmin);
        
        checkUser = new JCheckBox("Enregistrement");
        checkUser.setBounds(453, 257, 128, 23);
        frame.getContentPane().add(checkUser);
        
        JLabel lblNewLabel_3_2 = new JLabel("Connectez sur la fenetre");
        lblNewLabel_3_2.setBounds(400, 228, 185, 16);
        frame.getContentPane().add(lblNewLabel_3_2);
        
        // Add mutual exclusivity to checkboxes
        checkAdmin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (checkAdmin.isSelected()) {
                    checkUser.setSelected(false);
                }
            }
        });
        
        checkUser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (checkUser.isSelected()) {
                    checkAdmin.setSelected(false);
                }
            }
        });
    }
    
    public void afficher() {
        frame.setVisible(true);
    }
}