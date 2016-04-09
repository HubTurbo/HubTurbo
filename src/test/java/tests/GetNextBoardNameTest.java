package tests;

import org.junit.Test;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.TestController;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GetNextBoardNameTest {

    private static final String BOARDNAME1 = "Board 1";
    private static final String BOARDNAME2 = "Board 2";
    private static final String BOARDNAME3 = "Board 3";

    @Test
    public void getNextBoardNameTest_multipleBoards_lastOpenBoardExists_returnNextBoardName() {
        Preferences testPrefs = TestController.createTestPreferences();

        List<PanelInfo> board1 = new ArrayList<>();
        List<PanelInfo> board2 = new ArrayList<>();
        List<PanelInfo> board3 = new ArrayList<>();

        testPrefs.addBoard(BOARDNAME1, board1);
        testPrefs.addBoard(BOARDNAME2, board2);
        testPrefs.addBoard(BOARDNAME3, board3);


        testPrefs.setLastOpenBoard(BOARDNAME1);
        assertEquals(BOARDNAME3, testPrefs.getNextBoardName().get());

        testPrefs.setLastOpenBoard(BOARDNAME3);
        assertEquals(BOARDNAME2, testPrefs.getNextBoardName().get());

        testPrefs.setLastOpenBoard(BOARDNAME2);
        assertEquals(BOARDNAME1, testPrefs.getNextBoardName().get());

        testPrefs.setLastOpenBoard(BOARDNAME1);
        assertEquals(BOARDNAME3, testPrefs.getNextBoardName().get());
    }

    @Test
    public void getNextBoardNameTest_multipleBoards_noBoardOpen_returnFirstBoard() {
        Preferences testPrefs = TestController.createTestPreferences();

        List<PanelInfo> board1 = new ArrayList<>();
        List<PanelInfo> board2 = new ArrayList<>();
        List<PanelInfo> board3 = new ArrayList<>();

        testPrefs.addBoard(BOARDNAME1, board1);
        testPrefs.addBoard(BOARDNAME2, board2);
        testPrefs.addBoard(BOARDNAME3, board3);

        testPrefs.getNextBoardName();
        assertEquals(false, testPrefs.getLastOpenBoard().isPresent());
        assertEquals(BOARDNAME3, testPrefs.getNextBoardName().get());
    }

    @Test
    public void getNextBoardNameTest_noBoards_returnNothing() {
        Preferences testPrefs = TestController.createTestPreferences();

        testPrefs.getNextBoardName();
        assertEquals(false, testPrefs.getLastOpenBoard().isPresent());
    }

    @Test
    public void getNextBoardNameTest_singleBoard_returnTheOnlyBoard() {
        Preferences testPrefs = TestController.createTestPreferences();

        List<PanelInfo> board1 = new ArrayList<>();
        testPrefs.addBoard(BOARDNAME1, board1);
        testPrefs.setLastOpenBoard(BOARDNAME1);

        testPrefs.getNextBoardName();
        assertEquals(BOARDNAME1, testPrefs.getLastOpenBoard().get());
    }

}
