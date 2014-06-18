package ui;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import logic.Issue;

public class CustomListCell extends ListCell<Issue> {
	

    private static final String STYLE_PARENT_NAME = "-fx-font-size: 9px;";
	private static final String STYLE_ISSUE_NAME = "-fx-font-size: 24px;";

	@Override
    public void updateItem(Issue issue, boolean empty) {
        super.updateItem(issue, empty);
        if (issue == null) return;
        
        VBox everything = new VBox();
        
        Text issueName = new Text(issue.getTitle());
        issueName.setStyle(STYLE_ISSUE_NAME);
        issue.titleProperty().addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> stringProperty, String oldValue, String newValue) {
				issueName.setText(newValue);
			}
          });
        
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
        
        if (issue != null) {
            setGraphic(everything);
        }
    }
}
