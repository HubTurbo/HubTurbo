package guitests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.UI;
import ui.components.FilterTextField;
import ui.components.PanelNameTextField;
import util.PlatformEx;
import util.events.ShowRenamePanelEvent;

public class UseGlobalConfigsTest extends UITest {

    @Override
    public void launchApp() {
        // isTestMode in UI checks for testconfig too so we don't need to specify --test=true here.
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true");
    }

    @Test
    public void globalConfigTest() {
        // Override setupMethod() if you want to do stuff to the JSON beforehand
        TextField repoOwnerField = find("#repoOwnerField");
        doubleClick(repoOwnerField);
        doubleClick(repoOwnerField);
        type("dummy").push(KeyCode.TAB);
        type("dummy").push(KeyCode.TAB);
        type("test").push(KeyCode.TAB);
        type("test");
        click("Sign in");
        ComboBox<String> repositorySelector = findOrWaitFor("#repositorySelector");
        waitForValue(repositorySelector);
        assertEquals("dummy/dummy", repositorySelector.getValue());
        
        press(KeyCode.CONTROL).press(KeyCode.X).release(KeyCode.X).release(KeyCode.CONTROL);

        // Make a new board
        click("Boards");
        click("Save as");
        // Somehow the text field cannot be populated by typing on the CI, use setText instead.
        // TODO find out why
        ((TextField) find("#boardnameinput")).setText("Empty Board");
        click("OK");
        
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(0)));
        type("Renamed panel");
        PanelNameTextField renameTextField1 = find("#dummy/dummy_col0_renameTextField");
        assertEquals("Renamed panel", renameTextField1.getText());
        push(KeyCode.ENTER);

        // Load dummy2/dummy2 too
        press(KeyCode.CONTROL).press(KeyCode.P).release(KeyCode.P).release(KeyCode.CONTROL);
        waitUntilNodeAppears("#dummy/dummy_col1_filterTextField");
        click("#dummy/dummy_col1_filterTextField");
        type("repo");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("dummy2/dummy2");
        push(KeyCode.ENTER);

        click("#dummy/dummy_col1_renameButton");
        type("Dummy 2 panel");
        PanelNameTextField renameTextField2 = find("#dummy/dummy_col1_renameTextField");
        assertEquals("Dummy 2 panel", renameTextField2.getText());
        push(KeyCode.ENTER);
        
        // Creating panel to the left
        press(KeyCode.SHIFT).press(KeyCode.CONTROL).press(KeyCode.P);
        release(KeyCode.P).release(KeyCode.CONTROL).release(KeyCode.SHIFT);
        
        FilterTextField filterTextField3 = find("#dummy/dummy_col0_filterTextField");
        click(filterTextField3);
        type("is");
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("open");
        assertEquals("is:open", filterTextField3.getText());
        push(KeyCode.ENTER);

        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(0)));
        type("Open issues");
        PanelNameTextField renameTextField3 = find("#dummy/dummy_col0_renameTextField");
        assertEquals("Open issues", renameTextField3.getText());
        push(KeyCode.ENTER);

        // Make a new board
        click("Boards");
        click("Save as");
        ((TextField) find("#boardnameinput")).setText("Dummy Board");
        click("OK");

        // Then exit program...
        click("File");
        click("Quit");

        // ...and check if the test JSON is still there...
        File testConfig = new File(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        if (!(testConfig.exists() && testConfig.isFile())) fail();

        // ...then check that the JSON file contents are correct.
        Preferences testPref = new Preferences(true);
        
        // Credentials
        assertEquals("test", testPref.getLastLoginUsername());
        assertEquals("test", testPref.getLastLoginPassword());
        
        // Last viewed repository
        RepositoryId lastViewedRepository = testPref.getLastViewedRepository().get();
        assertEquals("dummy/dummy", lastViewedRepository.generateId());
        
        // Boards
        Map<String, List<PanelInfo>> boards = testPref.getAllBoards();
        List<PanelInfo> emptyBoard = boards.get("Empty Board");
        assertEquals(1, emptyBoard.size());
        assertEquals("", emptyBoard.get(0).getPanelFilter());
        assertEquals("Panel", emptyBoard.get(0).getPanelName());
        
        List<PanelInfo> dummyBoard = boards.get("Dummy Board");
        assertEquals(3, dummyBoard.size());
        assertEquals("is:open", dummyBoard.get(0).getPanelFilter());
        assertEquals("", dummyBoard.get(1).getPanelFilter());
        assertEquals("repo:dummy2/dummy2", dummyBoard.get(2).getPanelFilter());
        assertEquals("Open issues", dummyBoard.get(0).getPanelName());
        assertEquals("Renamed panel", dummyBoard.get(1).getPanelName());
        assertEquals("Dummy 2 panel", dummyBoard.get(2).getPanelName());
        
        // Panels
        List<String> lastOpenFilters = testPref.getLastOpenFilters();
        List<String> lastOpenPanelNames = testPref.getPanelNames();
        
        assertEquals("is:open", lastOpenFilters.get(0));
        assertEquals("", lastOpenFilters.get(1));
        assertEquals("repo:dummy2/dummy2", lastOpenFilters.get(2));

        assertEquals("Open issues", lastOpenPanelNames.get(0));
        assertEquals("Renamed panel", lastOpenPanelNames.get(1));
        assertEquals("Dummy 2 panel", lastOpenPanelNames.get(2));
        
    }

}
