package tests;

import static org.junit.Assert.*;
import prefs.PanelInfo;
import prefs.Preferences;

import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

public class BoardSwitchTest {

    Preferences testPrefs;
    private static final String BOARDNAME1 = "Board 1";
    private static final String BOARDNAME2 = "Board 2";
    private static final String BOARDNAME3 = "Board 3";

    @Test
    public void boardsSwitchTest() {
        testPrefs = new Preferences(false);

        List<PanelInfo> board1 = new ArrayList<PanelInfo>();
        List<PanelInfo> board2 = new ArrayList<PanelInfo>();
        List<PanelInfo> board3 = new ArrayList<PanelInfo>();

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
    public void noBoardSwitchTest() {
        testPrefs = new Preferences(false);

        testPrefs.switchBoard();
        assertEquals(false, testPrefs.getLastOpenBoard().isPresent());
    }

    @Test
    public void noBoardOpenSwitchTest() {
        testPrefs = new Preferences(false);

        List<PanelInfo> board1 = new ArrayList<PanelInfo>();
        List<PanelInfo> board2 = new ArrayList<PanelInfo>();
        List<PanelInfo> board3 = new ArrayList<PanelInfo>();

        testPrefs.addBoard(BOARDNAME1, board1);
        testPrefs.addBoard(BOARDNAME2, board2);
        testPrefs.addBoard(BOARDNAME3, board3);

        testPrefs.switchBoard();
        assertEquals(false, testPrefs.getLastOpenBoard().isPresent());
    }

    @Test
    public void oneBoardSwitchTest() {
        testPrefs = new Preferences(false);

        List<PanelInfo> board1 = new ArrayList<PanelInfo>();
        testPrefs.addBoard(BOARDNAME1, board1);
        testPrefs.setLastOpenBoard(BOARDNAME1);

        testPrefs.switchBoard();
        assertEquals(BOARDNAME1, testPrefs.getLastOpenBoard().get());
    }

}
