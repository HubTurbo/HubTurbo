package ui.components.pickers;

import javafx.application.Platform;
import javafx.stage.Stage;
import ui.UI;
import util.events.ShowBoardPickerEventHandler;

import java.util.List;
import java.util.Optional;

/**
 * Represents a picker for user to search and switch to another board.
 */
public class BoardPicker {

    private final UI ui;
    private final Stage stage;

    /**
     * A BoardPicker is created by trigger a ShowBoardPickerEvent.
     *
     * @param ui
     * @param stage
     */
    public BoardPicker(UI ui, Stage stage) {
        this.ui = ui;
        this.stage = stage;
        ui.registerEvent((ShowBoardPickerEventHandler) e -> Platform.runLater(() -> showBoardPicker(e.boards)));
    }

    private void showBoardPicker(List<String> boards) {
        BoardPickerDialog boardPickerDialog = new BoardPickerDialog(boards, stage);
        Optional<String> result = boardPickerDialog.showAndWait();
        stage.show();

        result.ifPresent(res -> ui.getMenuControl().switchBoard(res));
    }

}
