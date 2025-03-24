package Auth;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.ActionEvent;

public class Reset2 {

	private JFrame frame;
	private JPasswordField tempPasswordField;
	private JPasswordField newPasswordField;
	private JPasswordField confirmPasswordField;
	private static final String DB_URL = "jdbc:sqlite:users.db"; 

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Reset2 window = new Reset2();
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
	public Reset2() {
		initialize();
	}
	
	/**
     * Établit une connexion à la base de données SQLite.
     * @return Connection Connexion à la base de données.
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
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 508, 337);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel_1 = new JLabel("Reinitialiser votre mot de passe");
		lblNewLabel_1.setBounds(148, 23, 202, 16);
		frame.getContentPane().add(lblNewLabel_1);
		
		JLabel lblNewLabel = new JLabel("Mot de passe temporaire");
		lblNewLabel.setBounds(41, 98, 165, 16);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblNouveauMotDe = new JLabel("Nouveau mot de passe");
		lblNouveauMotDe.setBounds(41, 149, 171, 16);
		frame.getContentPane().add(lblNouveauMotDe);
		
		JLabel lblConfirmerLeMot = new JLabel("Confirmer le mot de passe");
		lblConfirmerLeMot.setBounds(35, 208, 171, 16);
		frame.getContentPane().add(lblConfirmerLeMot);
		
		JButton btnNewButton = new JButton("Valider");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePassword();
				
			}
		});
		btnNewButton.setBounds(385, 257, 117, 29);
		frame.getContentPane().add(btnNewButton);
		
		tempPasswordField = new JPasswordField();
		tempPasswordField.setBounds(273, 93, 131, 26);
		frame.getContentPane().add(tempPasswordField);
		
		newPasswordField = new JPasswordField();
		newPasswordField.setBounds(273, 144, 131, 26);
		frame.getContentPane().add(newPasswordField);
		
		confirmPasswordField = new JPasswordField();
		confirmPasswordField.setBounds(273, 203, 131, 26);
		frame.getContentPane().add(confirmPasswordField);
	}
	
	/**
     * Change le mot de passe utilisateur après vérification.
     */
	
	private void changePassword() {
        String tempPassword = new String(tempPasswordField.getPassword()).trim();
        String newPassword = new String(newPasswordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

     // Vérification que tous les champs sont remplis
        if (tempPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

     // Vérification que les nouveaux mots de passe correspondent
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(frame, "Les nouveaux mots de passe ne correspondent pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement checkStmt = conn.prepareStatement("SELECT email FROM users WHERE password = ?");
             PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET password = ? WHERE password = ?")) {

            checkStmt.setString(1, hashPassword(tempPassword));
            ResultSet rs = checkStmt.executeQuery();
            
         // Vérification que le mot de passe temporaire est correct
            if (!rs.next()) {
                JOptionPane.showMessageDialog(frame, "Mot de passe temporaire incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

         // Mise à jour du mot de passe dans la base de données
            updateStmt.setString(1, hashPassword(newPassword));
            updateStmt.setString(2, hashPassword(tempPassword));
            updateStmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Mot de passe changé avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
            
            frame.dispose(); // Close window after successful reset
            Connexion connexion = new Connexion();
			connexion.afficher();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Erreur lors de la mise à jour du mot de passe.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
	
	/**
     * Hache un mot de passe avec SHA-256.
     * @param password Le mot de passe à hacher.
     * @return String Le mot de passe haché.
     */
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
