package ui;

import javafx.application.Platform;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import ui.components.Notification;
import util.TickingTimer;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class NotificationController {

    private final NotificationPane notificationPane;
    private TickingTimer notificationPaneTimer;
    private Optional<Notification> notification = Optional.empty();

    public NotificationController(NotificationPane notificationPane) {
        this.notificationPane = notificationPane;
    }

    public void showNotification(Notification notification) {
        Platform.runLater(() -> {
            if (notificationPane.isShowing()) {
                hideNotification();
            }
            notificationPane.setGraphic(notification.getIcon());
            notificationPane.setText(notification.getMessage());
            notificationPane.getActions().clear();
            notificationPane.getActions().add(new Action(notification.getButtonLabel(),
                    actionEvent -> {
                        notification.getButtonRunnable().run();
                        hideNotification();
                    }));
            notificationPaneTimer = new TickingTimer("Notification Timer", notification.getTimeoutDuration(),
                    integer -> {}, () -> Platform.runLater(this::triggerTimeoutAction), TimeUnit.SECONDS);
            notificationPane.setOnHiding(event -> triggerTimeoutAction());
            notificationPane.show();
            notificationPaneTimer.start();
            this.notification = Optional.of(notification);
        });
    }

    public void triggerTimeoutAction() {
        // must be run in a Platform.runLater or from the UI thread
        determineAndRunTimeoutAction();
        hideNotification();
    }

    private void determineAndRunTimeoutAction() {
        if (notification.isPresent() &&
            notification.get().getNotificationType() == Notification.NotificationType.ACTIONONBUTTONANDTIMEOUT) {

            notification.get().getTimeoutRunnable().run();
            // other NotifcationTypes can be implemented here if needed
        }
    }

    private void hideNotification() {
        // must be run in a Platform.runLater or from the UI thread
        if (notificationPaneTimer.isStarted()) {
            notificationPaneTimer.stop();
        }
        notificationPane.hide();
        notification = Optional.empty();
    }

    public void triggerNotificationAction() {
        Platform.runLater(() -> {
            if (notificationPane.isShowing()) {
                determineAndRunNotificationAction();
                hideNotification();
            }
        });
    }

    private void determineAndRunNotificationAction() {
        if (notification.isPresent() &&
            notification.get().getNotificationType() == Notification.NotificationType.ACTIONONBUTTONANDTIMEOUT) {

            notification.get().getButtonRunnable().run();
            // other NotifcationTypes can be implemented here if needed
        }
    }

}
