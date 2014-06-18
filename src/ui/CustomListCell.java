package ui;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import com.sun.javafx.geom.Rectangle;

public class CustomListCell extends ListCell<String> {
	
	
	
    private static final String STYLE_PARENT_NAME = "-fx-font-size: 9px;";
	private static final String STYLE_ISSUE_NAME = "-fx-font-size: 24px;";

	@Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
//        Rectangle rect = new Rectangle(100, 20);     
        
        BorderPane bp = new BorderPane();
        bp.setCenter(new Label(item));
        
        VBox everything = new VBox();
        
        Text issueName = new Text(item);
        issueName.setStyle(STYLE_ISSUE_NAME);
        
        Text parentName = new Text("parent");
        parentName.setStyle(STYLE_PARENT_NAME);
        
        HBox labels = new HBox();
        Text label1 = new Text("label1");
        Text label2 = new Text("label2");
        labels.getChildren().addAll(label1, label2);
        labels.setStyle(Demo.STYLE_BORDERS);
        
        Text assignees = new Text("assignees");
        
        everything.getChildren().addAll(issueName, parentName, labels, assignees);
//        everything.getChildren().stream().forEach((node) -> node.setStyle(Demo.STYLE_BORDERS));
        
        if (item != null) {
            setGraphic(everything);
        }
    }
}
