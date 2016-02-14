package ui.components.pickers;

import backend.resource.TurboMilestone;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class PickerMilestone extends TurboMilestone implements Comparable<PickerMilestone> {
    boolean isSelected = false;
    boolean isHighlighted = false;
    boolean isFaded = false;
    boolean isExisting = false;
    MilestonePickerDialog dialog;

    PickerMilestone(TurboMilestone milestone, MilestonePickerDialog dialog) {
        super(milestone.getRepoId(), milestone.getId(), milestone.getTitle());
        setDueDate(milestone.getDueDate());
        setDescription(milestone.getDescription() == null ? "" : milestone.getDescription());
        setOpen(milestone.isOpen());
        setOpenIssues(milestone.getOpenIssues());
        setClosedIssues(milestone.getClosedIssues());
        this.dialog = dialog;
    }

    PickerMilestone(PickerMilestone milestone, MilestonePickerDialog dialog) {
        super(milestone.getRepoId(), milestone.getId(), milestone.getTitle());
        setDueDate(milestone.getDueDate());
        setDescription(milestone.getDescription() == null ? "" : milestone.getDescription());
        setOpen(milestone.isOpen());
        setOpenIssues(milestone.getOpenIssues());
        setClosedIssues(milestone.getClosedIssues());
        setFaded(milestone.isFaded());
        setHighlighted(milestone.isHighlighted());
        setSelected(milestone.isSelected());
        setExisting(milestone.isExisting());
        this.dialog = dialog;
    }

    public Node getNode() {
        Label milestone = new Label(getTitle());
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(milestone.getText(), milestone.getFont());
        milestone.setPrefWidth(width + 30);
        milestone.getStyleClass().add("labels");
        milestone.setStyle("-fx-background-color: yellow;");

        if (isSelected) {
            milestone.setText(milestone.getText() + " ✓");
        }

        if (isHighlighted) {
            milestone.setStyle(milestone.getStyle() + "-fx-border-color: black;");
        }

        if (isFaded) {
            milestone.setStyle(milestone.getStyle() + "-fx-opacity: 40%;");
        }

        milestone.setOnMouseClicked(e -> dialog.toggleMilestone(getTitle()));

        return milestone;
    }

    public Node getNewlyAssignedMilestoneNode(boolean hasSuggestion) {
        Label milestone = new Label(getTitle());
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(milestone.getText(), milestone.getFont());
        milestone.setPrefWidth(width + 30);
        milestone.getStyleClass().add("labels");
        milestone.setStyle("-fx-background-color: yellow;");

        if (hasSuggestion) {
            milestone.setStyle(milestone.getStyle() + "-fx-opacity: 40%;");
            if (isSelected) {
                milestone.getStyleClass().add("labels-removed"); // add strikethrough
            }
        }

        if (isSelected) {
            milestone.setStyle(milestone.getStyle() + "-fx-border-color: black;");
        }

        milestone.setOnMouseClicked(e -> dialog.toggleMilestone(getTitle()));

        return milestone;
    }

    public Node getExistingMilestoneNode(boolean hasSuggestion) {
        Label milestone = new Label(getTitle());
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(milestone.getText(), milestone.getFont());
        milestone.setPrefWidth(width + 30);
        milestone.getStyleClass().add("labels");
        milestone.setStyle("-fx-background-color: yellow;");

        if (isSelected && (hasSuggestion || isHighlighted)) {
            milestone.setStyle(milestone.getStyle() + "-fx-opacity: 40%;");
        }

        if (!isExisting || !isSelected || hasSuggestion || isHighlighted) {
            milestone.getStyleClass().add("labels-removed"); // add strikethrough
        }

        milestone.setOnMouseClicked(e -> dialog.toggleMilestone(getTitle()));

        return milestone;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }

    public boolean isHighlighted() {
        return this.isHighlighted;
    }

    public void setFaded(boolean isFaded) {
        this.isFaded = isFaded;
    }

    public boolean isFaded() {
        return this.isFaded;
    }
    public void setExisting(boolean isExisting) {
        this.isExisting = isExisting;
    }

    public boolean isExisting() {
        return this.isExisting;
    }

    /**
     * This treats null milestones with no set due date to be "larger than"
     * those which have due dates
     *
     * @param milestone
     * @return
     */
    @Override
    public int compareTo(PickerMilestone milestone) {
        if (this.getDueDate().equals(milestone)) return 0;
        if (!this.getDueDate().isPresent()) return 1;
        if (!milestone.getDueDate().isPresent()) return -1;

        return this.getDueDate().get()
                .isAfter(milestone.getDueDate().get()) ? 1 : -1;
    }
}
