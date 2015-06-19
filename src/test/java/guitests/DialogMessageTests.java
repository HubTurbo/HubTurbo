package guitests;

import javafx.application.Platform;
import org.junit.Test;
import util.DialogMessage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.assertEquals;
import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

public class DialogMessageTests extends UITest {

    @Test
    public void showErrorDialogTest() {
        Platform.runLater(() -> DialogMessage.showErrorDialog("Error", "Test Error Dialog"));
        sleep(1000);
        assertNodeExists(hasText("Test Error Dialog"));
        click("OK");
    }

    @Test
    public void showYesNoWarningDialogTest() throws ExecutionException, InterruptedException {
        FutureTask<Boolean> yesTask = new FutureTask<>(() ->
                DialogMessage.showYesNoWarningDialog("Warning", "Warning Header", "Warning Message", "yEs", "nO"));
        Platform.runLater(yesTask);
        sleep(1000);
        assertNodeExists(hasText("Warning Header"));
        click("yEs");
        assertEquals(true, yesTask.get());
        FutureTask<Boolean> noTask = new FutureTask<>(() ->
                DialogMessage.showYesNoWarningDialog("Warning", "Warning Header", "Warning Message", "yEs", "nO"));
        Platform.runLater(noTask);
        sleep(1000);
        assertNodeExists(hasText("Warning Message"));
        click("nO");
        assertEquals(false, noTask.get());
    }
}
