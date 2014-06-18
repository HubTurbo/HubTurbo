package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import logic.Issue;

public class IssuePanel extends VBox {

	private ListView<Issue> listView = new ListView<>();
	private ObservableList<Issue> issues = FXCollections.observableArrayList();

	public IssuePanel() {
		listView.setItems(issues);
		listView.setCellFactory(new Callback<ListView<Issue>, ListCell<Issue>>() {
			@Override
			public ListCell<Issue> call(ListView<Issue> list) {
				return new CustomListCell();
			}
		});
		getChildren().add(listView);
	}

	public ObservableList<Issue> getItems() {
		return issues;
	}
}
