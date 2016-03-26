package tests;

import backend.resource.TurboIssue;
import javafx.embed.swing.JFXPanel;
import org.junit.Before;
import org.junit.Test;
import ui.NotificationController;
import ui.components.Notification;
import undo.UndoController;
import undo.actions.Action;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

public class NotificationTests {
    NotificationController notificationController;
    UndoController undoController;
    Action<TurboIssue> action;

    @Before
    public void initComponents() {
        // UndoController's showNotification constructs a Notification object, which fails
        // if JavaFX Runtime is not started. Instantiating a JFXPanel resolves this.
        new JFXPanel();

        action = mock(Action.class);
        notificationController = mock(NotificationController.class);
        undoController = new UndoController(notificationController);
    }

    @Test
    public void notificationController_actionSuccess_showNotification() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        result.complete(true);

        TurboIssue issue = mock(TurboIssue.class);
        doReturn(result).when(action).act(any(TurboIssue.class));

        undoController.addAction(issue, action);

        verify(notificationController, times(1)).showNotification(any(Notification.class));
    }

    @Test
    public void notificationController_actionFailed_noNotification() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        result.complete(false);

        TurboIssue issue = mock(TurboIssue.class);
        doReturn(result).when(action).act(any(TurboIssue.class));

        undoController.addAction(issue, action);

        verify(notificationController, times(0)).showNotification(any(Notification.class));
    }
}
