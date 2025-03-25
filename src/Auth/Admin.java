package Auth;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ButtonGroup;
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
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

/**
 * Classe Admin permettant de gérer les utilisateurs via une interface graphique.
 */

public class Admin {

    private JFrame frame;
    private JTable table;
    private JTextField textField_2;
    private JPasswordField passwordField;
    private Integer selectedUserId = null;
    private JRadioButton rdbtnUser;
    private JRadioButton rdbtnAdmin;
    private ButtonGroup roleButtonGroup;

    private static final String DB_URL = "jdbc:sqlite:users.db";

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

    public Admin() {
        initialize();
        createTable();
        populateTable();
    }

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

    private static void createTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "email TEXT NOT NULL UNIQUE, " +
                         "password TEXT NOT NULL, " +
                         "is_admin BOOLEAN NOT NULL DEFAULT 0)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static boolean isValidPassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+<>?]).{12,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    static boolean isValidEmail(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

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

        // Role radio buttons
        rdbtnUser = new JRadioButton("User");
        rdbtnUser.setBounds(266, 48, 141, 23);
        rdbtnUser.setSelected(true); // Default to User
        frame.getContentPane().add(rdbtnUser);

        rdbtnAdmin = new JRadioButton("Admin");
        rdbtnAdmin.setBounds(266, 83, 141, 23);
        frame.getContentPane().add(rdbtnAdmin);

        // Create a button group to ensure only one can be selected at a time
        roleButtonGroup = new ButtonGroup();
        roleButtonGroup.add(rdbtnUser);
        roleButtonGroup.add(rdbtnAdmin);

        // Table mouse listener
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    // Get data from the selected row
                    selectedUserId = (Integer) table.getValueAt(selectedRow, 0);
                    String email = (String) table.getValueAt(selectedRow, 1);
                    boolean isAdmin = (Boolean) table.getValueAt(selectedRow, 3);

                    // Set the values to the fields
                    textField_2.setText(email);
                    
                    // Set the appropriate radio button for role
                    if (isAdmin) {
                        rdbtnAdmin.setSelected(true);
                    } else {
                        rdbtnUser.setSelected(true);
                    }

                    // Clear password field
                    passwordField.setText("");

                    // Double-click information
                    if (e.getClickCount() == 2) {
                        JOptionPane.showMessageDialog(frame, 
                            "Selected User ID: " + selectedUserId + "\nNote: You'll need to enter a new password to update this user.",
                            "User Selected", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        // New Button
        JButton btnNewButton = new JButton("New");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String email = textField_2.getText();
                String password = passwordField.getText();
                boolean isAdmin = rdbtnAdmin.isSelected();

                // Validation checks
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

                // Check if email already exists
                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE email = ?")) {

                    pstmt.setString(1, email);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(frame, "Email is already registered!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Hash the password
                        String hashedPassword = hashPassword(password);

                        // Insert new user with role
                        try (PreparedStatement insertStmt = conn.prepareStatement(
                                "INSERT INTO users (email, password, is_admin) VALUES (?, ?, ?)")) {
                            insertStmt.setString(1, email);
                            insertStmt.setString(2, hashedPassword);
                            insertStmt.setBoolean(3, isAdmin);

                            int rowsAffected = insertStmt.executeUpdate();

                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(frame, "New user added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                                populateTable();
                                textField_2.setText("");
                                passwordField.setText("");
                                rdbtnUser.setSelected(true); // Reset to default
                                selectedUserId = null;
                            } else {
                                JOptionPane.showMessageDialog(frame, "Failed to add new user. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "An error occurred while adding the user.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnNewButton.setBounds(6, 157, 81, 29);
        frame.getContentPane().add(btnNewButton);

        // Update Button
        JButton btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedUserId == null) {
                    JOptionPane.showMessageDialog(frame, "Please select a user from the table first.", "No User Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String newEmail = textField_2.getText();
                String newPassword = passwordField.getText();
                boolean isAdmin = rdbtnAdmin.isSelected();

                // Validation checks
                if (newEmail.isEmpty() || newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Email and Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
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

                // Update user details including role
                try (Connection conn = DriverManager.getConnection(DB_URL);
                     PreparedStatement pstmt = conn.prepareStatement(
                             "UPDATE users SET email = ?, password = ?, is_admin = ? WHERE id = ?")) {

                    pstmt.setString(1, newEmail);
                    pstmt.setString(2, hashedPassword);
                    pstmt.setBoolean(3, isAdmin);
                    pstmt.setInt(4, selectedUserId);

                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(frame, "User details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        populateTable();
                        textField_2.setText("");
                        passwordField.setText("");
                        rdbtnUser.setSelected(true); // Reset to default
                        selectedUserId = null;
                    } else {
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

        // Delete Button
        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedUserId == null) {
                    JOptionPane.showMessageDialog(frame, "Please select a user from the table first.", "No User Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(frame,
                        "Are you sure you want to delete user ID " + selectedUserId + "?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try (Connection conn = DriverManager.getConnection(DB_URL);
                         PreparedStatement pstmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {

                        pstmt.setInt(1, selectedUserId);

                        int rowsAffected = pstmt.executeUpdate();

                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(frame, "User with ID " + selectedUserId + " deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                            populateTable();
                            textField_2.setText("");
                            passwordField.setText("");
                            rdbtnUser.setSelected(true); // Reset to default
                            selectedUserId = null;
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

        // Email Label and TextField
        JLabel lblNewLabel_1_2 = new JLabel("Email");
        lblNewLabel_1_2.setBounds(39, 52, 61, 16);
        frame.getContentPane().add(lblNewLabel_1_2);

        textField_2 = new JTextField();
        textField_2.setColumns(10);
        textField_2.setBounds(95, 47, 143, 26);
        frame.getContentPane().add(textField_2);

        // Password Label and PasswordField
        JLabel lblNewLabel_1_2_1 = new JLabel("Password");
        lblNewLabel_1_2_1.setBounds(22, 108, 61, 16);
        frame.getContentPane().add(lblNewLabel_1_2_1);

        passwordField = new JPasswordField();
        passwordField.setBounds(95, 103, 143, 26);
        frame.getContentPane().add(passwordField);

        // Logout Section
        JLabel lblNewLabel_1 = new JLabel("Log out");
        lblNewLabel_1.setBounds(588, 29, 61, 16);
        frame.getContentPane().add(lblNewLabel_1);

        JButton btnNewButton_1 = new JButton("Confirm");
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Connexion connexion = new Connexion();
                connexion.afficher();
                frame.dispose();
            }
        });
        btnNewButton_1.setBounds(555, 58, 117, 29);
        frame.getContentPane().add(btnNewButton_1);
    }

    // Method to populate JTable with data from SQLite database
    private void populateTable() {
        DefaultTableModel model = new DefaultTableModel();
        table.setModel(model);

        // Set the column names
        model.addColumn("ID");
        model.addColumn("Email");
        model.addColumn("Password");
        model.addColumn("Is Admin");

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

            while (rs.next()) {
                // Add rows to the table
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getBoolean("is_admin")
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
    
   
	



	

