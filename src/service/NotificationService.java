package service;

import model.Notification;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les notifications utilisateur.
 */
public class NotificationService {

    private final DatabaseService databaseService;

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS notifications ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "user_id INTEGER NOT NULL,"
            + "message TEXT NOT NULL,"
            + "is_read INTEGER DEFAULT 0,"
            + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "recording_id INTEGER NOT NULL"
            + ")";

    private static final String INSERT_NOTIFICATION_QUERY = "INSERT INTO notifications "
            + "(user_id, message, is_read, timestamp, recording_id) "
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String GET_USER_NOTIFICATIONS_QUERY = "SELECT * FROM notifications "
            + "WHERE user_id = ? ORDER BY timestamp DESC";

    private static final String MARK_AS_READ_QUERY = "UPDATE notifications "
            + "SET is_read = 1 WHERE id = ?";

    private static final String COUNT_UNREAD_QUERY = "SELECT COUNT(*) FROM notifications "
            + "WHERE user_id = ? AND is_read = 0";

    /**
     * Constructeur qui initialise le service avec la dépendance de base de données.
     * 
     * @param databaseService Le service de base de données
     */
    public NotificationService(DatabaseService databaseService) {
        this.databaseService = databaseService;

        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crée une nouvelle notification pour un utilisateur.
     * 
     * @param userId      L'identifiant de l'utilisateur destinataire
     * @param message     Le message de la notification
     * @param recordingId L'identifiant de l'enregistrement concerné
     * @return true si la création a réussi, false sinon
     */
    public boolean createNotification(int userId, String message, int recordingId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = databaseService.connect();
            stmt = conn.prepareStatement(INSERT_NOTIFICATION_QUERY);
            stmt.setInt(1, userId);
            stmt.setString(2, message);
            stmt.setInt(3, 0); // Non lu par défaut
            stmt.setString(4, LocalDateTime.now().toString());
            stmt.setInt(5, recordingId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création d'une notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * Récupère toutes les notifications d'un utilisateur.
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return La liste des notifications
     */
    public List<Notification> getUserNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.connect();
            stmt = conn.prepareStatement(GET_USER_NOTIFICATIONS_QUERY);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Notification notification = new Notification(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("message"),
                        rs.getInt("is_read") == 1,
                        LocalDateTime.parse(rs.getString("timestamp")),
                        rs.getInt("recording_id"));
                notifications.add(notification);
            }

            return notifications;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des notifications: " + e.getMessage());
            e.printStackTrace();
            return notifications;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * Marque une notification comme lue.
     * 
     * @param notificationId L'identifiant de la notification
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean markAsRead(int notificationId) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = databaseService.connect();
            stmt = conn.prepareStatement(MARK_AS_READ_QUERY);
            stmt.setInt(1, notificationId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du marquage d'une notification comme lue: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    /**
     * Compte le nombre de notifications non lues pour un utilisateur.
     * 
     * @param userId L'identifiant de l'utilisateur
     * @return Le nombre de notifications non lues
     */
    public int countUnreadNotifications(int userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.connect();
            stmt = conn.prepareStatement(COUNT_UNREAD_QUERY);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des notifications non lues: " + e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    /**
     * Crée la table des notifications si elle n'existe pas.
     * 
     * @throws SQLException Si une erreur SQL survient
     */
    private void createTableIfNotExists() throws SQLException {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = databaseService.connect();
            stmt = conn.createStatement();
            stmt.executeUpdate(CREATE_TABLE_QUERY);

            System.out.println("Table notifications créée ou existante");
        } finally {
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        }
    }

    /**
     * Ferme les ressources JDBC.
     * 
     * @param rs   Le ResultSet à fermer
     * @param stmt Le PreparedStatement à fermer
     * @param conn La Connection à fermer
     */
    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                /* ignore */ }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                /* ignore */ }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                /* ignore */ }
        }
    }
}