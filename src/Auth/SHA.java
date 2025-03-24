package Auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA {

    // Method to generate SHA-256 hash
    public static String generateSHA256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data);

        // Convert bytes to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}


