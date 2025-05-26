package view;

import model.Notification;
import service.NotificationService;
import controller.AudioRecorderController;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panneau pour afficher les notifications de l'utilisateur.
 */
public class NotificationPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final int userId;
    private final NotificationService notificationService;
    private final AudioRecorderController controller;
    private final JLabel titleLabel;
    private final JPanel notificationsContainer;
    private final JLabel noNotificationsLabel;
    private final JButton refreshButton;

    /**
     * Constructeur qui initialise le panneau de notifications.
     * 
     * @param userId              L'identifiant de l'utilisateur
     * @param notificationService Le service de notifications
     * @param controller          Le contrôleur pour l'écoute des enregistrements
     */
    public NotificationPanel(int userId, NotificationService notificationService, AudioRecorderController controller) {
        this.userId = userId;
        this.notificationService = notificationService;
        this.controller = controller;

        // Configurer le panneau
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Titre
        titleLabel = new JLabel("Notifications");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Bouton de rafraîchissement
        refreshButton = new JButton("Rafraîchir");
        refreshButton.addActionListener(e -> refreshNotifications());

        // Panneau d'en-tête
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        // Conteneur de notifications avec défilement
        notificationsContainer = new JPanel();
        notificationsContainer.setLayout(new BoxLayout(notificationsContainer, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(notificationsContainer);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Message quand il n'y a pas de notifications
        noNotificationsLabel = new JLabel("Aucune notification");
        noNotificationsLabel.setHorizontalAlignment(JLabel.CENTER);
        notificationsContainer.add(noNotificationsLabel);

        // Ajouter les composants au panneau
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Charger les notifications
        refreshNotifications();
    }

    /**
     * Rafraîchit la liste des notifications.
     */
    public void refreshNotifications() {
        notificationsContainer.removeAll();

        List<Notification> notifications = notificationService.getUserNotifications(userId);

        if (notifications.isEmpty()) {
            notificationsContainer.add(noNotificationsLabel);
        } else {
            // Mettre à jour le nombre de notifications non lues dans le titre
            int unreadCount = (int) notifications.stream().filter(n -> !n.isRead()).count();
            if (unreadCount > 0) {
                titleLabel.setText("Notifications (" + unreadCount + " non lues)");
            } else {
                titleLabel.setText("Notifications");
            }

            // Ajouter chaque notification
            for (Notification notification : notifications) {
                notificationsContainer.add(createNotificationPanel(notification));
                // Ajouter un espace entre les notifications
                notificationsContainer.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        // Rafraîchir l'affichage
        revalidate();
        repaint();
    }

    /**
     * Crée un panneau pour une notification.
     * 
     * @param notification La notification à afficher
     * @return Le panneau pour la notification
     */
    private JPanel createNotificationPanel(Notification notification) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(notification.isRead() ? Color.LIGHT_GRAY : new Color(70, 130, 180), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Message de la notification
        JLabel messageLabel = new JLabel(
                "<html><body style='width: 300px'>" + notification.getMessage() + "</body></html>");
        messageLabel
                .setFont(notification.isRead() ? new Font("Arial", Font.PLAIN, 12) : new Font("Arial", Font.BOLD, 12));

        // Date de la notification
        JLabel dateLabel = new JLabel(notification.getTimestamp().format(DATE_FORMATTER));
        dateLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        dateLabel.setForeground(Color.GRAY);

        // Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Bouton Écouter
        JButton listenButton = new JButton("Écouter");
        listenButton.addActionListener(e -> {
            try {
                controller.playRecording(notification.getRecordingId());
                if (!notification.isRead()) {
                    notificationService.markAsRead(notification.getId());
                    refreshNotifications();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Erreur lors de la lecture: " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Bouton Marquer comme lu/non lu
        JButton markButton = new JButton(notification.isRead() ? "Marquer comme non lu" : "Marquer comme lu");
        markButton.addActionListener(e -> {
            // Inverser l'état de lecture
            if (notification.isRead()) {
                // Cette fonctionnalité nécessiterait une méthode supplémentaire dans le service
                JOptionPane.showMessageDialog(
                        this,
                        "Fonctionnalité en cours de développement",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                notificationService.markAsRead(notification.getId());
                refreshNotifications();
            }
        });

        actionsPanel.add(listenButton);
        actionsPanel.add(markButton);

        // Assembler le panneau
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        contentPanel.add(dateLabel, BorderLayout.SOUTH);

        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(actionsPanel, BorderLayout.SOUTH);

        return panel;
    }
}