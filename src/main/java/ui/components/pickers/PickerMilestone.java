package ui.components.pickers;

import backend.resource.TurboMilestone;
import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.Node;
import javafx.scene.control.Label;
import util.Utility;

import java.util.Optional;

public class PickerMilestone extends TurboMilestone {
    boolean isSelected = false;
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
        Label label = new Label(getTitle());
        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = fontLoader.computeStringWidth(label.getText(), label.getFont());
        label.setPrefWidth(width + 30);
        label.getStyleClass().add("labels");
        label.setStyle("-fx-border-color: black;");
        label.setStyle("-fx-background-color: yellow;");

        if (isSelected) {
            label.setText(label.getText() + " ✓");
        }

        label.setOnMouseClicked(e -> dialog.selectMilestone(getTitle()));
        return label;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return this.isSelected;
    }
}
