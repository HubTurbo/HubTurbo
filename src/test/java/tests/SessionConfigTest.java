package tests;

import org.junit.Test;
import prefs.SessionConfig;
import prefs.PanelInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class SessionConfigTest {

    @Test
    public void sessionConfigTest() {
        SessionConfig sessionConfig = new SessionConfig();
        LocalDateTime localDateTime = LocalDateTime.now();
        sessionConfig.setMarkedReadAt("dummy/dummy", 1, localDateTime);
        assertEquals(localDateTime, sessionConfig.getMarkedReadAt("dummy/dummy", 1).get());
        sessionConfig.clearMarkedReadAt("dummy/dummy", 1);
        assertEquals(Optional.empty(), sessionConfig.getMarkedReadAt("dummy/dummy", 1));
        ArrayList<PanelInfo> emptyList = new ArrayList<>();
        sessionConfig.addBoard("board1", emptyList);
        assertEquals(emptyList, sessionConfig.getBoardPanels("board1"));
    }

}
