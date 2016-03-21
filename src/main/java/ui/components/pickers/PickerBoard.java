package ui.components.pickers;

import backend.resource.TurboLabel;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

/**
 * This class is to represent a label in LabelPickerDialog
 *
 * It contains attributes such as selected, highlighted, removed and faded in order
 * to produce the appropriate styled node through getNode()
 */
public class PickerBoard {

    private String boardName;
    private boolean isHighlighted;
    private boolean isFaded;

    public PickerBoard(String boardName) {
        this.boardName = boardName;
        isHighlighted = false;
        isFaded = false;
    }

    public String getStyle() {
        String colour = "b1cfeb";
        int r = Integer.parseInt(colour.substring(0, 2), 16);
        int g = Integer.parseInt(colour.substring(2, 4), 16);
        int b = Integer.parseInt(colour.substring(4, 6), 16);
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        boolean bright = luminance > 128;
        return "-fx-background-color: #" + colour + "; -fx-text-fill: " + (bright ? "black;" : "white;");
    }

    public Node getNode() {
        // actual name for labels at the top, add tick for selected labels
        Label label = new Label(boardName);
        label.getStyleClass().add("labels");
        String style = getStyle() + (isHighlighted ? " -fx-border-color: black; -fx-font-weight: bold;" : "");
        style += (isFaded ? " -fx-opacity: 40%;" : ""); // change opacity if needed
        label.setStyle(style);

        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();
        double width = (double) fontLoader.computeStringWidth(label.getText(), label.getFont());
        label.setPrefWidth(width + 30);
        label.setText(label.getText());

        return label;
    }

    public PickerBoard highlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
        return this;
    }

    public PickerBoard faded(boolean isFaded) {
        this.isFaded = isFaded;
        return this;
    }

}
