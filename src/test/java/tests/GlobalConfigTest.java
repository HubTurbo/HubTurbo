package tests;

import org.junit.Test;
import prefs.GlobalConfig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class GlobalConfigTest {

    @Test
    public void globalConfigTest() {
        GlobalConfig globalConfig = new GlobalConfig();
        LocalDateTime localDateTime = LocalDateTime.now();
        globalConfig.setMarkedReadAt("dummy/dummy", 1, localDateTime);
        assertEquals(localDateTime, globalConfig.getMarkedReadAt("dummy/dummy", 1).get());
        globalConfig.clearMarkedReadAt("dummy/dummy", 1);
        assertEquals(Optional.empty(), globalConfig.getMarkedReadAt("dummy/dummy", 1));
        ArrayList<String> emptyList = new ArrayList<>();
        globalConfig.addBoard("board1", emptyList);
        assertEquals(emptyList, globalConfig.getBoardPanels("board1"));

    }

}
