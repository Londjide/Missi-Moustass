package Auth;

import static org.junit.Assert.*;

import org.junit.Test;

public class LoginTest {

	 @Test
	    public void testValidEmail() {
	        assertTrue(Inscription.isValidEmail("test@example.com"));
	        assertTrue(Inscription.isValidEmail("username@domain.co"));
	        assertFalse(Inscription.isValidEmail("invalid-email"));
	        assertFalse(Inscription.isValidEmail("user@domain"));
	        assertFalse(Inscription.isValidEmail("user@.com"));
	        assertTrue(Connexion.isValidEmail("toto@example.com"));
	        assertTrue(Connexion.isValidEmail("username@domain.co"));
	        assertFalse(Connexion.isValidEmail("invalid-email"));
	        assertFalse(Connexion.isValidEmail("user@domain"));
	        assertFalse(Connexion.isValidEmail("user@.com"));
	    
	    }

	    @Test
	   public void testValidPassword() {
	        assertTrue(Inscription.isValidPassword("StrongP@ss143"));
	        assertFalse(Inscription.isValidPassword("weakpass"));
	        assertFalse(Inscription.isValidPassword("Short1!"));
	        assertFalse(Inscription.isValidPassword("NoSpecialChar1"));
	        assertFalse(Inscription.isValidPassword("nouppercase1!"));
	    }

	    
	    
	   
	    
	  

}