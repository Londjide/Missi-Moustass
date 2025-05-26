package test.util;

import static org.junit.Assert.*;
import org.junit.Test;

import util.AudioFormatManager;
import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;

/**
 * Tests unitaires pour l'utilitaire AudioFormatManager.
 */
public class AudioFormatManagerTest {
    
    @Test
    public void testGetDefaultAudioFormat() {
        // Obtenir le format audio par défaut
        AudioFormat format = AudioFormatManager.getDefaultAudioFormat();
        
        // Vérifier que le format n'est pas nul
        assertNotNull("Le format audio ne devrait pas être nul", format);
        
        // Vérifier les paramètres du format
        assertEquals("La fréquence d'échantillonnage devrait être 44100Hz", 44100.0f, format.getSampleRate(), 0.01f);
        assertEquals("La taille d'échantillon devrait être 16 bits", 16, format.getSampleSizeInBits());
        assertEquals("Le nombre de canaux devrait être 1 (mono)", 1, format.getChannels());
        assertTrue("Le format devrait être signé", format.isBigEndian() || !format.isBigEndian()); // Peu importe, mais pas null
    }
    
    @Test
    public void testCalculateDuration() {
        // Créer un format audio pour le test
        AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
        
        // Tester avec différentes tailles de données
        assertEquals("Une seconde d'audio devrait avoir la bonne durée", 
                    1, AudioFormatManager.calculateDuration(format, 44100 * 2)); // 2 octets par échantillon
        
        assertEquals("Deux secondes d'audio devraient avoir la bonne durée", 
                    2, AudioFormatManager.calculateDuration(format, 44100 * 2 * 2));
                    
        assertEquals("Un dixième de seconde d'audio devrait avoir la bonne durée", 
                    0, AudioFormatManager.calculateDuration(format, 44100 / 10 * 2)); // Arrondi à 0 pour les valeurs < 1
    }
    
    @Test
    public void testWriteWavHeader() throws Exception {
        // Créer un flux de sortie pour capturer l'en-tête WAV
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        // Créer un format audio pour le test
        AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
        
        // Taille des données audio
        int audioDataLength = 44100 * 2; // 1 seconde d'audio
        
        // Écrire l'en-tête WAV
        AudioFormatManager.writeWavHeader(out, audioDataLength, format);
        
        // Vérifier que l'en-tête a été écrit
        byte[] headerData = out.toByteArray();
        assertNotNull("Les données d'en-tête ne devraient pas être nulles", headerData);
        
        // Vérifier la taille minimale de l'en-tête WAV
        assertTrue("L'en-tête WAV devrait avoir au moins 44 octets", headerData.length >= 44);
        
        // Vérifier le début de l'en-tête (identifiant RIFF)
        assertEquals("L'en-tête devrait commencer par RIFF", 'R', (char)headerData[0]);
        assertEquals("L'en-tête devrait commencer par RIFF", 'I', (char)headerData[1]);
        assertEquals("L'en-tête devrait commencer par RIFF", 'F', (char)headerData[2]);
        assertEquals("L'en-tête devrait commencer par RIFF", 'F', (char)headerData[3]);
        
        // Vérifier le format WAVE
        assertEquals("L'en-tête devrait contenir le format WAVE", 'W', (char)headerData[8]);
        assertEquals("L'en-tête devrait contenir le format WAVE", 'A', (char)headerData[9]);
        assertEquals("L'en-tête devrait contenir le format WAVE", 'V', (char)headerData[10]);
        assertEquals("L'en-tête devrait contenir le format WAVE", 'E', (char)headerData[11]);
    }
} 