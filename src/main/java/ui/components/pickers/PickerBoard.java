package ui.components.pickers;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ui.UI;

/**
 * This class is to represent a label in LabelPickerDialog
 *
 * It contains attributes such as selected, highlighted, removed and faded in order
 * to produce the appropriate styled node through getNode()
 */
public class PickerBoard extends VBox {

    @FXML
    private Label name;

    private String boardName;
    private boolean isHighlighted;

    public PickerBoard(String boardName) {
        this.boardName = boardName;
        isHighlighted = false;

        loadView();
    }

    private void loadView() {
        try {
            FXMLLoader loader = new FXMLLoader(UI.class.getResource("fxml/PickerBoardItem.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();

            name.setText(boardName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
        name.setText(boardName);
    }

    public String getBoardName() {
        return boardName;
    }

    public Node getNode() {
        // actual name for labels at the top, add tick for selected labels
        Label label = new Label(boardName);
        label.getStyleClass().add("labels");
        String style = getStyle() + (isHighlighted ? " -fx-border-color: black; -fx-font-weight: bold;" : "");
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

}
