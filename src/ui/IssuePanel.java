package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import logic.TurboIssue;

public class IssuePanel extends VBox {

	private ListView<TurboIssue> listView = new ListView<>();
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();

	public IssuePanel() {
		listView.setItems(issues);
		listView.setCellFactory(new Callback<ListView<TurboIssue>, ListCell<TurboIssue>>() {
			@Override
			public ListCell<TurboIssue> call(ListView<TurboIssue> list) {
				return new CustomListCell();
			}
		});
		getChildren().add(listView);
	}

	public ObservableList<TurboIssue> getItems() {
		return issues;
	}
}
