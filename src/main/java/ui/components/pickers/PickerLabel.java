package ui.components.pickers;

import backend.resource.TurboLabel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

// for use with LabelPickerDialog
public class PickerLabel extends TurboLabel {

    private LabelPickerUILogic labelPickerUILogic;
    private boolean isSelected;
    private boolean isHighlighted;
    private boolean isRemoved;
    private boolean isFaded;

    public PickerLabel(TurboLabel label, LabelPickerUILogic labelPickerUILogic) {
        super(label.getRepoId(), label.getColour(), label.getActualName());
        this.labelPickerUILogic = labelPickerUILogic;
        isSelected = false;
        isHighlighted = false;
        isRemoved = false;
        isFaded = false;
    }

    public PickerLabel(TurboLabel label, LabelPickerUILogic labelPickerUILogic,
                       boolean isSelected, boolean isHighlighted, boolean isRemoved, boolean isFaded) {
        super(label.getRepoId(), label.getColour(), label.getActualName());
        this.labelPickerUILogic = labelPickerUILogic;
        this.isSelected = isSelected;
        this.isHighlighted = isHighlighted;
        this.isRemoved = isRemoved;
        this.isFaded = isFaded;
    }

    @Override
    public Node getNode() {
        Label label = new Label(getActualName() + (isSelected ? " ✓" : "    ")); // add selection tick
        label.getStyleClass().add("labels");
        if (isRemoved) label.getStyleClass().add("labels-removed"); // add strikethrough
        String style = getStyle() + (isHighlighted ? " -fx-border-color: black;" : ""); // add highlight border
        style += (isFaded ? " -fx-opacity: 40%;" : ""); // change opacity if needed
        label.setStyle(style);

        if (getGroup().isPresent()) {
            Tooltip groupTooltip = new Tooltip(getGroup().get());
            label.setTooltip(groupTooltip);
        }

        label.setOnMouseClicked(e -> labelPickerUILogic.toggleLabel(getActualName()));
        return label;
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

    public void setIsRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
    }

    public boolean isFaded() {
        return isFaded;
    }

    public void setIsFaded(boolean isFaded) {
        this.isFaded = isFaded;
    }
}
