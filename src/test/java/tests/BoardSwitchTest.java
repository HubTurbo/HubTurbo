package tests;

import org.junit.Test;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.TestController;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BoardSwitchTest {

    private static final String BOARDNAME1 = "Board 1";
    private static final String BOARDNAME2 = "Board 2";
    private static final String BOARDNAME3 = "Board 3";

    @Test
    public void boardsSwitchTest() {
        Preferences testPrefs = TestController.createTestPreferences();

        List<PanelInfo> board1 = new ArrayList<>();
        List<PanelInfo> board2 = new ArrayList<>();
        List<PanelInfo> board3 = new ArrayList<>();

        testPrefs.addBoard(BOARDNAME1, board1);
        testPrefs.addBoard(BOARDNAME2, board2);
        testPrefs.addBoard(BOARDNAME3, board3);

        testPrefs.setLastOpenBoard(BOARDNAME1);

        testPrefs.switchBoard();
        assertEquals(BOARDNAME3, testPrefs.getLastOpenBoard().get());

        testPrefs.switchBoard();
        assertEquals(BOARDNAME2, testPrefs.getLastOpenBoard().get());

        testPrefs.switchBoard();
        assertEquals(BOARDNAME1, testPrefs.getLastOpenBoard().get());

        testPrefs.switchBoard();
        assertEquals(BOARDNAME3, testPrefs.getLastOpenBoard().get());

    }

    @Test
    public void deletedBoardOpenSwitchTest() {
        Preferences testPrefs = TestController.createTestPreferences();

        List<PanelInfo> board1 = new ArrayList<>();
        List<PanelInfo> board2 = new ArrayList<>();
        List<PanelInfo> board3 = new ArrayList<>();

        testPrefs.addBoard(BOARDNAME1, board1);
        testPrefs.addBoard(BOARDNAME2, board2);
        testPrefs.addBoard(BOARDNAME3, board3);

        testPrefs.setLastOpenBoard(BOARDNAME2);
        testPrefs.removeBoard(BOARDNAME2);

        testPrefs.switchBoard();
        assertEquals(BOARDNAME3, testPrefs.getLastOpenBoard().get());

        testPrefs.switchBoard();
        assertEquals(BOARDNAME1, testPrefs.getLastOpenBoard().get());

        testPrefs.switchBoard();
        assertEquals(BOARDNAME3, testPrefs.getLastOpenBoard().get());
    }

    @Test
    public void noBoardSwitchTest() {
        Preferences testPrefs = TestController.createTestPreferences();

        testPrefs.switchBoard();
        assertEquals(false, testPrefs.getLastOpenBoard().isPresent());
    }

    @Test
    public void noBoardOpenSwitchTest() {
        Preferences testPrefs = TestController.createTestPreferences();

        List<PanelInfo> board1 = new ArrayList<>();
        List<PanelInfo> board2 = new ArrayList<>();
        List<PanelInfo> board3 = new ArrayList<>();

        testPrefs.addBoard(BOARDNAME1, board1);
        testPrefs.addBoard(BOARDNAME2, board2);
        testPrefs.addBoard(BOARDNAME3, board3);

        testPrefs.switchBoard();
        assertEquals(true, testPrefs.getLastOpenBoard().isPresent());
    }

    @Test
    public void oneBoardSwitchTest() {
        Preferences testPrefs = TestController.createTestPreferences();

        List<PanelInfo> board1 = new ArrayList<>();
        testPrefs.addBoard(BOARDNAME1, board1);
        testPrefs.setLastOpenBoard(BOARDNAME1);

        testPrefs.switchBoard();
        assertEquals(BOARDNAME1, testPrefs.getLastOpenBoard().get());
    }

}
