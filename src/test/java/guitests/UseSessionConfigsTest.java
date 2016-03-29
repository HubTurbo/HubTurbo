package guitests;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.eclipse.egit.github.core.RepositoryId;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.PanelInfo;
import prefs.Preferences;
import ui.IdGenerator;
import ui.TestController;
import ui.UI;
import ui.components.FilterTextField;
import ui.components.KeyboardShortcuts;
import ui.issuepanel.FilterPanel;
import ui.issuepanel.PanelControl;
import util.PlatformEx;
import util.events.ShowRenamePanelEvent;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UseSessionConfigsTest extends UITest {
    @Override
    public void launchApp() {
        // isTestMode in UI checks for testconfig too so we don't need to specify --test=true here.
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true");
    }

    @Test
    public void useGlobalConfigTest() {
        // Override setupMethod() if you want to do stuff to the JSON beforehand

        UI ui = TestController.getUI();
        PanelControl panels = ui.getPanelControl();

        selectAll();
        type("dummy");
        pushKeys(KeyCode.TAB);
        type("dummy");
        pushKeys(KeyCode.TAB);
        type("test");
        pushKeys(KeyCode.TAB);
        type("test");
        click("Sign in");
        ComboBox<String> repositorySelector = findOrWaitFor(IdGenerator.getRepositorySelectorIdReference());
        waitForValue(repositorySelector);
        assertEquals("dummy/dummy", repositorySelector.getValue());

        pushKeys(KeyboardShortcuts.MAXIMIZE_WINDOW);

        // Make a new board
        click("Boards");
        click("Save as");

        // Somehow the text field cannot be populated by typing on the CI, use setText instead.
        // TODO find out why
        ((TextField) find(IdGenerator.getBoardNameInputFieldIdReference())).setText("Empty Board");
        click("OK");

        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(0)));
        type("Renamed panel");
        push(KeyCode.ENTER);
        FilterPanel filterPanel0 = (FilterPanel) panels.getPanel(0);
        assertEquals("Renamed panel", filterPanel0.getNameText().getText());

        FilterTextField filterTextField0 = filterPanel0.getFilterTextField();
        waitUntilNodeAppears(filterTextField0);
        Platform.runLater(filterTextField0::requestFocus);
        PlatformEx.waitOnFxThread();
        type("is");
        pushKeys(KeyCode.SHIFT, KeyCode.SEMICOLON);
        type("issue");
        push(KeyCode.ENTER);

        // Load dummy2/dummy2 too
        pushKeys(KeyboardShortcuts.CREATE_RIGHT_PANEL);
        PlatformEx.waitOnFxThread();
        FilterPanel filterPanel1 = (FilterPanel) panels.getPanel(1);
        FilterTextField filterTextField1 = filterPanel1.getFilterTextField();
        waitUntilNodeAppears(filterTextField1);
        Platform.runLater(filterTextField1::requestFocus);
        PlatformEx.waitOnFxThread();
        type("repo");
        pushKeys(KeyCode.SHIFT, KeyCode.SEMICOLON);
        type("dummy2/dummy2");
        pushKeys(KeyCode.ENTER);

        Label renameButton1 = filterPanel1.getRenameButton();
        click(renameButton1);
        type("Dummy 2 panel");
        push(KeyCode.ENTER);
        assertEquals("Dummy 2 panel", filterPanel1.getNameText().getText());

        pushKeys(KeyboardShortcuts.CREATE_LEFT_PANEL);
        PlatformEx.waitOnFxThread();
        sleep(500);
        FilterPanel filterPanel2 = (FilterPanel) panels.getPanel(0);
        FilterTextField filterTextField2 = filterPanel2.getFilterTextField();
        Platform.runLater(filterTextField2::requestFocus);
        PlatformEx.waitOnFxThread();
        type("is");
        pushKeys(KeyCode.SHIFT, KeyCode.SEMICOLON);
        type("open");
        assertEquals("is:open", filterTextField2.getText());
        pushKeys(KeyCode.ENTER);

        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(0)));
        type("Open issues");
        push(KeyCode.ENTER);
        assertEquals("Open issues", filterPanel2.getNameText().getText());


        // Make a new board
        click("Boards");
        click("Save as");

        // Text field cannot be populated by typing on the CI, use setText instead
        ((TextField) find(IdGenerator.getBoardNameInputFieldIdReference())).setText("Dummy Board");
        click("OK");

        // Then exit program...
        click("File");
        click("Quit");

        // ...and check if the test JSON is still there...
        File testConfig = new File(TestController.TEST_DIRECTORY, TestController.TEST_SESSION_CONFIG_FILENAME);
        if (!(testConfig.exists() && testConfig.isFile())) {
            fail();
        }

        // ...then check that the JSON file contents are correct.
        Preferences testPref = TestController.loadTestPreferences();

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
        assertEquals("is:issue", dummyBoard.get(1).getPanelFilter());
        assertEquals("repo:dummy2/dummy2", dummyBoard.get(2).getPanelFilter());
        assertEquals("Open issues", dummyBoard.get(0).getPanelName());
        assertEquals("Renamed panel", dummyBoard.get(1).getPanelName());
        assertEquals("Dummy 2 panel", dummyBoard.get(2).getPanelName());

        // Panels
        List<String> lastOpenFilters =
                testPref.getPanelInfo()
                        .stream()
                        .map(PanelInfo::getPanelFilter)
                        .collect(Collectors.toList());
        List<String> lastOpenPanelNames =
                testPref.getPanelInfo()
                        .stream()
                        .map(PanelInfo::getPanelName)
                        .collect(Collectors.toList());

        assertEquals("is:open", lastOpenFilters.get(0));
        assertEquals("is:issue", lastOpenFilters.get(1));
        assertEquals("repo:dummy2/dummy2", lastOpenFilters.get(2));

        assertEquals("Open issues", lastOpenPanelNames.get(0));
        assertEquals("Renamed panel", lastOpenPanelNames.get(1));
        assertEquals("Dummy 2 panel", lastOpenPanelNames.get(2));
    }
}
