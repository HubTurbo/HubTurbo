package tests;

import guitests.UITest;
import org.junit.Test;
import prefs.SessionConfig;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.TestController;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        Preferences prefs = TestController.createTestPreferences();

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

        Preferences prefs = TestController.createTestPreferences();
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
