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
        waitUntilNodeAppears(hasText("Test Error Dialog"));
        assertNodeExists(hasText("Test Error Dialog"));
        clickOn("OK");
    }

    @Test
    public void showYesNoWarningDialogTest() throws ExecutionException, InterruptedException {
        FutureTask<Boolean> yesTask = new FutureTask<>(() ->
                DialogMessage.showYesNoWarningDialog("Warning", "Warning Header", "Warning Message", "yEs", "nO"));
        Platform.runLater(yesTask);
        waitUntilNodeAppears(hasText("Warning Header"));
        assertNodeExists(hasText("Warning Header"));
        clickOn("yEs");
        assertEquals(true, yesTask.get());
        FutureTask<Boolean> noTask = new FutureTask<>(() ->
                DialogMessage.showYesNoWarningDialog("Warning", "Warning Header", "Warning Message", "yEs", "nO"));
        Platform.runLater(noTask);
        waitUntilNodeAppears(hasText("Warning Message"));
        assertNodeExists(hasText("Warning Message"));
        clickOn("nO");
        assertEquals(false, noTask.get());
    }

    @Test
    public void showYesNoConfirmationDialog_tryYesAndNo_getCorrectValue()
            throws ExecutionException, InterruptedException {

        FutureTask<Boolean> yesTask = new FutureTask<>(() ->
                DialogMessage.showYesNoConfirmationDialog("Confirm", "Confirm Header", "Confirm Message", "yes", "no"));
        Platform.runLater(yesTask);
        waitUntilNodeAppears(hasText("Confirm Header"));
        assertNodeExists(hasText("Confirm Header"));
        clickOn("yes");
        assertEquals(true, yesTask.get());

        FutureTask<Boolean> noTask = new FutureTask<>(() ->
                DialogMessage.showYesNoWarningDialog("Confirm", "Confirm Header", "Confirm Message",
                                                     "Non-Standard-Yes", "Non-Standard-No"));
        Platform.runLater(noTask);
        waitUntilNodeAppears(hasText("Confirm Message"));
        assertNodeExists(hasText("Confirm Message"));
        clickOn("Non-Standard-No");
        assertEquals(false, noTask.get());
    }

    @Test
    public void showInformationDialogTest() {
        Platform.runLater(() -> DialogMessage.showInformationDialog("Information", "Test Information Dialog"));
        waitUntilNodeAppears(hasText("Test Information Dialog"));
        assertNodeExists(hasText("Test Information Dialog"));
        clickOn("OK");
    }
}
