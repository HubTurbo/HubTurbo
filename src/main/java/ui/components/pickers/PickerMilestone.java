package ui.components.pickers;

import backend.resource.TurboMilestone;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class PickerMilestone extends TurboMilestone implements Comparable<PickerMilestone> {
    boolean isSelected = false;
    boolean isHighlighted = false;
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
            milestone.setStyle("-fx-border-color: black;");
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
