package ui.components.pickers;

import javafx.application.Platform;
import javafx.stage.Stage;
import ui.UI;
import util.events.ShowBoardPickerEventHandler;

import java.util.List;
import java.util.Optional;

public class BoardPicker {

    private final UI ui;
    private final Stage stage;

    // A BoardPicker is created by trigger a ShowBoardPickerEvent.
    public BoardPicker(UI ui, Stage stage) {
        this.ui = ui;
        this.stage = stage;
        ui.registerEvent((ShowBoardPickerEventHandler) e -> Platform.runLater(() -> showBoardPicker(e.boards)));
    }

    // TODO implement multiple dialogs, currently, only one dialog is allowed and it blocks the main UI when open

    private void showBoardPicker(List<String> boards) {
        // create new BoardPickerDialog
        BoardPickerDialog boardPickerDialog = new BoardPickerDialog(boards, stage);
        // show BoardPickerDialog and wait for result
        Optional<String> result = boardPickerDialog.showAndWait();
        stage.show(); // ensures stage is showing after label picker is closed (mostly for tests)
        // if result is present (user did not cancel) then replace issue labels with result

        result.ifPresent(res -> {
            ui.getMenuControl().switchBoard(res);
        });
    }

}
