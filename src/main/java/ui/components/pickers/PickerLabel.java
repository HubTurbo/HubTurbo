package ui.components.pickers;

import backend.resource.TurboLabel;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

public class PickerLabel extends TurboLabel {

    private boolean isSelected;
    private boolean isRemoved;
    private boolean isHighlighted;

    public PickerLabel(TurboLabel label) {
        super(label.getRepoId(), label.getColour(), label.getActualName());
        isSelected = false;
        isRemoved = false;
        isHighlighted = false;
    }

    public PickerLabel(TurboLabel label, boolean isSelected, boolean isRemoved, boolean isHighlighted) {
        super(label.getRepoId(), label.getColour(), label.getActualName());
        this.isSelected = isSelected;
        this.isRemoved = isRemoved;
        this.isHighlighted = isHighlighted;
    }

    @Override
    public Node getNode() {
        javafx.scene.control.Label node = new javafx.scene.control.Label(getName());
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setIsRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setIsHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }

}
