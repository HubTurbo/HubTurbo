package util.events;

import java.util.List;

public class ShowBoardPickerEvent extends Event {

    public final List<String> boards;

    public ShowBoardPickerEvent(List<String> boards) {
        this.boards = boards;
    }

}
