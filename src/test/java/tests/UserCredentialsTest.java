package tests;

import backend.UserCredentials;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class UserCredentialsTest {

    @Test
    public void userCredentialsTest() {
        UserCredentials userCredentials = new UserCredentials("username", "password");
        UserCredentials userCredentials1 = new UserCredentials("test", "test");
        assertNotEquals(userCredentials.hashCode(), userCredentials1.hashCode());
        assertEquals(true, userCredentials.equals(userCredentials));
        assertEquals(false, userCredentials.equals(null));
        assertEquals(false, userCredentials.equals(userCredentials1));
    }

}
