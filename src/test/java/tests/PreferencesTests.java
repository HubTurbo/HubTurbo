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
    public void prefClearLastOpenBoard_mockSessionConfig_sessionConfigRespectiveMethodCalled()
            throws NoSuchFieldException, IllegalAccessException {
        SessionConfig sessionConfig = mock(SessionConfig.class);
        Preferences prefs = TestController.createTestPreferences();
        setSessionConfigField(prefs, sessionConfig);
        prefs.clearLastOpenBoard();

        verify(sessionConfig, times(1)).clearLastOpenBoard();
    }

    /**
     * Tests that Preferences' clearLastOpenBoard method calls SessionConfig's getBoardPanels once
     * and receives corresponding result
     */
    @Test
    public void prefGetBoardPanels_mockSessionConfig_sessionConfigRespectiveMethodCalled()
            throws NoSuchFieldException, IllegalAccessException {
        SessionConfig sessionConfig = mock(SessionConfig.class);
        List<PanelInfo> expected = new ArrayList<>();
        when(sessionConfig.getBoardPanels("board")).thenReturn(expected);

        Preferences prefs = TestController.createTestPreferences();
        setSessionConfigField(prefs, sessionConfig);
        prefs.clearLastOpenBoard();

        List<PanelInfo> actual = prefs.getBoardPanels("board");
        assertEquals(expected, actual);

        final List<PanelInfo> mocked = verify(sessionConfig, times(1)).getBoardPanels("board");
        assertEquals(mocked, null); // this workaround is needed to pass findbugs.
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
