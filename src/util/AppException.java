package util;

/**
 * Exception personnalisée pour l'application.
 * Cette classe permet de mieux gérer les erreurs spécifiques à l'application.
 */
public class AppException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Type d'erreur de l'application.
     */
    public enum ErrorType {
        // Erreurs d'authentification
        AUTHENTICATION_FAILED("Échec de l'authentification"),
        INVALID_CREDENTIALS("Identifiants invalides"),
        USER_NOT_FOUND("Utilisateur non trouvé"),
        
        // Erreurs de base de données
        DATABASE_CONNECTION_FAILED("Échec de la connexion à la base de données"),
        DATABASE_QUERY_FAILED("Échec de la requête à la base de données"),
        
        // Erreurs de cryptographie
        ENCRYPTION_FAILED("Échec du chiffrement"),
        DECRYPTION_FAILED("Échec du déchiffrement"),
        INTEGRITY_CHECK_FAILED("Échec de la vérification d'intégrité"),
        
        // Erreurs d'enregistrement audio
        RECORDING_FAILED("Échec de l'enregistrement audio"),
        PLAYBACK_FAILED("Échec de la lecture audio"),
        AUDIO_FORMAT_UNSUPPORTED("Format audio non supporté"),
        
        // Erreurs de fichier
        FILE_NOT_FOUND("Fichier non trouvé"),
        FILE_ACCESS_DENIED("Accès au fichier refusé"),
        
        // Erreurs générales
        INVALID_INPUT("Entrée invalide"),
        OPERATION_CANCELLED("Opération annulée"),
        INTERNAL_ERROR("Erreur interne");
        
        private final String message;
        
        ErrorType(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private final ErrorType errorType;
    
    /**
     * Crée une nouvelle exception avec un type d'erreur prédéfini.
     * 
     * @param errorType Le type d'erreur
     */
    public AppException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }
    
    /**
     * Crée une nouvelle exception avec un type d'erreur prédéfini et un message personnalisé.
     * 
     * @param errorType Le type d'erreur
     * @param message Le message personnalisé
     */
    public AppException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    /**
     * Crée une nouvelle exception avec un type d'erreur prédéfini et une cause.
     * 
     * @param errorType Le type d'erreur
     * @param cause La cause de l'exception
     */
    public AppException(ErrorType errorType, Throwable cause) {
        super(errorType.getMessage(), cause);
        this.errorType = errorType;
    }
    
    /**
     * Crée une nouvelle exception avec un type d'erreur prédéfini, un message personnalisé et une cause.
     * 
     * @param errorType Le type d'erreur
     * @param message Le message personnalisé
     * @param cause La cause de l'exception
     */
    public AppException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    /**
     * Obtient le type d'erreur de cette exception.
     * 
     * @return Le type d'erreur
     */
    public ErrorType getErrorType() {
        return errorType;
    }
} 