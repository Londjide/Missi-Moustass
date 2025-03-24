package Auth;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.JPasswordField;

/**
 * Classe Admin permettant de gérer les utilisateurs via une interface graphique.
 */

public class Admin<rs> {

	private JFrame frame;
	private JTable table;
	private JTextField textField_2;
	private JPasswordField passwordField;
	private Integer selectedUserId = null; // To store the currently selected user ID

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Admin window = new Admin();
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
	public Admin() {
		initialize();
		createTable();
		populateTable();
	}
	
	
	private static final String DB_URL = "jdbc:sqlite:users.db";
	 
	 
	/**
     * Établit une connexion à la base de données SQLite.
     * @return Connection objet représentant la connexion à la base de données.
     */
	public static Connection connect() {
		Connection conn = null;
		 
		try {
			conn = DriverManager.getConnection(DB_URL);
			System.out.println("Connexion a SQLite etablie.");
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return conn;
	}
	 
	 
	/**
	 * Initialize the contents of the frame.
	 */
	private static void createTable() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
			Statement stmt = conn.createStatement()) {
			String sql = "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, email Varchar NOT NULL, password Varchar NOT NULL )";
			stmt.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
     * Vérifie si le mot de passe respecte les critères de sécurité.
     * @param password Le mot de passe à valider.
     * @return true si le mot de passe est valide, sinon false.
     */
	static boolean isValidPassword(String password) {
		String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+<>?]).{12,}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(password);
		return matcher.matches();
	}
	    
	/**
     * Vérifie si l'email est valide.
     * @param email L'email à valider.
     * @return true si l'email est valide, sinon false.
     */
	static boolean isValidEmail(String email) {
		String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	/**
     * Initialise l'interface graphique.
     */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 754, 502);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Administration");
		lblNewLabel.setBounds(340, 6, 110, 16);
		frame.getContentPane().add(lblNewLabel);
		
		JPanel panel = new JPanel();
		panel.setBounds(146, 198, 445, 270);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		
		table = new JTable();
		table.setCellSelectionEnabled(true);
		table.setColumnSelectionAllowed(true);
		table.setBounds(6, 6, 433, 258);
		panel.add(table);
		
		// Add a MouseListener to the table
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int selectedRow = table.getSelectedRow();
				if (selectedRow != -1) {
					// Get data from the selected row
					selectedUserId = (Integer) table.getValueAt(selectedRow, 0);
					String email = (String) table.getValueAt(selectedRow, 1);
					
					// Set the values to the fields
					textField_2.setText(email);
					
					// We can't retrieve the original password as it's hashed,
					// so we clear the password field to avoid confusion
					passwordField.setText("");
					
					// Let the user know they need to enter a new password for updates
					if (e.getClickCount() == 2) { // Double-click
						JOptionPane.showMessageDialog(frame, 
							"Selected User ID: " + selectedUserId + "\nNote: You'll need to enter a new password to update this user.",
							"User Selected", 
							JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		});

		JButton btnNewButton = new JButton("New");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Capture the email and password from the text fields
				String email = textField_2.getText();
				String password = passwordField.getText();

				if (email.isEmpty() || password.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Email and Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (!isValidEmail(email)) {
					JOptionPane.showMessageDialog(frame, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (!isValidPassword(password)) {
					JOptionPane.showMessageDialog(frame, "Password must be at least 12 characters and include uppercase, lowercase, number, and special character.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// Check if the email already exists in the database
				try (Connection conn = DriverManager.getConnection(DB_URL);
					PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?")) {

					pstmt.setString(1, email);
					try (ResultSet rs = pstmt.executeQuery()) {
						if (rs.next() && rs.getInt(1) > 0) {
							// Email already exists
							JOptionPane.showMessageDialog(frame, "Email is already registered!", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						// If email doesn't exist, hash the password
						String hashedPassword = hashPassword(password);

						// Insert the new user into the database
						try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO users (email, password) VALUES (?, ?)")) {
							insertStmt.setString(1, email);
							insertStmt.setString(2, hashedPassword);

							// Execute the insert statement
							int rowsAffected = insertStmt.executeUpdate();

							if (rowsAffected > 0) {
								// Show a success message
								JOptionPane.showMessageDialog(frame, "New user added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

								// Refresh the table to reflect the new entry
								populateTable();
								// Clear the fields
								textField_2.setText("");
								passwordField.setText("");
								// Reset selectedUserId
								selectedUserId = null;
							} else {
								// If insertion fails, show an error message
								JOptionPane.showMessageDialog(frame, "Failed to add new user. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				} catch (SQLException ex) {
					ex.printStackTrace();
					// Show an error message if there is an exception
					JOptionPane.showMessageDialog(frame, "An error occurred while adding the user.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
			
		btnNewButton.setBounds(6, 157, 81, 29);
		frame.getContentPane().add(btnNewButton);
		
		JButton btnUpdate = new JButton("Update");
		btnUpdate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Check if a user is selected
				if (selectedUserId == null) {
					JOptionPane.showMessageDialog(frame, "Please select a user from the table first.", "No User Selected", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				// Retrieve the new email and password from the text fields
				String newEmail = textField_2.getText();
				String newPassword = passwordField.getText();

				// Check if email or password is empty
				if (newEmail.isEmpty() || newPassword.isEmpty()) {
					JOptionPane.showMessageDialog(frame, "Email and Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
					return;  // Exit the method if fields are empty
				}
				if (!isValidEmail(newEmail)) {
					JOptionPane.showMessageDialog(frame, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (!isValidPassword(newPassword)) {
					JOptionPane.showMessageDialog(frame, "Password must be at least 12 characters and include uppercase, lowercase, number, and special character.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String hashedPassword = hashPassword(newPassword);

				// Update the user details in the database
				try (Connection conn = DriverManager.getConnection(DB_URL);
					PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET email = ?, password = ? WHERE id = ?")) {

					// Set the parameters for the update query
					pstmt.setString(1, newEmail);     // Set new email
					pstmt.setString(2, hashedPassword);  // Set new password
					pstmt.setInt(3, selectedUserId);      // Set the selected user ID

					// Execute the update query
					int rowsAffected = pstmt.executeUpdate();

					if (rowsAffected > 0) {
						// Success: User details updated successfully
						JOptionPane.showMessageDialog(frame, "User details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
						populateTable();  // Refresh the table to reflect the updated data
						// Clear fields after update
						textField_2.setText("");
						passwordField.setText("");
						selectedUserId = null; // Reset selection
					} else {
						// Failure: No user found with the selected ID
						JOptionPane.showMessageDialog(frame, "No user found with the selected ID.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch (SQLException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(frame, "An error occurred while updating the user.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		btnUpdate.setBounds(111, 157, 93, 29);
		frame.getContentPane().add(btnUpdate);
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Check if a user is selected
				if (selectedUserId == null) {
					JOptionPane.showMessageDialog(frame, "Please select a user from the table first.", "No User Selected", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				// Confirm the deletion with the user
				int confirm = JOptionPane.showConfirmDialog(frame,
						"Are you sure you want to delete user ID " + selectedUserId + "?",
						"Confirm Deletion",
						JOptionPane.YES_NO_OPTION);

				if (confirm == JOptionPane.YES_OPTION) {
					// Delete the user from the database
					try (Connection conn = DriverManager.getConnection(DB_URL);
						PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {

						// Set the user ID to be deleted
						pstmt.setInt(1, selectedUserId);

						// Execute the delete query
						int rowsAffected = pstmt.executeUpdate();

						if (rowsAffected > 0) {
							JOptionPane.showMessageDialog(frame, "User with ID " + selectedUserId + " deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
							populateTable();  // Refresh the table to reflect the updated data
							// Clear fields after deletion
							textField_2.setText("");
							passwordField.setText("");
							selectedUserId = null; // Reset selection
						} else {
							JOptionPane.showMessageDialog(frame, "No user found with the selected ID.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					} catch (SQLException ex) {
						ex.printStackTrace();
						JOptionPane.showMessageDialog(frame, "An error occurred while deleting the user.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		btnDelete.setBounds(225, 157, 81, 29);
		frame.getContentPane().add(btnDelete);
		
		// Add a status label to show currently selected user
		JLabel lblSelectedUser = new JLabel("No user selected");
		lblSelectedUser.setBounds(519, 52, 200, 16);
		frame.getContentPane().add(lblSelectedUser);
		
		JLabel lblNewLabel_1_2 = new JLabel("Email");
		lblNewLabel_1_2.setBounds(39, 52, 61, 16);
		frame.getContentPane().add(lblNewLabel_1_2);
		
		textField_2 = new JTextField();
		textField_2.setColumns(10);
		textField_2.setBounds(95, 47, 143, 26);
		frame.getContentPane().add(textField_2);
		
		JLabel lblNewLabel_1_2_1 = new JLabel("Password");
		lblNewLabel_1_2_1.setBounds(22, 108, 61, 16);
		frame.getContentPane().add(lblNewLabel_1_2_1);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(95, 103, 143, 26);
		frame.getContentPane().add(passwordField);
	}
	
	// Method to populate JTable with data from SQLite database
    private void populateTable() {
        DefaultTableModel model = new DefaultTableModel();
        table.setModel(model);
	
		// Set the column names
		model.addColumn("ID");
		model.addColumn("Email");
		model.addColumn("Password");
		

		try (Connection conn = DriverManager.getConnection(DB_URL);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

			while (rs.next()) {
				// Add rows to the table
				model.addRow(new Object[]{
					rs.getInt("id"),
					rs.getString("email"),
					rs.getString("password"),
					
				});
			}
			

		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
   
    
    /**
     * Hache un mot de passe avec SHA-256.
     * @param password Le mot de passe à hacher.
     * @return Le mot de passe haché en hexadécimal.
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
    
   
	



	

