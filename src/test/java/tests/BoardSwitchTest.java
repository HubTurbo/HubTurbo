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
        List<PanelInfo> board2 = new ArrayList<PanelInfo>();
        List<PanelInfo> board3 = new ArrayList<PanelInfo>();
        
        testPrefs.addBoard("Board 1", board1);
        testPrefs.addBoard("Board 2", board2);
        testPrefs.addBoard("Board 3", board3);
        
        testPrefs.setLastOpenBoard("Board 1");
        
        testPrefs.switchBoard();
        assertEquals("Board 3", testPrefs.getLastOpenBoard().get());
        
        testPrefs.switchBoard();
        assertEquals("Board 2", testPrefs.getLastOpenBoard().get());
        
        testPrefs.switchBoard();
        assertEquals("Board 1", testPrefs.getLastOpenBoard().get());
        
        testPrefs.switchBoard();
        assertEquals("Board 3", testPrefs.getLastOpenBoard().get());
        
    }
    
    @Test
    public void noBoardSwitchTest() {
        testPrefs = new Preferences(true);
        
        testPrefs.switchBoard();
        assertEquals(false, testPrefs.getLastOpenBoard().isPresent());
    }
    
    @Test
    public void noBoardOpenSwitchTest() {
        testPrefs = new Preferences(true);
        
        List<PanelInfo> board1 = new ArrayList<PanelInfo>();
        List<PanelInfo> board2 = new ArrayList<PanelInfo>();
        List<PanelInfo> board3 = new ArrayList<PanelInfo>();
        
        testPrefs.addBoard("Board 1", board1);
        testPrefs.addBoard("Board 2", board2);
        testPrefs.addBoard("Board 3", board3);
        
        testPrefs.switchBoard();
        assertEquals(false, testPrefs.getLastOpenBoard().isPresent());
    }
    
    @Test
    public void oneBoardSwitchTest() {
        testPrefs = new Preferences(true);
        
        List<PanelInfo> board1 = new ArrayList<PanelInfo>();
        testPrefs.addBoard("Board 1", board1);
        testPrefs.setLastOpenBoard("Board 1");
        
        testPrefs.switchBoard();
        assertEquals("Board 1", testPrefs.getLastOpenBoard().get());
    }

}
