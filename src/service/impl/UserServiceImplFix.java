package service.impl;

import model.User;
import service.CryptographyService;
import service.DatabaseService;
import service.UserService;
import service.ServiceFactoryFix;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserServiceImplFix implements UserService {

    private final DatabaseService databaseService;
    private final CryptographyService cryptographyService;

    public UserServiceImplFix(DatabaseService databaseService, CryptographyService cryptographyService) {
        this.databaseService = databaseService;
        this.cryptographyService = cryptographyService;
    }

    @Override
    public int authenticateUser(String email, String password) throws Exception {
        User user = getUserByEmail(email);
        if (user != null && user.getPasswordHash()
                .equals(cryptographyService.generateHash((password + user.getSalt()).getBytes()))) {
            return user.getId();
        }
        return -1;
    }

    @Override
    public int createUser(String email, String password, boolean isAdmin) throws Exception {
        String salt = generateSalt();
        String passwordHash = cryptographyService.generateHash((password + salt).getBytes());

        String query = "INSERT INTO users (email, password_hash, salt, is_admin) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseService.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, passwordHash);
            stmt.setString(3, salt);
            stmt.setBoolean(4, isAdmin);
            return stmt.executeUpdate();
        }
    }

    @Override
    public User getUserById(int id) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = databaseService.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getBoolean("is_admin"),
                            rs.getString("salt"));
                }
            }
        }
        return null;
    }

    @Override
    public User getUserByEmail(String email) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = databaseService.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getBoolean("is_admin"),
                            rs.getString("salt"));
                }
            }
        }
        return null;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";

        try (Connection conn = databaseService.connect();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getBoolean("is_admin"),
                        rs.getString("salt")));
            }
        }
        return users;
    }

    @Override
    public boolean isAdmin(int userId) throws Exception {
        User user = getUserById(userId);
        return user != null && user.isAdmin();
    }

    @Override
    public boolean resetPassword(int userId, String newPassword) throws Exception {
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }

        String salt = generateSalt();
        String passwordHash = cryptographyService.generateHash((newPassword + salt).getBytes());

        String query = "UPDATE users SET password_hash = ?, salt = ? WHERE id = ?";
        try (Connection conn = databaseService.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, passwordHash);
            stmt.setString(2, salt);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public void updateUser(User user) throws Exception {
        String query = "UPDATE users SET email = ?, is_admin = ? WHERE id = ?";
        try (Connection conn = databaseService.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getEmail());
            stmt.setBoolean(2, user.isAdmin());
            stmt.setInt(3, user.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteUser(int id) throws Exception {
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection conn = databaseService.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        new Random().nextBytes(salt);
        return java.util.Base64.getEncoder().encodeToString(salt);
    }
}