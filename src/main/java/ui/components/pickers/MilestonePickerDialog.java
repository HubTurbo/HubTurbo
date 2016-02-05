package ui.components.pickers;

import backend.resource.TurboIssue;
import backend.resource.TurboMilestone;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.spreadsheet.Picker;

import java.util.ArrayList;
import java.util.List;

public class MilestonePickerDialog extends Dialog<Integer> {
    public static final String DIALOG_TITLE = "Select Milestone";
    private final List<PickerMilestone> milestones = new ArrayList<>();
    private VBox milestoneBox;

    /**
     * Constructor to create a MilestonePickerDialog
     *
     * The issue and the milestones list provided should come from the same repository
     * @param stage
     * @param issue
     * @param milestones
     */
    public MilestonePickerDialog(Stage stage, TurboIssue issue, List<TurboMilestone> milestones) {
        initOwner(stage);
        setTitle(DIALOG_TITLE);
        setupButtons(getDialogPane());
        convertToPickerMilestones(issue, milestones);
        refreshUI();
        setupKeyEvents(getDialogPane());
    }

    private void setupKeyEvents(Node milestoneDialogPane) {
        milestoneDialogPane.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.RIGHT) {
                highlightNextMilestone(this.milestones);
                event.consume();
            }
            if (event.getCode() == KeyCode.LEFT) {
                highlightPreviousMilestone(this.milestones);
                event.consume();
            }
            if (event.getCode() == KeyCode.DOWN) {
                selectMilestone(this.milestones);
            }
            if (event.getCode() == KeyCode.UP) {
                unselectMilestone(this.milestones);
            }
            refreshUI();
        });
    }

    private void selectMilestone(List<PickerMilestone> milestones) {
        if (!hasHighlightedMilestone(milestones)) return;

        PickerMilestone highlightedMilestone = getHighlightedMilestone(milestones);

        milestones.stream()
                .forEach(milestone -> {
                    milestone.setSelected(milestone == highlightedMilestone);
                });
    }

    private boolean hasHighlightedMilestone(List<PickerMilestone> milestones) {
        return milestones.stream()
                .filter(milestone -> milestone.isHighlighted())
                .findAny()
                .isPresent();
    }

    private void unselectMilestone(List<PickerMilestone> milestones) {
        if (!hasHighlightedMilestone(milestones)) return;

        PickerMilestone highlightedMilestone = getHighlightedMilestone(milestones);

        milestones.stream()
                .forEach(milestone -> {
                    milestone.setSelected((milestone == highlightedMilestone) ? false : milestone.isSelected());
                });
    }

    private PickerMilestone getHighlightedMilestone(List<PickerMilestone> milestones) {
        return milestones.stream()
                .filter(milestone -> milestone.isHighlighted())
                .findAny()
                .get();
    }

    private void highlightNextMilestone(List<PickerMilestone> milestones) {
        PickerMilestone curMilestone = getHighlightedMilestone(milestones);
        int curMilestoneIndex = milestones.indexOf(curMilestone);
        if (curMilestoneIndex < milestones.size() - 1) {
            PickerMilestone nextMilestone = milestones.get(curMilestoneIndex + 1);
            nextMilestone.setHighlighted(true);
            curMilestone.setHighlighted(false);
        }
    }

    private void highlightPreviousMilestone(List<PickerMilestone> milestones) {
        PickerMilestone curMilestone = getHighlightedMilestone(milestones);
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

        selectAssignedMilestone(issue);

        if (hasSelectedMilestone()) {
            highlightSelectedMilestone();
        } else {
            highlightFirstMilestone();
        }
    }

    private void highlightFirstMilestone() {
        if (!this.milestones.isEmpty()) {
            this.milestones.get(0).setHighlighted(true);
        }
    }

    private void highlightSelectedMilestone() {
        this.milestones.stream()
                .filter(milestone -> milestone.isSelected())
                .findAny()
                .get()
                .setHighlighted(true);
    }

    private boolean hasSelectedMilestone() {
        return this.milestones.stream()
                .filter(milestone -> milestone.isSelected())
                .findAny()
                .isPresent();
    }

    private void selectAssignedMilestone(TurboIssue issue) {
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
    }

    private void setupButtons(DialogPane milestonePickerDialogPane) {
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        setConfirmResultConverter(confirmButtonType);

        milestonePickerDialogPane.getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
    }

    private void setConfirmResultConverter(ButtonType confirmButtonType) {
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

    /**
     * Finds the PickerMilestone in the milestones list which has milestoneName as title,
     * then toggles the selection status
     * @param milestoneName
     */
    public void toggleMilestone(String milestoneName) {
        this.milestones.stream()
                .forEach(milestone -> {
                    milestone.setSelected(milestone.getTitle().equals(milestoneName)
                            && !milestone.isSelected());
                });
        refreshUI();
    }
}
