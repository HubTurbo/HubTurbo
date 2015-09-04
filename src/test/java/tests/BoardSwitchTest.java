package tests;

import static org.junit.Assert.*;
import prefs.PanelInfo;
import prefs.Preferences;

import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

public class BoardSwitchTest {
    
    Preferences testPrefs;

    @Test
    public void boardsSwitchTest() {
        testPrefs = new Preferences(true);
        
        List<PanelInfo> board1 = new ArrayList<PanelInfo>();
        board1.add(new PanelInfo("Panel 1", ""));
        List<PanelInfo> board2 = new ArrayList<PanelInfo>();
        board2.add(new PanelInfo("Panel 1", ""));
        board2.add(new PanelInfo("Panel 2", ""));
        List<PanelInfo> board3 = new ArrayList<PanelInfo>();
        board3.add(new PanelInfo("Panel 1", ""));
        board3.add(new PanelInfo("Panel 2", ""));
        board3.add(new PanelInfo("Panel 3", ""));
        
        testPrefs.addBoard("Board 1", board1);
        testPrefs.addBoard("Board 2", board2);
        testPrefs.addBoard("Board 3", board3);
        
        testPrefs.setLastOpenBoard("Board 1");
        
        switchBoard();
        assertEquals("Board 3", testPrefs.getLastOpenBoard().get());
        
        switchBoard();
        assertEquals("Board 2", testPrefs.getLastOpenBoard().get());
        
        switchBoard();
        assertEquals("Board 1", testPrefs.getLastOpenBoard().get());
        
        switchBoard();
        assertEquals("Board 3", testPrefs.getLastOpenBoard().get());
        
    }
    
    @Test
    public void noBoardSwitchTest() {
        testPrefs = new Preferences(true);
        
        switchBoard();
        assertEquals(false, testPrefs.getLastOpenBoard().isPresent());
    }
    
    @Test
    public void noBoardOpenSwitchTest() {
        testPrefs = new Preferences(true);
        
        List<PanelInfo> board1 = new ArrayList<PanelInfo>();
        board1.add(new PanelInfo("Panel 1", ""));
        List<PanelInfo> board2 = new ArrayList<PanelInfo>();
        board2.add(new PanelInfo("Panel 1", ""));
        board2.add(new PanelInfo("Panel 2", ""));
        List<PanelInfo> board3 = new ArrayList<PanelInfo>();
        board3.add(new PanelInfo("Panel 1", ""));
        board3.add(new PanelInfo("Panel 2", ""));
        board3.add(new PanelInfo("Panel 3", ""));
        
        testPrefs.addBoard("Board 1", board1);
        testPrefs.addBoard("Board 2", board2);
        testPrefs.addBoard("Board 3", board3);
        
        switchBoard();
        assertEquals(false, testPrefs.getLastOpenBoard().isPresent());
    }
    
    @Test
    public void oneBoardSwitchTest() {
        testPrefs = new Preferences(true);
        
        List<PanelInfo> board1 = new ArrayList<PanelInfo>();
        board1.add(new PanelInfo("Panel 1", ""));
        testPrefs.addBoard("Board 1", board1);
        testPrefs.setLastOpenBoard("Board 1");
        
        switchBoard();
        assertEquals("Board 1", testPrefs.getLastOpenBoard().get());
        
    }
    
    public void switchBoard() {
        if (testPrefs.getLastOpenBoard().isPresent() && testPrefs.getAllBoards().size() > 1) {
            List<String> boardNames = new ArrayList<>(testPrefs.getAllBoards().keySet());
            int lastBoard = boardNames.indexOf(testPrefs.getLastOpenBoard().get());
            int index = (lastBoard + 1) % boardNames.size();
            
            testPrefs.setLastOpenBoard(boardNames.get(index));
        }
    }

}
