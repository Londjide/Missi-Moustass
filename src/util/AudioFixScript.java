package util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utilitaire pour diagnostiquer et réparer les problèmes audio.
 */
public class AudioFixScript {
    
    private static final String DB_URL = "jdbc:sqlite:database.db";
    
    /**
     * Point d'entrée pour l'outil de diagnostic audio.
     */
    public static void main(String[] args) {
        try {
            // Charger le pilote SQLite
            Class.forName("org.sqlite.JDBC");
            
            System.out.println("=== Diagnostic Audio ===");
            
            // Tester les périphériques audio
            testAudioDevices();
            
            // Tester les formats audio
            testAudioFormats();
            
            // Tester un enregistrement simple
            testRecordingPlayback();
            
            // Vérifier les enregistrements dans la base de données
            checkDatabaseRecordings();
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Teste les périphériques audio disponibles.
     */
    private static void testAudioDevices() {
        System.out.println("\n--- Périphériques Audio ---");
        
        // Mixer Info pour la capture (entrée)
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        System.out.println("Périphériques disponibles: " + mixerInfos.length);
        
        for (int i = 0; i < mixerInfos.length; i++) {
            Mixer.Info info = mixerInfos[i];
            Mixer mixer = AudioSystem.getMixer(info);
            
            Line.Info[] sourceLines = mixer.getSourceLineInfo();
            Line.Info[] targetLines = mixer.getTargetLineInfo();
            
            System.out.println((i + 1) + ". " + info.getName());
            System.out.println("   - Description: " + info.getDescription());
            System.out.println("   - Lignes source (sortie): " + sourceLines.length);
            System.out.println("   - Lignes cible (entrée): " + targetLines.length);
            
            // Vérifier si ce périphérique peut être utilisé pour l'enregistrement
            boolean canRecord = false;
            for (Line.Info lineInfo : targetLines) {
                if (lineInfo.getLineClass() == TargetDataLine.class) {
                    canRecord = true;
                    break;
                }
            }
            System.out.println("   - Peut enregistrer: " + canRecord);
        }
    }
    
    /**
     * Teste les formats audio supportés.
     */
    private static void testAudioFormats() {
        System.out.println("\n--- Formats Audio ---");
        
        // Format standard pour l'enregistrement
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        
        System.out.println("Format standard:");
        System.out.println("- Taux d'échantillonnage: " + format.getSampleRate() + " Hz");
        System.out.println("- Taille des échantillons: " + format.getSampleSizeInBits() + " bits");
        System.out.println("- Canaux: " + format.getChannels());
        System.out.println("- Encodage: " + format.getEncoding());
        System.out.println("- Big Endian: " + format.isBigEndian());
        
        // Vérifier si ce format est supporté pour l'enregistrement
        boolean isSupported = AudioSystem.isLineSupported(new DataLine.Info(TargetDataLine.class, format));
        System.out.println("Ce format est supporté pour l'enregistrement: " + isSupported);
        
        if (!isSupported) {
            System.out.println("\nTentative avec d'autres formats:");
            
            // Essayer d'autres formats
            float[] sampleRates = {8000, 11025, 16000, 22050, 44100, 48000};
            int[] sampleSizes = {8, 16, 24};
            int[] channels = {1, 2};
            boolean[] bigEndians = {false, true};
            
            for (float sampleRate : sampleRates) {
                for (int sampleSize : sampleSizes) {
                    for (int channel : channels) {
                        for (boolean bigEndian : bigEndians) {
                            format = new AudioFormat(sampleRate, sampleSize, channel, true, bigEndian);
                            isSupported = AudioSystem.isLineSupported(new DataLine.Info(TargetDataLine.class, format));
                            
                            if (isSupported) {
                                System.out.println("Format supporté trouvé:");
                                System.out.println("- Taux d'échantillonnage: " + format.getSampleRate() + " Hz");
                                System.out.println("- Taille des échantillons: " + format.getSampleSizeInBits() + " bits");
                                System.out.println("- Canaux: " + format.getChannels());
                                System.out.println("- Big Endian: " + format.isBigEndian());
                                break;
                            }
                        }
                        if (isSupported) break;
                    }
                    if (isSupported) break;
                }
                if (isSupported) break;
            }
        }
    }
    
    /**
     * Teste l'enregistrement et la lecture audio.
     */
    private static void testRecordingPlayback() {
        System.out.println("\n--- Test d'Enregistrement et Lecture ---");
        
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
        
        if (!AudioSystem.isLineSupported(targetInfo)) {
            System.out.println("Format audio non supporté pour l'enregistrement. Test impossible.");
            return;
        }
        
        // Créer un fichier temporaire
        File tempFile = new File("test_recording.wav");
        
        try {
            System.out.println("Enregistrement de 3 secondes...");
            
            // Ouvrir la ligne d'enregistrement
            TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetLine.open(format);
            targetLine.start();
            
            // Enregistrer dans un fichier WAV
            AudioInputStream ais = new AudioInputStream(targetLine);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, tempFile);
            
            // Arrêter l'enregistrement après 3 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    targetLine.stop();
                    targetLine.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            
            // Attendre la fin de l'enregistrement
            while (targetLine.isActive()) {
                Thread.sleep(100);
            }
            
            System.out.println("Enregistrement terminé. Fichier: " + tempFile.getAbsolutePath());
            System.out.println("Taille du fichier: " + (tempFile.length() / 1024) + " KB");
            
            // Lecture de l'enregistrement
            System.out.println("Lecture de l'enregistrement...");
            
            AudioInputStream playbackStream = AudioSystem.getAudioInputStream(tempFile);
            DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, playbackStream.getFormat());
            SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            
            sourceLine.open(playbackStream.getFormat());
            sourceLine.start();
            
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            
            while ((bytesRead = playbackStream.read(buffer, 0, buffer.length)) != -1) {
                sourceLine.write(buffer, 0, bytesRead);
            }
            
            sourceLine.drain();
            sourceLine.stop();
            sourceLine.close();
            playbackStream.close();
            
            System.out.println("Lecture terminée.");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test d'enregistrement/lecture: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Supprimer le fichier temporaire
            if (tempFile.exists()) {
                tempFile.delete();
                System.out.println("Fichier temporaire supprimé.");
            }
        }
    }
    
