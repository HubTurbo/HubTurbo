package guitests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.loadui.testfx.utils.TestUtils;

import javafx.scene.input.KeyCode;
import ui.UI;
import ui.components.FilterTextField;
import util.PlatformEx;
import util.events.testevents.UpdateDummyRepoEvent;

public class MetadataUpdateTest extends UITest {

    @Test
    public void testUpdatedTriggersMetadata() {
        resetRepo();

        updated24();
        ensureMetadataDownloadIsTriggered();
        ensureMetadataIsReceived();

        TestUtils.awaitCondition(() ->
            findQuiet("1 comments since, involving test.").isPresent());
        assertTrue(findQuiet("2 comments since, involving User 1, User 2.").isPresent());
    }

    private void ensureMetadataDownloadIsTriggered() {
        TestUtils.awaitCondition(() ->
            findQuiet("Getting metadata for dummy/dummy...").isPresent());
    }

    private void ensureMetadataIsReceived() {
        TestUtils.awaitCondition(() ->
            findQuiet("Received metadata from dummy/dummy!").isPresent());
    }

    private void updated24() {
        updated24("updated");
    }

    private void updated24(String qualifier) {
        FilterTextField field = find("#dummy/dummy_col0_filterTextField");

        // Select everything in the field
        doubleClick(field);
        doubleClick(field);

        type(qualifier);
        press(KeyCode.SHIFT).press(KeyCode.SEMICOLON).release(KeyCode.SEMICOLON).release(KeyCode.SHIFT);
        type("24");
        push(KeyCode.ENTER);
    }

    private void resetRepo() {
        UI.events.triggerEvent(UpdateDummyRepoEvent.resetRepo("dummy/dummy"));
        PlatformEx.waitOnFxThread();
    }
}

