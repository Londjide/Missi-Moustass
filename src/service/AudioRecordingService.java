package service;

import model.AudioRecording;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface pour le service d'enregistrement audio.
 * Ce service gère toutes les opérations liées aux enregistrements audio :
 * enregistrement, lecture, stockage et récupération.
 */
public interface AudioRecordingService {
    
    /**
     * Démarre l'enregistrement audio.
     * 
     * @param fileName Le nom du fichier dans lequel sauvegarder l'enregistrement
     */
    void startRecording(String fileName);
    
    /**
     * Arrête l'enregistrement en cours.
     */
    void stopRecording();
    
    /**
     * Joue un enregistrement audio.
     * 
     * @param fileName Le nom du fichier à lire
     */
    void playRecording(String fileName);
    
    /**
     * Arrête la lecture d'un enregistrement audio.
     */
    void stopPlaying();
    
    /**
     * Sauvegarde un enregistrement dans la base de données.
     * 
     * @param recording L'enregistrement à sauvegarder
     * @return L'identifiant de l'enregistrement sauvegardé
     * @throws Exception Si une erreur survient lors de la sauvegarde
     */
    int saveRecording(AudioRecording recording) throws Exception;
    
    /**
     * Récupère un enregistrement spécifique.
     * 
     * @param recordingId L'identifiant de l'enregistrement à récupérer
     * @return L'enregistrement correspondant à l'identifiant
     * @throws Exception Si une erreur survient lors de la récupération
     */
    AudioRecording getRecording(int recordingId) throws Exception;
    
    /**
     * Crée un nouvel objet AudioRecording.
     * 
     * @param name Le nom de l'enregistrement
     * @param timestamp La date et l'heure de l'enregistrement
     * @param duration La durée de l'enregistrement en secondes
     * @param userId L'identifiant de l'utilisateur propriétaire
     * @return Un nouvel objet AudioRecording
     */
    AudioRecording createRecording(String name, LocalDateTime timestamp, int duration,
                                   int userId);
    
    /**
     * Récupère tous les enregistrements d'un répertoire.
     * 
     * @param directory Le répertoire contenant les enregistrements
     * @return Une liste d'enregistrements
     */
    List<AudioRecording> getRecordings(String directory);
    
    /**
     * Supprime un enregistrement de la base de données.
     * 
     * @param recordingId L'identifiant de l'enregistrement à supprimer
     * @return Vrai si l'enregistrement a été supprimé, faux sinon
     * @throws Exception Si une erreur survient lors de la suppression
     */
    boolean deleteRecording(int recordingId) throws Exception;
    
    /**
     * Vérifie si un enregistrement est en cours d'enregistrement.
     * 
     * @return Vrai si un enregistrement est en cours d'enregistrement, faux sinon
     */
    boolean isRecording();
    
    /**
     * Vérifie si un enregistrement est en cours de lecture.
     * 
     * @return Vrai si un enregistrement est en cours de lecture, faux sinon
     */
    boolean isPlaying();
} 