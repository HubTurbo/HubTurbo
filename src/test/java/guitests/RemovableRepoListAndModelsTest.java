package guitests;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.Preferences;
import ui.IdGenerator;
import ui.TestController;
import ui.UI;
import util.PlatformEx;

import java.io.File;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static ui.components.KeyboardShortcuts.CREATE_RIGHT_PANEL;

public class RemovableRepoListAndModelsTest extends UITest {

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--testconfig=true");
    }

    @Override
    public void beforeStageStarts() {
        // setup test json with last viewed repo "dummy/dummy"
        // obviously the json for that repo doesn't exist
        Preferences prefs = TestController.createTestPreferences();
        prefs.setLastLoginCredentials("test", "test");
        prefs.setLastViewedRepository("dummy/dummy");
    }

    /**
     * This test will test:
     * - UI.getCurrentlyUsedRepos() has correct count
     * -> especially no duplicate of repo qualifier and default repo
     * if they are the same
     * -> this is relevant to remove model because used repo list depends
     * on this
     * - Logic.removeUnusedModels makes model count correct
     * - Repo > Remove menu has correct no. of items
     * -> especially when different letter casing for repo references
     * -> count will be +1 for SeparatorMenuItem
     * -> SeparatorMenuItem disabledProperty is false
     * -> Testing enabledMenuItems will be +1, disabled no +1
     */
    @Test
    public void repoRemoveListAndModel() {
        UI ui = TestController.getUI();
        Optional<Menu> reposMenuOpt = ui.getMenuControl().getMenus().stream()
                .filter(menu -> menu.getText().equalsIgnoreCase("Repos")).findFirst();
        if (!reposMenuOpt.isPresent()) {
            fail();
        }
        Optional<MenuItem> removeRepoMenuOpt = reposMenuOpt.get().getItems().stream()
                .filter(menuItem -> menuItem.getText().equalsIgnoreCase
                        ("Remove")).findFirst();
        if (!removeRepoMenuOpt.isPresent()) {
            fail();
        }

        Menu removeRepoMenu = (Menu) removeRepoMenuOpt.get();

        int noOfUsedRepo, totalRepoInSystem;


        // check if test json is present
        File testConfig = new File(TestController.TEST_DIRECTORY, TestController.TEST_SESSION_CONFIG_FILENAME);
        if (!(testConfig.exists() && testConfig.isFile())) {
            fail();
        }

        // we check that only 1 repo is in use
        noOfUsedRepo = 1;
        totalRepoInSystem = 1;
        assertNodeExists(IdGenerator.getLoginDialogOwnerFieldIdReference());
        type("dummy").push(KeyCode.TAB).type("dummy").push(KeyCode.ENTER);
        assertEquals(noOfUsedRepo, ui.getCurrentlyUsedRepos().size());
        assertEquals(noOfUsedRepo, ui.logic.getOpenRepositories().size());
        assertEquals(totalRepoInSystem + 1, removeRepoMenu.getItems().size());
        assertEquals(totalRepoInSystem + 1 - noOfUsedRepo,
                     getNoOfEnabledMenuItems(removeRepoMenu.getItems()));
        assertEquals(noOfUsedRepo, getNoOfDisabledMenuItems(removeRepoMenu.getItems()));

        // we check that if there is a panel referencing same repo,
        // it's still 1 repo in use
        noOfUsedRepo = 1;
        totalRepoInSystem = 1;
        Platform.runLater(getFilterTextFieldAtPanel(0)::requestFocus);
        PlatformEx.waitOnFxThread();
        selectAll();
        type("repo:dummY/Dummy");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(noOfUsedRepo, ui.getCurrentlyUsedRepos().size());
        assertEquals(noOfUsedRepo, ui.logic.getOpenRepositories().size());
        assertEquals(totalRepoInSystem + 1, removeRepoMenu.getItems().size());
        assertEquals(totalRepoInSystem + 1 - noOfUsedRepo,
                     getNoOfEnabledMenuItems(removeRepoMenu.getItems()));
        assertEquals(noOfUsedRepo, getNoOfDisabledMenuItems(removeRepoMenu.getItems()));

        // we check for panel referencing different repo(s)
        noOfUsedRepo = 2;
        totalRepoInSystem = 2;
        selectAll();
        type("repo:dummy2/dummy2");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(noOfUsedRepo, ui.getCurrentlyUsedRepos().size());
        assertEquals(noOfUsedRepo, ui.logic.getOpenRepositories().size());
        assertEquals(totalRepoInSystem + 1, removeRepoMenu.getItems().size());
        assertEquals(totalRepoInSystem + 1 - noOfUsedRepo,
                     getNoOfEnabledMenuItems(removeRepoMenu.getItems()));
        assertEquals(noOfUsedRepo, getNoOfDisabledMenuItems(removeRepoMenu.getItems()));

        noOfUsedRepo = 3;
        totalRepoInSystem = 3;
        selectAll();
        type("(repo:duMMy2/Dummy2 ");
        press(KeyCode.SHIFT).press(KeyCode.BACK_SLASH).release(KeyCode.BACK_SLASH).release(KeyCode.SHIFT);
        type(" repo:dummy3/dummy3)");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(noOfUsedRepo, ui.getCurrentlyUsedRepos().size());
        assertEquals(noOfUsedRepo, ui.logic.getOpenRepositories().size());
        assertEquals(totalRepoInSystem + 1, removeRepoMenu.getItems().size());
        assertEquals(totalRepoInSystem + 1 - noOfUsedRepo,
                     getNoOfEnabledMenuItems(removeRepoMenu.getItems()));
        assertEquals(noOfUsedRepo, getNoOfDisabledMenuItems(removeRepoMenu.getItems()));

        noOfUsedRepo = 4;
        totalRepoInSystem = 4;
        pushKeys(CREATE_RIGHT_PANEL);
        waitUntilNodeAppears(getFilterTextFieldAtPanel(1));
        clickFilterTextFieldAtPanel(1);
        selectAll();
        type("repo:dummy4/dummy4");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(noOfUsedRepo, ui.getCurrentlyUsedRepos().size());
        assertEquals(noOfUsedRepo, ui.logic.getOpenRepositories().size());
        assertEquals(totalRepoInSystem + 1, removeRepoMenu.getItems().size());
        assertEquals(totalRepoInSystem + 1 - noOfUsedRepo,
                     getNoOfEnabledMenuItems(removeRepoMenu.getItems()));
        assertEquals(noOfUsedRepo, getNoOfDisabledMenuItems(removeRepoMenu.getItems()));

        noOfUsedRepo = 3;
        totalRepoInSystem = 4;
        selectAll();
        type("repo:duMMY/duMMY");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(noOfUsedRepo, ui.getCurrentlyUsedRepos().size());
        assertEquals(noOfUsedRepo, ui.logic.getOpenRepositories().size());
        assertEquals(totalRepoInSystem + 1, removeRepoMenu.getItems().size()); // remove would not decrease
        assertEquals(totalRepoInSystem + 1 - noOfUsedRepo,
                     getNoOfEnabledMenuItems(removeRepoMenu.getItems()));
        assertEquals(noOfUsedRepo, getNoOfDisabledMenuItems(removeRepoMenu.getItems()));
    }

    public long getNoOfEnabledMenuItems(ObservableList<MenuItem> menuItems) {
        return menuItems.stream().filter(menuItem -> !menuItem.disableProperty().get()).count();
    }

    public long getNoOfDisabledMenuItems(ObservableList<MenuItem> menuItems) {
        return menuItems.stream().filter(menuItem -> menuItem.disableProperty().get()).count();
    }
}
