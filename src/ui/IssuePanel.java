package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import logic.TurboIssue;

public class IssuePanel extends VBox {

	private ListView<TurboIssue> listView = new ListView<>();
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private FilteredList<TurboIssue> filteredList;

	public IssuePanel() {
		getChildren().add(listView);

		listView.setCellFactory(new Callback<ListView<TurboIssue>, ListCell<TurboIssue>>() {
			@Override
			public ListCell<TurboIssue> call(ListView<TurboIssue> list) {
				return new CustomListCell();
			}
		});
		
		filteredList = new FilteredList<>(issues, p -> true);
		listView.setItems(filteredList);
	}

	public void filter(Filter filter) {
		filteredList.setPredicate(filter::isSatisfiedBy);
	
		// Set the cell factory again in order to force an update of the list view
		listView.setCellFactory(new Callback<ListView<TurboIssue>, ListCell<TurboIssue>>() {
			@Override
			public ListCell<TurboIssue> call(ListView<TurboIssue> list) {
				return new CustomListCell();
			}
		});
	}

	public ObservableList<TurboIssue> getItems() {
		return issues;
	}
}
