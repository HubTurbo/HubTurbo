package ui;

import java.lang.ref.WeakReference;
import java.util.function.Predicate;

import javafx.scene.input.TransferMode;
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
import javafx.scene.input.Dragboard;
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
	public static final filter.Predicate EMPTY_PREDICATE = new filter.Predicate();

	private final Stage mainStage;
	private final Model model;
	private final ColumnControl parentColumnControl;
	private final int columnIndex;
	
	private ListView<TurboIssue> listView;
	private ObservableList<TurboIssue> issues;
	private FilteredList<TurboIssue> filteredList;
	
	private Predicate<TurboIssue> predicate;
	private String filterInput = "";
	private FilterExpression currentFilterExpression = EMPTY_PREDICATE;

	public IssuePanel(Stage mainStage, Model model, ColumnControl parentColumnControl, int columnIndex) {
		this.mainStage = mainStage;
		this.model = model;
		this.parentColumnControl = parentColumnControl;
		this.columnIndex = columnIndex;

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
		getStyleClass().add("borders");
		
		setOnDragOver(e -> {
//			if (e.getGestureSource() != item && e.getDragboard().hasString()) {
//				e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
//			}
		});

		setOnDragEntered(e -> {
//			if (e.getGestureSource() != item && e.getDragboard().hasString()) {
//				item.setStyle(STYLE_YELLOW_BORDERS);
//			}

			e.consume();
		});

		setOnDragExited(e -> {
//			item.setStyle(style);

			e.consume();
		});

		setOnDragDropped(e -> {
			Dragboard db = e.getDragboard();
			boolean success = false;
			if (db.hasString()) {
				success = true;

//				currentPredicate.applyTo(issue);

//				DragData dd = DragData.deserialize(db.getString());
//
//				if (dd.source == DragSource.PANEL_MILESTONES) {
//					text.setText(text.getText() + "\n" + "added item "
//							+ dd.index);
//				} else if (dd.source == DragSource.TREE_ISSUES) {
//					text.setText(text.getText() + "\n" + "added issue "
//							+ dd.text);
//				} else if (dd.source == DragSource.TREE_LABELS) {
//					text.setText(text.getText() + "\n" + "added label "
//							+ dd.text);
//				} else if (dd.source == DragSource.TREE_CONTRIBUTORS) {
//					text.setText(text.getText() + "\n" + "added contributors "
//							+ dd.text);
//				}
			}
			e.setDropCompleted(success);

			e.consume();
		});
	}

	public void filter(FilterExpression filter) {
		currentFilterExpression = filter;
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
				} else{
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

	public FilterExpression getCurrentFilterExpression() {
		return currentFilterExpression;
	}
}
