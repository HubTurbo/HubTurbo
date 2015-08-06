package util.events;

public class ShowErrorDialogEvent extends Event {
    public final String header;
    public final String message; // Epoch milliseconds

    public ShowErrorDialogEvent(String header, String message) {
        this.header = header;
        this.message = message;
    }
}
