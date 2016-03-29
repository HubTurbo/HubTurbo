package ui.components.pickers;

import backend.resource.TurboLabel;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

/**
 * This class is to represent a label in LabelPickerDialog
 * <p>
 * It contains attributes such as selected, highlighted, removed and faded in order
 * to produce the appropriate styled node through getNode()
 */
public class PickerLabel extends TurboLabel {

    private boolean isSelected;
    private boolean isHighlighted;
    private boolean isRemoved;
    private boolean isFaded;
    private final boolean canDisplayFullName;

    public PickerLabel(TurboLabel label, boolean canDisplayFullName) {
        super(label.getRepoId(), label.getColour(), label.getFullName());
        isSelected = false;
        isHighlighted = false;
        isRemoved = false;
        isFaded = false;
        this.canDisplayFullName = canDisplayFullName;
    }

    @Override
    public Node getNode() {
        // actual name for labels at the top, add tick for selected labels
        Label label = new Label((canDisplayFullName ? getFullName() : getShortName()));
        label.getStyleClass().add("labels");
        if (isRemoved) label.getStyleClass().add("labels-removed"); // add strikethrough
        String style = getStyle() + (isHighlighted ? " -fx-border-color: black;" : ""); // add highlight border
        style += (isFaded ? " -fx-opacity: 40%;" : ""); // change opacity if needed
        label.setStyle(style);
        label.setText(label.getText() + (!canDisplayFullName && isSelected ? " ✓" : ""));

        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = (double) fontLoader.computeStringWidth(label.getText(), label.getFont());
        label.setPrefWidth(width + 30);

        if (isInGroup()) {
            Tooltip groupTooltip = new Tooltip(getGroupName());
            label.setTooltip(groupTooltip);
        }
        return label;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public PickerLabel selected(boolean isSelected) {
        this.isSelected = isSelected;
        return this;
    }

    public PickerLabel highlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
        return this;
    }

    public PickerLabel removed(boolean isRemoved) {
        this.isRemoved = isRemoved;
        return this;
    }

    public PickerLabel faded(boolean isFaded) {
        this.isFaded = isFaded;
        return this;
    }

    /**
     * This isn't unnecessary as fields are added, but are not taken into account for equality.
     *
     * @return
     */
    @Override
    @SuppressWarnings("PMD")
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * This isn't unnecessary as fields are added, but are not taken into account for equality.
     *
     * @return
     */
    @Override
    @SuppressWarnings("PMD")
    public int hashCode() {
        return super.hashCode();
    }
}
