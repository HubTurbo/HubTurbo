package ui;

import java.util.function.Predicate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Model;
import model.TurboIssue;

public class IssuePanel extends VBox {

	private final Stage mainStage;
	private final Model model;
	
	private ListView<TurboIssue> listView;
	private ObservableList<TurboIssue> issues;
	private FilteredList<TurboIssue> filteredList;
	
	private Predicate<TurboIssue> predicate;

	public IssuePanel(Stage mainStage, Model model) {
		this.mainStage = mainStage;
		this.model = model;

		getChildren().add(createFilterBox());
		
		issues = FXCollections.observableArrayList();
		listView = new ListView<>();
		getChildren().add(listView);
		predicate = p -> true;
		
		setup();
		refreshItems();
	}

	private Node createFilterBox() {
		HBox box = new HBox();
		Label label = new Label("<no filter>");
		box.setOnMouseClicked((e) -> {
			(new FilterDialog(mainStage, model)).show().thenApply(
					filter -> {
						this.filter(filter);
						label.setText(filter.toString());
						return true;
					});
		});
		box.getChildren().add(label);
		return box;
	}

	private void setup() {
		setPrefWidth(400);
		setVgrow(listView, Priority.ALWAYS);
		HBox.setHgrow(this, Priority.ALWAYS);
		setStyle(UI.STYLE_BORDERS);
	}

	public void filter(Filter filter) {
		predicate = filter::isSatisfiedBy;
		refreshItems();
	}
	
	public void refreshItems() {
		filteredList = new FilteredList<TurboIssue>(issues, predicate);
		
		IssuePanel that = this;
		
		// Set the cell factory every time - this forces the list view to update
		listView.setCellFactory(new Callback<ListView<TurboIssue>, ListCell<TurboIssue>>() {
			@Override
			public ListCell<TurboIssue> call(ListView<TurboIssue> list) {
				return new CustomListCell(mainStage, model, that);
			}
		});
		
		// Supposedly this also causes the list view to update - not sure
		// if it actually does on platforms other than Linux...
		listView.setItems(null);
		
		listView.setItems(filteredList);
	}

	public void setItems(ObservableList<TurboIssue> issues) {
		
		this.issues = issues;
		
		refreshItems();
	}

	public ObservableList<TurboIssue> getItems() {
		return issues;
	}
}
