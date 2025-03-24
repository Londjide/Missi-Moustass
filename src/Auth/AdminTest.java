package Auth;

import static org.junit.Assert.*;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AdminTest {
    private static final String TEST_DB_URL = "jdbc:sqlite:test_users.db";
    private Connection conn;

    // Establish a fresh connection before each test
    @Before
    public void setup() throws SQLException {
        conn = DriverManager.getConnection(TEST_DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT NOT NULL, password TEXT NOT NULL)");
        }
    }

    // Clean database before each test
    @Before
    public void cleanDatabase() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
        }
    }

    // Close connection after each test
    @After
    public void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    // Test Create User
    @Test
    public void testCreateUser() throws SQLException {
        String email = "testuser@example.com";
        String password = "Test@12345";

        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (email, password) VALUES (?, ?)")) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            int rowsAffected = pstmt.executeUpdate();
            assertEquals(1, rowsAffected);
        }

        // Verify user exists
        try (PreparedStatement pstmtCheck = conn.prepareStatement("SELECT email, password FROM users WHERE email = ?")) {
            pstmtCheck.setString(1, email);
            try (ResultSet rs = pstmtCheck.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(email, rs.getString("email"));
                assertEquals(password, rs.getString("password"));
            }
        }
    }

    // Test Update User
    @Test
    public void testUpdateUser() throws SQLException {
        String email = "testuser@example.com";
        String password = "Test@1234567";

        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (email, password) VALUES (?, ?)")) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        }

        String newEmail = "updateduser@example.com";
        String newPassword = "Updated@1234567";
        try (PreparedStatement pstmtUpdate = conn.prepareStatement("UPDATE users SET email = ?, password = ? WHERE email = ?")) {
            pstmtUpdate.setString(1, newEmail);
            pstmtUpdate.setString(2, newPassword);
            pstmtUpdate.setString(3, email);
            int rowsAffected = pstmtUpdate.executeUpdate();
            assertEquals(1, rowsAffected);
        }

        // Verify update
        try (PreparedStatement pstmtCheck = conn.prepareStatement("SELECT email, password FROM users WHERE email = ?")) {
            pstmtCheck.setString(1, newEmail);
            try (ResultSet rs = pstmtCheck.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(newEmail, rs.getString("email"));
                assertEquals(newPassword, rs.getString("password"));
            }
        }
    }
}
