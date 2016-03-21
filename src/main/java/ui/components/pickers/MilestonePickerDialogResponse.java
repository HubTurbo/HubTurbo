package ui.components.pickers;

import javafx.scene.control.ButtonType;

import java.util.Optional;

public class MilestonePickerDialogResponse {
    private final ButtonType buttonClicked;
    private final Optional<Integer> milestoneId;

    public MilestonePickerDialogResponse(ButtonType buttonClicked, Optional<Integer> milestoneId) {
        this.buttonClicked = buttonClicked;
        this.milestoneId = milestoneId;
    }

    public ButtonType getButtonClicked() {
        return buttonClicked;
    }

    public Optional<Integer> getMilestoneId() {
        return milestoneId;
    }
}
