package tests;

import org.junit.Test;
import prefs.SessionConfig;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.TestController;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PreferencesTests {
    /**
     * Tests that Preferences' clearLastOpenBoard method calls SessionConfig's clearLastOpenBoard once
     */
    @Test
    public void testClearLastOpenBoard() {
        SessionConfig sessionConfig = mock(SessionConfig.class);
        Preferences prefs =
                Preferences.load(TestController.TEST_SESSION_CONFIG_FILENAME, TestController.TEST_USER_CONFIG_FILENAME);

        try {
            setSessionConfigField(prefs, sessionConfig);
            prefs.clearLastOpenBoard();

        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            fail();
        }

        verify(sessionConfig, times(1)).clearLastOpenBoard();
    }

    /**
     * Tests that Preferences' clearLastOpenBoard method calls SessionConfig's getBoardPanels once
     * and receives corresponding result
     */
    @Test
    public void testGetBoardPanels() {
        SessionConfig sessionConfig = mock(SessionConfig.class);
        List<PanelInfo> expected = new ArrayList<>();
        when(sessionConfig.getBoardPanels("board")).thenReturn(expected);

        Preferences prefs =
                Preferences.load(TestController.TEST_SESSION_CONFIG_FILENAME, TestController.TEST_USER_CONFIG_FILENAME);
        try {
            setSessionConfigField(prefs, sessionConfig);
            prefs.clearLastOpenBoard();

        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            fail();
        }
        List<PanelInfo> actual = prefs.getBoardPanels("board");

        verify(sessionConfig, times(1)).getBoardPanels("board");
        assertEquals(expected, actual);
    }

    /**
     * Sets the sessionConfig field in a given Preferences object to a specified SessionConfig object
     * @param prefs The Preferences object whose field is to be set
     * @param sessionConfig The SessionConfig object to use
     */
    public void setSessionConfigField(Preferences prefs, SessionConfig sessionConfig)
            throws IllegalAccessException, NoSuchFieldException {
        Field sessionConfigField = Preferences.class.getDeclaredField("sessionConfig");
        sessionConfigField.setAccessible(true);
        sessionConfigField.set(prefs, sessionConfig);
    }
}
