package tests;

import backend.resource.TurboUser;
import org.eclipse.egit.github.core.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TurboUserTest {

    @Test
    public void turboUserTest() {
        User user = new User();
        user.setLogin("test");
        TurboUser turboUser = new TurboUser("dummy/dummy", user);
        assertEquals("dummy/dummy", turboUser.getRepoId());
    }

}
