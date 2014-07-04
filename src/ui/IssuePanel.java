package ui;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import filter.FilterExpression;
import filter.Parser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
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
import filter.ParseException;

public class IssuePanel extends VBox {

	private static final String NO_FILTER = "<no filter>";
	private final Stage mainStage;
	private final Model model;
	
	private ListView<TurboIssue> listView;
	private ObservableList<TurboIssue> issues;
	private FilteredList<TurboIssue> filteredList;
	
	private Predicate<TurboIssue> predicate;
	private String filterInput = "";

	public static final filter.Predicate EMPTY_PREDICATE = new filter.Predicate();

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
		Label label = new Label(NO_FILTER);
		label.setPadding(new Insets(3));
		box.setOnMouseClicked((e) -> {
			(new FilterDialog(mainStage, model, filterInput)).show().thenApply(
					filterString -> {
						filterInput = filterString;
						if (filterString.isEmpty()) {
							label.setText(NO_FILTER);
							this.filter(EMPTY_PREDICATE);
						} else {
				        	try {
				        		FilterExpression filter = Parser.parse(filterString);
				        		if (filter != null) {
									label.setText(filter.toString());
				                	this.filter(filter);
				        		} else {
									label.setText(NO_FILTER);
				                	this.filter(EMPTY_PREDICATE);
				        		}
				        	} catch (ParseException ex){
				            	label.setText("Parse error in filter: " + ex);
				            	this.filter(EMPTY_PREDICATE);
				        	}
						}
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
//		setStyle(UI.STYLE_BORDERS);
		getStyleClass().add("borders");
	}

	public void filter(FilterExpression filter) {
		predicate = filter::isSatisfiedBy;
		refreshItems();
	}
	
	public void refreshItems() {
		filteredList = new FilteredList<TurboIssue>(issues, predicate);
		
		WeakReference<IssuePanel> that = new WeakReference<IssuePanel>(this);
		
		// Set the cell factory every time - this forces the list view to update
		listView.setCellFactory(new Callback<ListView<TurboIssue>, ListCell<TurboIssue>>() {
			@Override
			public ListCell<TurboIssue> call(ListView<TurboIssue> list) {
				if(that.get() != null){
					return new IssuePanelCell(mainStage, model, that.get());
				}else{
					return null;
				}
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
