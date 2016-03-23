package ui.components.pickers;

import javafx.scene.control.ButtonType;

import java.util.Optional;

public class AssigneePickerDialogResponse {
    private final ButtonType buttonClicked;
    private final Optional<String> assigneeLoginName;

    public AssigneePickerDialogResponse(ButtonType buttonClicked, Optional<String> assigneeLoginName) {
        this.buttonClicked = buttonClicked;
        this.assigneeLoginName = assigneeLoginName;
    }

    public ButtonType getButtonClicked() {
        return buttonClicked;
    }

    public Optional<String> getAssigneeLoginName() {
        return assigneeLoginName;
    }
}
