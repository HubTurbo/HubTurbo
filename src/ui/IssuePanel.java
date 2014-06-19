package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import logic.TurboIssue;

public class IssuePanel extends VBox {

	private ListView<TurboIssue> listView;
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private FilteredList<TurboIssue> filteredList;

	public IssuePanel() {
		listView = new ListView<>();
		getChildren().add(listView);
		setVgrow(listView, Priority.ALWAYS);
		
		refreshItems();
	}

	public void filter(Filter filter) {
		filteredList.setPredicate(filter::isSatisfiedBy);
		
		refreshItems();
	}
	
	public void refreshItems() {
		setItems(issues);
	}

	public void setItems(ObservableList<TurboIssue> issues) {
		filteredList = new FilteredList<>(issues, p -> true);
				
		// Set the cell factory every time - this forces the list view to update
		listView.setCellFactory(new Callback<ListView<TurboIssue>, ListCell<TurboIssue>>() {
			@Override
			public ListCell<TurboIssue> call(ListView<TurboIssue> list) {
				return new CustomListCell();
			}
		});
		
		// Supposedly this causes the list view to update - not sure
		// if it actually does on platforms other than Linux...
		listView.setItems(null);
		
		listView.setItems(filteredList);
	}

	public ObservableList<TurboIssue> getItems() {
		return issues;
	}
}
