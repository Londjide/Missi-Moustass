package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe utilitaire pour gérer la journalisation (logging).
 * Cette classe suit le principe de responsabilité unique (SRP) en ne gérant
 * que les fonctionnalités liées à la journalisation.
 */
public class LogManager {
    
    // Niveaux de journalisation
    public enum LogLevel {
        DEBUG, INFO, WARNING, ERROR, FATAL
    }
    
    private static final String LOG_DIRECTORY = "logs";
    private static final String LOG_FILE_PREFIX = "app_";
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    
    private static LogLevel currentLogLevel = LogLevel.INFO;
    private static boolean consoleOutput = true;
    
    /**
     * Initialise le gestionnaire de journalisation.
     * Crée le répertoire de journalisation s'il n'existe pas.
     */
    public static void initialize() {
        File logDir = new File(LOG_DIRECTORY);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }
    
    /**
     * Définit le niveau de journalisation actuel.
     * 
     * @param level Le niveau de journalisation à définir
     */
    public static void setLogLevel(LogLevel level) {
        currentLogLevel = level;
    }
    
    /**
     * Active ou désactive la sortie console des journaux.
     * 
     * @param enabled true pour activer la sortie console, false pour la désactiver
     */
    public static void setConsoleOutput(boolean enabled) {
        consoleOutput = enabled;
    }
    
    /**
     * Journalise un message de niveau DEBUG.
     * 
     * @param tag Un tag pour identifier la source du message
     * @param message Le message à journaliser
     */
    public static void debug(String tag, String message) {
        log(LogLevel.DEBUG, tag, message, null);
    }
    
    /**
     * Journalise un message de niveau INFO.
     * 
     * @param tag Un tag pour identifier la source du message
     * @param message Le message à journaliser
     */
    public static void info(String tag, String message) {
        log(LogLevel.INFO, tag, message, null);
    }
    
    /**
     * Journalise un message de niveau WARNING.
     * 
     * @param tag Un tag pour identifier la source du message
     * @param message Le message à journaliser
     */
    public static void warning(String tag, String message) {
        log(LogLevel.WARNING, tag, message, null);
    }
    
    /**
     * Journalise un message de niveau ERROR.
     * 
     * @param tag Un tag pour identifier la source du message
     * @param message Le message à journaliser
     * @param throwable L'exception associée à l'erreur (peut être null)
     */
    public static void error(String tag, String message, Throwable throwable) {
        log(LogLevel.ERROR, tag, message, throwable);
    }
    
    /**
     * Journalise un message de niveau FATAL.
     * 
     * @param tag Un tag pour identifier la source du message
     * @param message Le message à journaliser
     * @param throwable L'exception associée à l'erreur (peut être null)
     */
    public static void fatal(String tag, String message, Throwable throwable) {
        log(LogLevel.FATAL, tag, message, throwable);
    }
    
    /**
     * Journalise un message avec le niveau et les informations spécifiés.
     * 
     * @param level Le niveau de journalisation
     * @param tag Un tag pour identifier la source du message
     * @param message Le message à journaliser
     * @param throwable L'exception associée (peut être null)
     */
    private static void log(LogLevel level, String tag, String message, Throwable throwable) {
        if (level.ordinal() < currentLogLevel.ordinal()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        String logLine = String.format("[%s] [%s] [%s] %s: %s",
                now.format(TIME_FORMATTER),
                level.toString(),
                Thread.currentThread().getName(),
                tag,
                message);
        
        if (consoleOutput) {
            // Écrire dans la console
            System.out.println(logLine);
            
            if (throwable != null) {
                throwable.printStackTrace(System.out);
            }
        }
        
        // Écrire dans le fichier de journal
        writeToLogFile(now, logLine, throwable);
    }
    
    /**
     * Écrit une ligne de journal dans le fichier de journal du jour.
     * 
     * @param dateTime La date et l'heure du message
     * @param logLine La ligne de journal à écrire
     * @param throwable L'exception associée (peut être null)
     */
    private static void writeToLogFile(LocalDateTime dateTime, String logLine, Throwable throwable) {
        String fileName = LOG_FILE_PREFIX + dateTime.format(DATE_FORMATTER) + LOG_FILE_EXTENSION;
        File logFile = new File(LOG_DIRECTORY, fileName);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println(logLine);
            
            if (throwable != null) {
                throwable.printStackTrace(writer);
            }
        } catch (IOException e) {
            // En cas d'erreur d'écriture dans le fichier, afficher l'erreur dans la console
            System.err.println("Erreur lors de l'écriture dans le fichier de journal: " + e.getMessage());
        }
    }
} 