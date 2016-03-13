package ui.components.issuepicker;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;

import backend.resource.TurboIssue;
import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * This class is to represent a displayed issue in IssuePickerDialog
 *
 * It contains attributes such as selected, highlighted, removed and faded in order
 * to produce the appropriate styled node through getNode()
 */
public class PickerIssue extends TurboIssue {

    private static final String FADED_OPACITY = "40%";
    private static final String DEFAULT_COLOR = "f48322";

    private boolean isFaded;
    private boolean isRemoved;
    
    public PickerIssue(TurboIssue issue) {
        super(issue);
    }

    public Node getNode() {
        Label label = new Label(String.valueOf(getId()));
        label.getStyleClass().add("labels");
        if (isRemoved) label.getStyleClass().add("labels-removed"); // add strikethrough
        label.setStyle(getStyle());

        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = (double) fontLoader.computeStringWidth(label.getText(), label.getFont());
        label.setPrefWidth(width + 30);
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

        if (isFaded) style+= String.format("-fx-opacity: %s; ", FADED_OPACITY);
        return style;
    }
}
