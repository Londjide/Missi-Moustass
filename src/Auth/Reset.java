package Auth;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.awt.event.ActionEvent;

public class Reset  {

	private JFrame frame;
	private JTextField textField;

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Reset window = new Reset();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
     * URL de connexion à la base de données SQLite.
     */
	
	private static final String DB_URL = "jdbc:sqlite:users.db";
	
	
	/**
     * Établit une connexion à la base de données SQLite.
     * @return Un objet Connection si la connexion est réussie, sinon null.
     */
	
	public static Connection connect( ) {
		 
		 Connection conn = null;
		 
		 try {
			 conn = DriverManager.getConnection(DB_URL);
			 System.out.println ("Connexion a SQLite etablie.");
		 }
		 catch (SQLException e) {
			 System.out.println(e.getMessage());
			 
			 
		 }
		 return conn;
	 }
	

    /**
     * Constructeur de la classe Reset, qui initialise l'interface utilisateur.
     */
	
	public Reset() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 554, 359);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Reinitialiser votre mot de passe");
		lblNewLabel.setBounds(166, 19, 202, 16);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Veuillez entrer votre email");
		lblNewLabel_1.setBounds(28, 126, 171, 16);
		frame.getContentPane().add(lblNewLabel_1);
		
		textField = new JTextField();
		textField.setBounds(213, 121, 179, 26);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnNewButton = new JButton("Valider");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                resetPassword();
                
                
            }
        });
		
		btnNewButton.setBounds(412, 247, 117, 29);
		frame.getContentPane().add(btnNewButton);
	}
	
	/**
     * Méthode pour réinitialiser le mot de passe de l'utilisateur.
     * Vérifie si l'email est valide, génère un nouveau mot de passe et le met à jour dans la base de données.
     */
	
	private void resetPassword() {
        String email = textField.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Veuillez entrer votre email.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tempPassword = generateTempPassword();
        String hashedTempPassword = hashPassword(tempPassword);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET password = ? WHERE email = ?")) {

        	pstmt.setString(1, hashedTempPassword);
            pstmt.setString(2, email);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(frame, "Votre mot de passe temporaire est: " + tempPassword, "Succès", JOptionPane.INFORMATION_MESSAGE);
                
                Reset2 reset2 = new Reset2();
				frame.dispose(); // Fermer la fenêtre de reinitialisation
				reset2.afficher(); // Utiliser la nouvelle méthode
			
            } else {
                JOptionPane.showMessageDialog(frame, "Email non trouvé.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Erreur lors de la mise à jour du mot de passe.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder tempPassword = new StringBuilder(8);

        for (int i = 0; i < 8; i++) {
            tempPassword.append(chars.charAt(random.nextInt(chars.length())));
        }

        return tempPassword.toString();
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur de hachage du mot de passe", e);
        }
}
    
    public void afficher() {

		frame.setVisible(true);
}
}
