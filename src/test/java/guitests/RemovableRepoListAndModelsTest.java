package guitests;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import org.junit.Test;
import org.loadui.testfx.utils.FXTestUtils;
import prefs.ConfigFileHandler;
import prefs.GlobalConfig;
import prefs.Preferences;
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
        ConfigFileHandler configFileHandler =
                new ConfigFileHandler(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setLastLoginCredentials("test", "test");
        globalConfig.setLastViewedRepository("dummy/dummy");
        configFileHandler.saveGlobalConfig(globalConfig);
    }

    /**
     * This test will test:
     * - UI.getCurrentlyUsedRepos() has correct count
     *   -> especially no duplicate of repo qualifier and default repo
     *      if they are the same
     *   -> this is relevant to remove model because used repo list depends
     *      on this
     * - Logic.removeUnusedModels makes model count correct
     * - Repo > Remove menu has correct no. of items
     *   -> especially when different letter casing for repo references
     *   -> count will be +1 for SeparatorMenuItem
     *   -> SeparatorMenuItem disabledProperty is false
     *     -> Testing enabledMenuItems will be +1, disabled no +1
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
                .filter(menuItem -> menuItem.getText().equalsIgnoreCase("Remove")).findFirst();
        if (!removeRepoMenuOpt.isPresent()) {
            fail();
        }

        Menu removeRepoMenu = (Menu) removeRepoMenuOpt.get();

        int noOfUsedRepo, totalRepoInSystem;


        // check if test json is present
        File testConfig = new File(Preferences.DIRECTORY, Preferences.TEST_CONFIG_FILE);
        if (!(testConfig.exists() && testConfig.isFile())) {
            fail();
        }

        // we check that only 1 repo is in use
        noOfUsedRepo = 1;
        totalRepoInSystem = 1;
        assertNodeExists("#repoOwnerField");
        type("dummy").push(KeyCode.TAB).type("dummy").push(KeyCode.ENTER);
        assertEquals(ui.getCurrentlyUsedRepos().size(), noOfUsedRepo);
        assertEquals(ui.logic.getOpenRepositories().size(), noOfUsedRepo);
        assertEquals(removeRepoMenu.getItems().size(), totalRepoInSystem + 1);
        assertEquals(getNoOfEnabledMenuItems(removeRepoMenu.getItems()),
                totalRepoInSystem + 1 - noOfUsedRepo);
        assertEquals(getNoOfDisabledMenuItems(removeRepoMenu.getItems()), noOfUsedRepo);

        // we check that if there is a panel referencing same repo,
        // it's still 1 repo in use
        noOfUsedRepo = 1;
        totalRepoInSystem = 1;
        Platform.runLater(find("#dummy/dummy_col0_filterTextField")::requestFocus);
        PlatformEx.waitOnFxThread();
        selectAll();
        type("repo:dummY/Dummy");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(ui.getCurrentlyUsedRepos().size(), noOfUsedRepo);
        assertEquals(ui.logic.getOpenRepositories().size(), noOfUsedRepo);
        assertEquals(removeRepoMenu.getItems().size(), totalRepoInSystem + 1);
        assertEquals(getNoOfEnabledMenuItems(removeRepoMenu.getItems()),
                totalRepoInSystem + 1 - noOfUsedRepo);
        assertEquals(getNoOfDisabledMenuItems(removeRepoMenu.getItems()), noOfUsedRepo);

        // we check for panel referencing different repo(s)
        noOfUsedRepo = 2;
        totalRepoInSystem = 2;
        selectAll();
        type("repo:dummy2/dummy2");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(ui.getCurrentlyUsedRepos().size(), noOfUsedRepo);
        assertEquals(ui.logic.getOpenRepositories().size(), noOfUsedRepo);
        assertEquals(removeRepoMenu.getItems().size(), totalRepoInSystem + 1);
        assertEquals(getNoOfEnabledMenuItems(removeRepoMenu.getItems()),
                totalRepoInSystem + 1 - noOfUsedRepo);
        assertEquals(getNoOfDisabledMenuItems(removeRepoMenu.getItems()), noOfUsedRepo);

        noOfUsedRepo = 3;
        totalRepoInSystem = 3;
        selectAll();
        type("(repo:duMMy2/Dummy2 ");
        press(KeyCode.SHIFT).press(KeyCode.BACK_SLASH).release(KeyCode.BACK_SLASH).release(KeyCode.SHIFT);
        type(" repo:dummy3/dummy3)");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(ui.getCurrentlyUsedRepos().size(), noOfUsedRepo);
        assertEquals(ui.logic.getOpenRepositories().size(), noOfUsedRepo);
        assertEquals(removeRepoMenu.getItems().size(), totalRepoInSystem + 1);
        assertEquals(getNoOfEnabledMenuItems(removeRepoMenu.getItems()),
                totalRepoInSystem + 1 - noOfUsedRepo);
        assertEquals(getNoOfDisabledMenuItems(removeRepoMenu.getItems()), noOfUsedRepo);

        noOfUsedRepo = 4;
        totalRepoInSystem = 4;
        pushKeys(CREATE_RIGHT_PANEL);
        waitUntilNodeAppears("#dummy/dummy_col1_filterTextField");
        click("#dummy/dummy_col1_filterTextField");
        selectAll();
        type("repo:dummy4/dummy4");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(ui.getCurrentlyUsedRepos().size(), noOfUsedRepo);
        assertEquals(ui.logic.getOpenRepositories().size(), noOfUsedRepo);
        assertEquals(removeRepoMenu.getItems().size(), totalRepoInSystem + 1);
        assertEquals(getNoOfEnabledMenuItems(removeRepoMenu.getItems()),
                totalRepoInSystem + 1 - noOfUsedRepo);
        assertEquals(getNoOfDisabledMenuItems(removeRepoMenu.getItems()), noOfUsedRepo);

        noOfUsedRepo = 3;
        totalRepoInSystem = 4;
        selectAll();
        type("repo:duMMY/duMMY");
        push(KeyCode.ENTER);
        PlatformEx.waitOnFxThread();
        assertEquals(ui.getCurrentlyUsedRepos().size(), noOfUsedRepo);
        assertEquals(ui.logic.getOpenRepositories().size(), noOfUsedRepo);
        assertEquals(removeRepoMenu.getItems().size(), totalRepoInSystem + 1); // remove would not decrease
        assertEquals(getNoOfEnabledMenuItems(removeRepoMenu.getItems()),
                totalRepoInSystem + 1 - noOfUsedRepo);
        assertEquals(getNoOfDisabledMenuItems(removeRepoMenu.getItems()), noOfUsedRepo);
    }

    public long getNoOfEnabledMenuItems(ObservableList<MenuItem> menuItems) {
        return menuItems.stream().filter(menuItem -> !menuItem.disableProperty().get()).count();
    }

    public long getNoOfDisabledMenuItems(ObservableList<MenuItem> menuItems) {
        return menuItems.stream().filter(menuItem -> menuItem.disableProperty().get()).count();
    }
}
