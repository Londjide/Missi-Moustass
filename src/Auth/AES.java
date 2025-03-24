package Auth;


	import javax.crypto.Cipher;
	import javax.crypto.KeyGenerator;
	import javax.crypto.SecretKey;
	import javax.crypto.spec.SecretKeySpec;
	import java.util.Base64;

	public class AES {

	    private static final String ALGORITHM = "AES";
	    private static final int KEY_SIZE = 256;

	    // Generate a secret key for AES
	    public static SecretKey generateSecretKey() throws Exception {
	        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
	        keyGen.init(KEY_SIZE);
	        return keyGen.generateKey();
	    }

	    // Encrypt data using AES
	    public static byte[] encrypt(byte[] data, SecretKey key) throws Exception {
	        Cipher cipher = Cipher.getInstance(ALGORITHM);
	        cipher.init(Cipher.ENCRYPT_MODE, key);
	        return cipher.doFinal(data);
	    }

	    // Decrypt data using AES
	    public static byte[] decrypt(byte[] encryptedData, SecretKey key) throws Exception {
	        Cipher cipher = Cipher.getInstance(ALGORITHM);
	        cipher.init(Cipher.DECRYPT_MODE, key);
	        return cipher.doFinal(encryptedData);
	    }

	    // Convert SecretKey to a Base64 string (to store or transfer the key)
	    public static String encodeKeyToBase64(SecretKey key) {
	        return Base64.getEncoder().encodeToString(key.getEncoded());
	    }

	    // Convert Base64 string to SecretKey
	    public static SecretKey decodeKeyFromBase64(String keyBase64) {
	        byte[] decodedKey = Base64.getDecoder().decode(keyBase64);
	        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
	    }
	}


