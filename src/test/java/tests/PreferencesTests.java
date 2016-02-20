package tests;

import org.junit.Test;
import prefs.GlobalConfig;
import prefs.PanelInfo;
import prefs.Preferences;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PreferencesTests {
    /**
     * Tests that Preferences' clearLastOpenBoard method calls GlobalConfig's clearLastOpenBoard once
     */
    @Test
    public void testClearLastOpenBoard() {
        GlobalConfig config = mock(GlobalConfig.class);
        Preferences prefs = Preferences.load(Preferences.TEST_SESSION_CONFIG_FILE);

        prefs.global = config;
        prefs.clearLastOpenBoard();

        verify(config, times(1)).clearLastOpenBoard();
    }

    /**
     * Tests that Preferences' clearLastOpenBoard method calls GlobalConfig's getBoardPanels once
     * and receives corresponding result
     */
    @Test
    public void testGetBoardPanels() {
        GlobalConfig config = mock(GlobalConfig.class);
        List<PanelInfo> expected = new ArrayList<>();
        when(config.getBoardPanels("board")).thenReturn(expected);

        Preferences prefs = Preferences.load(Preferences.TEST_SESSION_CONFIG_FILE);
        prefs.global = config;
        List<PanelInfo> actual = prefs.getBoardPanels("board");

        verify(config, times(1)).getBoardPanels("board");
        assertEquals(expected, actual);
    }
}
