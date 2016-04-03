package undo;

import backend.resource.TurboIssue;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.util.Pair;
import ui.NotificationController;
import ui.components.Notification;
import undo.actions.Action;
import util.DialogMessage;
import util.Utility;

import java.util.Optional;

/**
 * Holds an undo buffer of size 1.
 * Buffer stores the undo action.
 */
public class UndoController {

    private static final String OCTICON_INFO = "\uf059";

    private final NotificationController notificationController;
    private Optional<Pair<TurboIssue, Action<TurboIssue>>> undoBuffer;

    public UndoController(NotificationController notificationController) {
        this.notificationController = notificationController;
        undoBuffer = Optional.empty();
    }

    /**
     * Gets called when an action is undone
     */
    public void undoCallback() {
        if (undoBuffer.isPresent()) {
            TurboIssue issue = undoBuffer.get().getKey();
            Action<TurboIssue> action = undoBuffer.get().getValue();
            action.undo(issue).thenApply(isSuccessful -> handleActionResult(issue, action, isSuccessful, true));
            undoBuffer = Optional.empty();
        }
    }

    /**
     * Adds an action to the undo buffer.
     *
     * @param issue  The TurboIssue to be acted on
     * @param action The Action that acts on the above TurboIssue
     */
    public void addAction(TurboIssue issue, Action<TurboIssue> action) {
        undoBuffer = Optional.of(new Pair<>(issue, action));
        action.act(issue)
                .thenApply(success -> handleActionResult(issue, action, success, false))
                .exceptionally((e) -> handleActionException(issue, action, e));
    }

    /**
     * Possibly shows a notification or an error dialog depending on {@code success} and {@code isUndo}.
     *
     * @param issue the TurboIssue acted on
     * @param action the Action that acted on the issue
     * @param success whether the action was successful
     * @param isUndo whether action is an undo
     * @return {@code success}
     */
    private boolean handleActionResult(TurboIssue issue, Action<TurboIssue> action, Boolean success,
                                       boolean isUndo) {
        if (!success) {
            String errorMessage = "Please check if you have write permissions to " + issue.getRepoId();
            showErrorDialog(issue, action, errorMessage);
            return success;
        }
        if (!isUndo) {
            showNotification(issue.getId(), issue.getTitle(), action.getDescription());
            return success;
        }
        return success;
    }

    /**
     * Shows an error dialog for an {@code action} that has been been carried out with an {@code issue} but
     * encountered an {@code Exception}. The error dialog shows the message obtained from calling {@code} getMessage
     * on the exception and removing the Exception Class from the message.
     * @param issue
     * @param action
     * @param exception
     * @return
     */
    private boolean handleActionException(TurboIssue issue, Action<TurboIssue> action, Throwable exception) {
        showErrorDialog(issue, action, Utility.removeFirstWord(exception.getMessage()));
        return false;
    }

    /**
     * Shows a notification with a summary of the completed action.
     * The notification contains a runnable which reverts the last action done.
     * @param issueId
     * @param issueTitle
     * @param actionDescription
     */
    private void showNotification(int issueId, String issueTitle, String actionDescription) {
        Notification notification = new Notification(createInfoOcticon(),
                actionDescription + " for #" + issueId + ": " + issueTitle,
                "Undo", this::undoCallback);
        notificationController.showNotification(notification);
    }

    /**
     * Shows an error dialog with a summary of the attempted action
     * @param issue
     * @param action
     * @param errorMessage
     */
    private void showErrorDialog(TurboIssue issue, Action action, String errorMessage) {
        Platform.runLater(() -> DialogMessage.showErrorDialog(
                "Error Requesting GitHub",
                String.format("An error occurred while attempting to %s on:\n\n%s\n\n" + errorMessage,
                              action.getDescription(), issue)
        ));
    }

    private Label createInfoOcticon() {
        Label label = new Label(OCTICON_INFO);
        label.setPadding(new Insets(0, 0, 5, 0));
        label.getStyleClass().addAll("octicon");
        return label;
    }

}
