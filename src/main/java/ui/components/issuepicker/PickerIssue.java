package ui.components.issuepicker;

import backend.resource.TurboIssue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

/**
 * This class is to represent a displayed issue in IssuePickerDialog
 *
 * It contains attributes such as selected, highlighted, removed and faded in order
 * to produce the appropriate styled node through getNode()
 */
public class PickerIssue extends TurboIssue {

    private static final String LABEL_TITLE = "%d %s";
    private static final String FADED_OPACITY = "40%";
    private static final String DEFAULT_COLOR = "f48322";

    private final String displayTitle;
    private boolean isFaded;
    private boolean isRemoved;
    
    public PickerIssue(TurboIssue issue) {
        super(issue);
        displayTitle = String.format(LABEL_TITLE, getId(), getTitle());
    }

    public Node getNode() {
        Label label = new Label(displayTitle);
        label.getStyleClass().add("labels");
        if (isRemoved) label.getStyleClass().add("labels-removed"); // add strikethrough
        label.setStyle(getStyle());
        label.setMaxWidth(200);
        label.setTooltip(new Tooltip(displayTitle));
        return label;
    }

    public PickerIssue removed(boolean isRemoved) {
        this.isRemoved = isRemoved;
        return this;
    }

    public PickerIssue faded(boolean isFaded) {
        this.isFaded = isFaded;
        return this;
    }
    
    private String getStyle() {
        String style =  String.format("-fx-background-color: #%s; ", DEFAULT_COLOR);

        if (isFaded) style += String.format("-fx-opacity: %s; ", FADED_OPACITY);
        return style;
    }

    /**
     * This isn't unnecessary as fields are added, but are not taken into account for equality.
     * @return
     */
    @Override
    @SuppressWarnings("PMD")
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * This isn't unnecessary as fields are added, but are not taken into account for equality.
     * @return
     */
    @Override
    @SuppressWarnings("PMD")
    public int hashCode() {
        return super.hashCode();
    }
}
