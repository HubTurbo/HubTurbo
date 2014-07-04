//package ui;
//
//import java.awt.Desktop;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.ArrayList;
//
//import model.Model;
//import model.TurboIssue;
//import model.TurboUser;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.scene.control.CheckBox;
//import javafx.scene.control.ContextMenu;
//import javafx.scene.control.Label;
//import javafx.scene.control.ListCell;
//import javafx.scene.control.MenuItem;
//import javafx.scene.input.MouseButton;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.scene.text.Font;
//import javafx.scene.text.Text;
//import javafx.stage.Stage;
//
//public class SingleCheckListCell extends ListCell<SingleCheckListItemModel> {
//	
//	boolean checked = false;
//	
//	public SingleCheckListCell() {
//		super();
//	}
//
//	@Override
//	public void updateItem(SingleCheckListItemModel item, boolean empty) {
//		super.updateItem(item, empty);
//		if (item == null)
//			return;
//
//		CheckBox checkbox = new CheckBox();
//		checkbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
//	        public void changed(ObservableValue<? extends Boolean> ov,
//	            Boolean oldValue, Boolean newValue) {
//	        	checked = newValue;
//	        }
//	    });
//		checkbox.setSelected(checked);
//
//		Label text = new Label(item.getContents());
//		
//		
//		HBox everything = new HBox();
//		everything.setSpacing(4);
//		everything.getChildren().addAll(checkbox, text);
////		if (assignee != null) everything.getChildren().add(assigneeBox);
////		everything.getChildren().add(labels);
//
//		setGraphic(everything);
//
////		setStyle(UI.STYLE_BORDERS + "-fx-border-radius: 5;");
////		getStyleClass().add("borders");
//		
////		setContextMenu(new ContextMenu(createGroupContextMenu(issue)));
//
////		registerEvents(issue);
//	}
//	
//	
//}
