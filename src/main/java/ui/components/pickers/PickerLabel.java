package ui.components.pickers;

import backend.resource.TurboLabel;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

// for use with LabelPickerDialog
public class PickerLabel extends TurboLabel {

    private LabelPickerDialog labelPickerDialog;
    private boolean isSelected;
    private boolean isHighlighted;

    public PickerLabel(TurboLabel label, LabelPickerDialog labelPickerDialog) {
        super(label.getRepoId(), label.getColour(), label.getActualName());
        this.labelPickerDialog = labelPickerDialog;
        isSelected = false;
        isHighlighted = false;
    }

    @Override
    public Node getNode() {
        javafx.scene.control.Label node;
        if (isSelected) { // add selection tick
            node = new javafx.scene.control.Label(getName() + " ✓");
        } else {
            node = new javafx.scene.control.Label(getName());
        }
        node.getStyleClass().add("labels");
        if (isHighlighted) { // add highlight border
            node.setStyle(getStyle() + "-fx-border-color: black;");
        } else {
            node.setStyle(getStyle());
        }
        if (getGroup().isPresent()) {
            Tooltip groupTooltip = new Tooltip(getGroup().get());
            node.setTooltip(groupTooltip);
        }
        node.setOnMouseClicked(e -> labelPickerDialog.toggleLabel(getActualName()));
        return node;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setIsHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }

}
