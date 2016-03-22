package unstable;

import org.junit.Test;

import guitests.UITest;
import javafx.scene.input.KeyCode;
import ui.IdGenerator;
import ui.UI;
import ui.components.FilterTextField;
import util.events.testevents.UpdateDummyRepoEvent;

public class MetadataUpdateTest extends UITest {

    @Test
    public void testUpdatedTriggersMetadata() {
        resetRepo();

        updated24();
        ensureMetadataDownloadIsTriggered();
        ensureMetadataIsReceived();

        awaitCondition(() -> existsQuiet("1 comments since, involving test."));
        awaitCondition(() -> existsQuiet("2 comments since, involving User 1, User 2."));
    }

    private void ensureMetadataDownloadIsTriggered() {
        awaitCondition(() -> existsQuiet("Getting metadata for dummy/dummy..."));
    }

    private void ensureMetadataIsReceived() {
        awaitCondition(() -> existsQuiet("Received metadata from dummy/dummy!"));
    }

    private void updated24() {
        updated24("updated");
    }

    private void updated24(String qualifier) {
        // Select everything in the field
        clickFilterTextFieldAtPanel(0);
        selectAll();

        type(String.format("%s:24", qualifier));
        push(KeyCode.ENTER);
    }

    private void resetRepo() {
        UI.events.triggerEvent(UpdateDummyRepoEvent.resetRepo("dummy/dummy"));
    }
}

