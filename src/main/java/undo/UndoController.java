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
            action.undo(issue).thenApply(success -> showErrorDialogOnFailure(success, issue, action));
            undoBuffer = Optional.empty();
        }
    }

    /**
     * Adds an action to the undo buffer.
     *
     * @param issue The TurboIssue to be acted on
     * @param action The Action that acts on the above TurboIssue
     */
    public void addAction(TurboIssue issue, Action<TurboIssue> action) {
        undoBuffer = Optional.of(new Pair<>(issue, action));
        action.act(issue).thenApply(success -> showErrorDialogOnFailure(success, issue, action));
        Notification notification = new Notification(createInfoOcticon(),
                "Undo " + action.getDescription() + " for #" + issue.getId() + ": " + issue.getTitle(),
                "Undo", this::undoCallback);
        notificationController.showNotification(notification);
    }

    private boolean showErrorDialogOnFailure(Boolean success, TurboIssue issue, Action action) {
        if (!success) {
            // if not successful, show error dialog
            Platform.runLater(() -> DialogMessage.showErrorDialog(
                    "GitHub Write Error",
                    String.format(
                            "An error occurred while attempting to %s on:\n\n%s\n\n"
                                    + "Please check if you have write permissions to %s.",
                            action.getDescription(), issue, issue.getRepoId()
                    )
            ));
        }
        return success;
    }

    private Label createInfoOcticon() {
        Label label = new Label(OCTICON_INFO);
        label.setPadding(new Insets(0, 0, 5, 0));
        label.getStyleClass().addAll("octicon");
        return label;
    }

}
