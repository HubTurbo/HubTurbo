package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboMilestone;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class MilestonePickerDialog extends Dialog<Integer> {
    private final List<PickerMilestone> milestones = new ArrayList<>();
    VBox milestoneBox;

    MilestonePickerDialog(Stage stage, TurboIssue issue, List<TurboMilestone> milestones) {
        initOwner(stage);
        setTitle("Edit Milestone");
        setupButtons();
        convertToPickerMilestones(issue, milestones);
        refreshUI();


        getDialogPane().setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.DOWN) {
                highlightNextMilestone(this.milestones);
            }
            if (event.getCode() == KeyCode.UP) {
                highlightPreviousMilestone(this.milestones);
            }
            refreshUI();
        });
    }

    private void highlightNextMilestone(List<PickerMilestone> milestones) {
        PickerMilestone curMilestone = milestones.stream()
                .filter(milestone -> milestone.isHighlighted())
                .findAny()
                .get();
        int curMilestoneIndex = milestones.indexOf(curMilestone);
        if (curMilestoneIndex < milestones.size() - 1) {
            PickerMilestone nextMilestone = milestones.get(curMilestoneIndex + 1);
            nextMilestone.setHighlighted(true);
            curMilestone.setHighlighted(false);
        }
    }

    private void highlightPreviousMilestone(List<PickerMilestone> milestones) {
        PickerMilestone curMilestone = milestones.stream()
                .filter(milestone -> milestone.isHighlighted())
                .findAny()
                .get();
        int curMilestoneIndex = milestones.indexOf(curMilestone);
        if (curMilestoneIndex > 0) {
            PickerMilestone nextMilestone = milestones.get(curMilestoneIndex - 1);
            nextMilestone.setHighlighted(true);
            curMilestone.setHighlighted(false);
        }
    }

    private void convertToPickerMilestones(TurboIssue issue, List<TurboMilestone> milestones) {
        for (int i = 0; i < milestones.size(); i++) {
            this.milestones.add(new PickerMilestone(milestones.get(i), this));
        }

        this.milestones.stream()
                .filter(milestone -> {
                    if (issue.getMilestone().isPresent()) {
                        return issue.getMilestone().get() == milestone.getId();
                    } else {
                        return false;
                    }
                })
                .forEach(milestone -> {
                    milestone.setSelected(true);
                });

        if (this.milestones.stream()
                .filter(milestone -> milestone.isHighlighted())
                .findAny()
                .isPresent()) {
            this.milestones.stream()
                    .filter(milestone -> milestone.isSelected())
                    .findAny()
                    .get()
                    .setHighlighted(true);
        } else {
            if (!this.milestones.isEmpty()) {
                this.milestones.get(0).setHighlighted(true);
            }
        }
    }

    private void setupButtons() {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        setResultConverter((dialogButton) -> {
           if (dialogButton == confirmButtonType && this.milestones.stream()
                    .filter(milestone -> milestone.isSelected())
                    .count() > 0) {
               return this.milestones.stream()
                       .filter(milestone -> milestone.isSelected())
                       .map(milestone -> milestone.getId())
                       .findFirst()
                       .get();
           } else {
               return null;
           }
        });
    }

    private void refreshUI() {
        milestoneBox = new VBox();

        FlowPane openMilestones = createMilestoneGroup();
        populateOpenMilestone(milestones, openMilestones);

        milestoneBox.getChildren().add(new Label("Open Milestones"));
        milestoneBox.getChildren().add(openMilestones);

        FlowPane closedMilestones = createMilestoneGroup();
        populateClosedMilestones(milestones, closedMilestones);

        milestoneBox.getChildren().add(new Label("Closed Milestones"));
        milestoneBox.getChildren().add(closedMilestones);

        getDialogPane().setContent(milestoneBox);
    }

    private void populateClosedMilestones(List<PickerMilestone> pickerMilestoneList, FlowPane closedMilestones) {
        pickerMilestoneList.stream()
                .filter(milestone -> !milestone.isOpen())
                .forEach(milestone -> closedMilestones.getChildren().add(milestone.getNode()));
    }

    private void populateOpenMilestone(List<PickerMilestone> pickerMilestoneList, FlowPane openMilestones) {
        pickerMilestoneList.stream()
                .filter(milestone -> milestone.isOpen())
                .forEach(milestone -> openMilestones.getChildren().add(milestone.getNode()));
    }

    private FlowPane createMilestoneGroup() {
        FlowPane milestoneGroup = new FlowPane();
        milestoneGroup.setPadding(new Insets(3));
        milestoneGroup.setHgap(3);
        milestoneGroup.setStyle("-fx-border-radius: 3;-fx-background-color: white;-fx-border-color: black;");
        return milestoneGroup;
    }

    public void selectMilestone(String milestoneName) {
        milestones.stream()
                .forEach(milestone -> {
                    milestone.setSelected(milestone.getTitle().equals(milestoneName)
                            && !milestone.isSelected());
                });
        refreshUI();
    }
}