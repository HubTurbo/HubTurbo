package util.events;

import java.util.List;

/**
 * Represents an event that is triggered when user press the keyboard shortcut for Board Picker.
 */
public class ShowBoardPickerEvent extends Event {

    public final List<String> boards;

    public ShowBoardPickerEvent(List<String> boards) {
        this.boards = boards;
    }

}
