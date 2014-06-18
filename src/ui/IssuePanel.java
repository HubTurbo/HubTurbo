package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class IssuePanel extends VBox {

	private ListView<String> listView = new ListView<>();
	private ObservableList<String> items = FXCollections.observableArrayList();

	public IssuePanel() {
		listView.setItems(items);
		listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> list) {
				return new CustomListCell();
			}
		});
		getChildren().add(listView);
	}

	public ObservableList<String> getItems() {
		return items;
	}
}
