package ui.components;

import javafx.scene.Node;

public class Notification {

    public static final int DEFAULT_NOTIFICATION_PANE_VISIBLE_PERIOD = 10;

    public enum NotificationType { NOACTION, ACTIONONBUTTON, ACTIONONTIMEOUT, ACTIONONBUTTONANDTIMEOUT }

    private final NotificationType notificationType;
    private final Node icon;
    private final String message;
    private final String buttonLabel;
    private final Runnable timeoutRunnable;
    private final Runnable buttonRunnable;
    private final int timeoutDuration;

    public Notification
            (Node icon, String message, String buttonLabel, Runnable timeoutRunnable, Runnable buttonRunnable) {
        this.notificationType = NotificationType.ACTIONONBUTTONANDTIMEOUT;
        this.icon = icon;
        this.message = message;
        this.buttonLabel = buttonLabel;
        this.timeoutRunnable = timeoutRunnable;
        this.buttonRunnable = buttonRunnable;
        this.timeoutDuration = DEFAULT_NOTIFICATION_PANE_VISIBLE_PERIOD;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public Node getIcon() {
        return icon;
    }

    public String getMessage() {
        return message;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public Runnable getTimeoutRunnable() {
        return timeoutRunnable;
    }

    public Runnable getButtonRunnable() {
        return buttonRunnable;
    }

    public int getTimeoutDuration() {
        return timeoutDuration;
    }

}
