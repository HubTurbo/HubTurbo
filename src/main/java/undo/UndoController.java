package undo;

import backend.Logic;
import backend.resource.TurboIssue;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.util.Pair;
import ui.NotificationController;
import ui.components.Notification;
import undo.actions.Action;
import undo.actions.ChangeLabelsAction;
import util.DialogMessage;

import java.util.Optional;

public class UndoController {

    private static final String OCTICON_INFO = "\uf059";

    private Logic logic;
    private NotificationController notificationController;
    private Optional<Pair<TurboIssue, Action>> undoBuffer;

    public UndoController(Logic logic, NotificationController notificationController) {
        this.logic = logic;
        this.notificationController = notificationController;
        undoBuffer = Optional.empty();
    }

    public void undoCallback() {
        if (undoBuffer.isPresent()) {
            logic.actOnIssueUI(undoBuffer.get().getKey(), undoBuffer.get().getValue().invert());
            undoBuffer = Optional.empty();
        }
    }

    public void timeoutCallback() {
        if (undoBuffer.isPresent()) {
            logic.actOnIssueRepo(undoBuffer.get().getKey(), undoBuffer.get().getValue()).thenApply(success ->
                    showErrorDialogOnFailure(success, undoBuffer.get().getValue(), undoBuffer.get().getKey()));
            undoBuffer = Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public void addAction(TurboIssue issue, Action action) {
        logic.actOnIssueUI(issue, action);
        if (undoBuffer.isPresent()) {
            Pair<Action, Action> reconciledActions =
                    undoBuffer.get().getValue().reconcile(undoBuffer.get().getValue(), action);
            if (!reconciledActions.getKey().isNoOp()) {
                logic.actOnIssueRepo(undoBuffer.get().getKey(), reconciledActions.getKey()).thenApply(success ->
                        showErrorDialogOnFailure(success, reconciledActions.getKey(), undoBuffer.get().getKey()));
            }
            action = reconciledActions.getValue();
        }
        if (action.isNoOp()) {
            undoBuffer = Optional.empty();
            notificationController.triggerTimeoutAction();
        } else {
            undoBuffer = Optional.of(new Pair<>(issue, action));
            Notification notification = new Notification(createInfoOcticon(),
                    "Undo " + action.getDescription() + " for #" + issue.getId() + ": " + issue.getTitle(),
                    "Undo", this::timeoutCallback, this::undoCallback);
            notificationController.showNotification(notification);
        }
    }

    private boolean showErrorDialogOnFailure(Boolean success, Action action, TurboIssue issue) {
        if (!success) {
            // if not successful, revert ui and show error dialog
            logic.actOnIssueUI(issue, action.invert());
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