    /**
     * Vérifie les enregistrements dans la base de données.
     */
    private static void checkDatabaseRecordings() {
        System.out.println("\n--- Enregistrements dans la Base de Données ---");
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Vérifier si la table recordings existe
            ResultSet tables = conn.getMetaData().getTables(null, null, "recordings", null);
            if (!tables.next()) {
                System.out.println("La table 'recordings' n'existe pas dans la base de données!");
                return;
            }
            
            // Compter les enregistrements
            ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM recordings");
            if (countRs.next()) {
                int count = countRs.getInt(1);
                System.out.println("Nombre d'enregistrements: " + count);
            }
            
            // Afficher les détails des enregistrements
            ResultSet rs = stmt.executeQuery("SELECT id, user_id, name, timestamp, file_path, duration FROM recordings");
            
            System.out.println("\nListe des enregistrements:");
            System.out.println("+----+----------+--------------------+---------------------+-----------------------------+----------+");
            System.out.println("| ID | User ID  | Nom                | Date                | Chemin                      | Durée(s) |");
            System.out.println("+----+----------+--------------------+---------------------+-----------------------------+----------+");
            
            boolean hasRecordings = false;
            while (rs.next()) {
                hasRecordings = true;
                int id = rs.getInt("id");
                int userId = rs.getInt("user_id");
                String name = rs.getString("name");
                String timestamp = rs.getString("timestamp");
                String filePath = rs.getString("file_path");
                int duration = rs.getInt("duration");
                
                // Vérifier si le fichier existe
                boolean fileExists = false;
                if (filePath != null) {
                    File file = new File(filePath);
                    fileExists = file.exists();
                }
                
                System.out.printf("| %-2d | %-8d | %-18s | %-19s | %-27s | %-8d | %s\n", 
                    id, userId, name, timestamp, filePath != null ? filePath : "(null)", duration,
                    filePath != null ? (fileExists ? "[OK]" : "[MANQUANT]") : "");
            }
            
            System.out.println("+----+----------+--------------------+---------------------+-----------------------------+----------+");
            
            if (!hasRecordings) {
                System.out.println("Aucun enregistrement trouvé dans la base de données!");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des enregistrements: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 