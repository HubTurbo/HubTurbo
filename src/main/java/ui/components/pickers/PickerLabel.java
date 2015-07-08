package ui.components.pickers;

import backend.resource.TurboLabel;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

public class PickerLabel extends TurboLabel {

    private boolean isSelected;
    private boolean isHighlighted;

    public PickerLabel(TurboLabel label) {
        super(label.getRepoId(), label.getColour(), label.getActualName());
        isSelected = false;
        isHighlighted = false;
    }

    @Override
    public Node getNode() {
        javafx.scene.control.Label node;
        if (isSelected) {
            node = new javafx.scene.control.Label(getName() + " ✓");
        } else {
            node = new javafx.scene.control.Label(getName());
        }
        node.getStyleClass().add("labels");
        if (isHighlighted) {
            node.setStyle(getStyle() + "-fx-border-color: black;");
        } else {
            node.setStyle(getStyle());
        }
        if (getGroup().isPresent()) {
            Tooltip groupTooltip = new Tooltip(getGroup().get());
            node.setTooltip(groupTooltip);
        }
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
