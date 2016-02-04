package ui.components.pickers;

import backend.resource.TurboMilestone;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import util.Utility;

import java.util.Optional;

public class PickerMilestone extends TurboMilestone {
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

        milestone.setOnMouseClicked(e -> dialog.selectMilestone(getTitle()));

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
}
