package ui;

import java.lang.ref.WeakReference;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Model;
import model.TurboIssue;
import filter.FilterExpression;
import filter.ParseException;
import filter.Parser;
import filter.PredicateApplicationException;

public class IssuePanel extends VBox {

	private static final String NO_FILTER = "No Filter";
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

		getChildren().add(createTop());
		
		issues = FXCollections.observableArrayList();
		listView = new ListView<>();
		getChildren().add(listView);
		predicate = p -> true;
		
		setup();
		refreshItems();
	}

	private Node createTop() {
		
		HBox filterBox = new HBox();
		Label label = new Label(NO_FILTER);
		label.setPadding(new Insets(3));
		filterBox.setOnMouseClicked((e) -> {
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
					})
				.exceptionally(ex -> {
					ex.printStackTrace();
					return false;
				});
		});
		filterBox.setAlignment(Pos.TOP_LEFT);
		HBox.setHgrow(filterBox, Priority.ALWAYS);
		filterBox.getChildren().add(label);
		
		HBox rightAlignBox = new HBox();

		Label addIssue = new Label("\u2795");
		addIssue.setStyle("-fx-font-size: 16pt;");
		addIssue.setOnMouseClicked((e) -> {
			TurboIssue issue = new TurboIssue("New issue", "", model);
			applyCurrentFilterExpressionToIssue(issue, false);
			(new IssueDialog(mainStage, model, issue)).show().thenApply(
					response -> {
						if (response.equals("ok")) {
							model.createIssue(issue);
						}
						// Required for some reason
						parentColumnControl.refresh();
						return true;
					})
					.exceptionally(ex -> {
						ex.printStackTrace();
						return false;
					});
		});
		
		Label closeList = new Label("\u274c");
		closeList.setStyle("-fx-font-size: 16pt;");
		closeList.setOnMouseClicked((e) -> {
			parentColumnControl.closeColumn(columnIndex);
		});
		
		HBox.setMargin(rightAlignBox, new Insets(0,5,0,0));
		rightAlignBox.setSpacing(5);
		rightAlignBox.setAlignment(Pos.TOP_RIGHT);
		HBox.setHgrow(rightAlignBox, Priority.ALWAYS);
		rightAlignBox.getChildren().addAll(addIssue, closeList);
		
		HBox topBox = new HBox();
		topBox.setSpacing(5);
		topBox.getChildren().addAll(filterBox, rightAlignBox);
		
		return topBox;
	}

	private void setup() {
		setPrefWidth(380);
		setMaxWidth(380);
		setVgrow(listView, Priority.ALWAYS);
//		HBox.setHgrow(this, Priority.ALWAYS);
		getStyleClass().add("borders");
		
		setOnDragOver(e -> {
			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
				e.acceptTransferModes(TransferMode.MOVE);
			}
		});

		setOnDragEntered(e -> {
			if (e.getDragboard().hasString()) {
				IssuePanelDragData dd = IssuePanelDragData.deserialise(e.getDragboard().getString());
				if (dd.getColumnIndex() != columnIndex) {
					getStyleClass().add("dragged-over");
				}
			}
			e.consume();
		});

		setOnDragExited(e -> {
			getStyleClass().remove("dragged-over");
			e.consume();
		});
		
		setOnDragDropped(e -> {
			Dragboard db = e.getDragboard();
			boolean success = false;
			if (db.hasString()) {
				success = true;
				IssuePanelDragData dd = IssuePanelDragData.deserialise(db.getString());
								
				// Find the right issue from its ID
				TurboIssue rightIssue = null;
				for (TurboIssue i : model.getIssues()) {
					if (i.getId() == dd.getIssueIndex()) {
						rightIssue = i;
					}
				}
				assert rightIssue != null;
				applyCurrentFilterExpressionToIssue(rightIssue, true);
			}
			e.setDropCompleted(success);

			e.consume();
		});
	}
	
	private void applyCurrentFilterExpressionToIssue(TurboIssue issue, boolean updateModel) {
		if (currentFilterExpression != EMPTY_PREDICATE) {
			try {
				if (currentFilterExpression.canBeAppliedToIssue()) {
					TurboIssue clone = new TurboIssue(issue);
					currentFilterExpression.applyTo(issue, model);
					if (updateModel) model.updateIssue(clone, issue);
					parentColumnControl.refresh();
				} else {
					throw new PredicateApplicationException("Could not apply predicate " + currentFilterExpression + ".");
				}
			} catch (PredicateApplicationException ex) {
				parentColumnControl.displayMessage(ex.getMessage());
			}
		}
	}
	
	public void filter(FilterExpression filter) {
		currentFilterExpression = filter;

		// This cast utilises a functional interface
		final BiFunction<TurboIssue, Model, Boolean> temp = filter::isSatisfiedBy;
		predicate = i -> temp.apply(i, model);
		
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
					return new IssuePanelCell(mainStage, model, that.get(), columnIndex);
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
