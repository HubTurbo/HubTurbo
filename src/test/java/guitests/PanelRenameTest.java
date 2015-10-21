package guitests;

import java.util.Random;

import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import org.junit.Test;
import org.loadui.testfx.exceptions.NoNodesFoundException;
import org.loadui.testfx.utils.FXTestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import ui.UI;
import ui.components.PanelNameTextField;
import util.PlatformEx;
import util.events.ShowRenamePanelEvent;

import static org.junit.Assert.assertEquals;
import static ui.components.KeyboardShortcuts.CREATE_RIGHT_PANEL;
import static ui.components.KeyboardShortcuts.MAXIMIZE_WINDOW;

public class PanelRenameTest extends UITest {

    public static final int EVENT_DELAY = 1000;
    public static final int PANEL_MAX_NAME_LENGTH = 36;

    @Override
    public void launchApp() {
        FXTestUtils.launchApp(TestUI.class, "--bypasslogin=true");
    }

    @Test
    public void panelRenameTest() {
        
        Random rand = new Random();
        
        // Test for saving panel name
        
        press(MAXIMIZE_WINDOW);
        sleep(EVENT_DELAY);

        // Testing case where rename is canceled with ESC
        // Expected: change not reflected
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(0)));
        sleep(EVENT_DELAY);
        type("Renamed panel");
        push(KeyCode.ESCAPE);
        Text panelNameText0 = find("#dummy/dummy_col0_nameText");
        assertEquals("Panel", panelNameText0.getText());
        sleep(EVENT_DELAY);
        
        press(CREATE_RIGHT_PANEL);
        
        // Testing case where a name with whitespaces at either end is submitted
        // Expected: new name accepted with whitespaces removed
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(1)));
        sleep(EVENT_DELAY);
        type("   Renamed panel  ");
        push(KeyCode.ENTER);
        Text panelNameText1 = find("#dummy/dummy_col1_nameText");
        assertEquals("Renamed panel", panelNameText1.getText());
        sleep(EVENT_DELAY);

        press(CREATE_RIGHT_PANEL);

        // Testing case where empty name is submitted
        // Expected: new name not accepted
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(2)));
        sleep(EVENT_DELAY);
        push(KeyCode.BACK_SPACE);
        push(KeyCode.ENTER);
        Text panelNameText2 = find("#dummy/dummy_col2_nameText");
        assertEquals("Panel", panelNameText2.getText());
        sleep(EVENT_DELAY);
        
        press(CREATE_RIGHT_PANEL);
        
        // Testing boundary case where a name shorter than maximum allowed length is submitted
        // Expected: new name accepted
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(3)));
        sleep(EVENT_DELAY);
        String randomName3 = RandomStringUtils.randomAlphanumeric(PANEL_MAX_NAME_LENGTH - 1);
        PanelNameTextField renameTextField3 = find("#dummy/dummy_col3_renameTextField");
        renameTextField3.setText(randomName3);
        push(KeyCode.ENTER);
        Text panelNameText3 = find("#dummy/dummy_col3_nameText");
        assertEquals(randomName3, panelNameText3.getText());
        sleep(EVENT_DELAY);
        
        press(CREATE_RIGHT_PANEL);
        
        // Testing boundary case where a name exactly at maximum allowed length is submitted
        // Expected: new name accepted
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(4)));
        sleep(EVENT_DELAY);
        String randomName4 = RandomStringUtils.randomAlphanumeric(PANEL_MAX_NAME_LENGTH);
        PanelNameTextField renameTextField4 = find("#dummy/dummy_col4_renameTextField");
        renameTextField4.setText(randomName4);
        push(KeyCode.ENTER);
        Text panelNameText4 = find("#dummy/dummy_col4_nameText");
        assertEquals(randomName4, panelNameText4.getText());
        sleep(EVENT_DELAY);

        press(CREATE_RIGHT_PANEL);

        // Testing boundary case where a name longer than maximum allowed length is submitted
        // Expected: new name not accepted
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(5)));
        sleep(EVENT_DELAY);
        String randomName5 = RandomStringUtils.randomAlphanumeric(PANEL_MAX_NAME_LENGTH + 1);
        PanelNameTextField renameTextField5 = find("#dummy/dummy_col5_renameTextField");
        renameTextField5.setText(randomName5);
        push(KeyCode.ENTER);
        Text panelNameText5 = find("#dummy/dummy_col5_nameText");
        assertEquals("Panel", panelNameText5.getText());
        sleep(EVENT_DELAY);
        
        press(CREATE_RIGHT_PANEL);
        
        // Testing typing more characters when textfield is full
        // Expected: new name accepted with additional characters not added
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(6)));
        sleep(EVENT_DELAY);
        String randomName6 = RandomStringUtils.randomAlphanumeric(PANEL_MAX_NAME_LENGTH);
        PanelNameTextField renameTextField6 = find("#dummy/dummy_col6_renameTextField");
        renameTextField6.setText(randomName6);
        
        int randomCaret6 = rand.nextInt(PANEL_MAX_NAME_LENGTH - 1);
        renameTextField6.positionCaret(randomCaret6);
        type("characters that will not be added");
        push(KeyCode.ENTER);
        Text panelNameText6 = find("#dummy/dummy_col6_nameText");
        assertEquals(randomName6, panelNameText6.getText());
        sleep(EVENT_DELAY);
        
        press(CREATE_RIGHT_PANEL);
        
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(7)));
        sleep(EVENT_DELAY);
        String randomName7 = RandomStringUtils.randomAlphanumeric(PANEL_MAX_NAME_LENGTH - 4);
        PanelNameTextField renameTextField7 = find("#dummy/dummy_col7_renameTextField");
        renameTextField7.setText(randomName7);
        
        int randomCaret7 = rand.nextInt(randomName7.length());
        renameTextField7.positionCaret(randomCaret7);
        // Random string that will make the name longer than max length
        String randomString = RandomStringUtils.randomAlphanumeric(5);
        type(randomString);
        push(KeyCode.ENTER);
        String expected = (randomName7.substring(0, randomCaret7) + 
                randomString.substring(0, 4) + randomName7.substring(randomCaret7, randomName7.length()));
        Text panelNameText7 = find("#dummy/dummy_col7_nameText");
        assertEquals(expected, panelNameText7.getText());
        sleep(EVENT_DELAY);

        // Testing whether the close button appears once rename box is opened.
        // Expected: Close button should not appear once rename box is opened and while edits are being made.
        //           It should appear once the rename box is closed and the edits are done.
        press(CREATE_RIGHT_PANEL);
        boolean isPresentBeforeEdit = exists("#dummy/dummy_col8_closeButton");
        PlatformEx.runAndWait(() -> UI.events.triggerEvent(new ShowRenamePanelEvent(8)));
        PlatformEx.waitOnFxThread();
        boolean isPresentDuringEdit = true; //stub value, this should change to false.
        try {
            exists("#dummy/dummy_col8_closeButton");
        } catch (NoNodesFoundException e){
            isPresentDuringEdit = false;
        }

        String randomName8 = RandomStringUtils.randomAlphanumeric(PANEL_MAX_NAME_LENGTH - 1);
        PanelNameTextField renameTextField8 = find("#dummy/dummy_col8_renameTextField");
        renameTextField8.setText(randomName8);
        push(KeyCode.ENTER);
        boolean isPresentAfterEdit = exists("#dummy/dummy_col8_closeButton");
        Text panelNameText8 = find("#dummy/dummy_col8_nameText");
        assertEquals(true, isPresentBeforeEdit);
        assertEquals(false, isPresentDuringEdit);
        assertEquals(true, isPresentAfterEdit);
        assertEquals(randomName8, panelNameText8.getText());
        PlatformEx.waitOnFxThread();


        // Testing typing excessive characters when textfield is less than full
        
        // Quitting to update json
        click("File");
        push(KeyCode.DOWN).push(KeyCode.DOWN).push(KeyCode.ENTER);
        sleep(EVENT_DELAY);
    }
}
