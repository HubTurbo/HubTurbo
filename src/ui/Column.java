package ui;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

import command.CommandType;
import command.TurboCommandExecutor;

import filter.FilterExpression;
import filter.ParseException;
import filter.Parser;
import filter.PredicateApplicationException;

public abstract class Column extends VBox {
	
	// A Column is a JavaFX node that is contained by a ColumnControl.
	// It is in charge of displaying a list of issues and providing functions
	// to filter it. It does not, however, specify how the list is to be
	// displayed -- that is the job of its subclasses.
	
	public static final String NO_FILTER = "No Filter";
	public static final filter.Predicate EMPTY_PREDICATE = new filter.Predicate();

	private final Stage mainStage;
	private final Model model;
	private final ColumnControl parentColumnControl;
	private int columnIndex;
	private final SidePanel sidePanel;

	private Predicate<TurboIssue> predicate = p -> true;
	private String filterInput = "";
	private FilterExpression currentFilterExpression = EMPTY_PREDICATE;
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private FilteredList<TurboIssue> filteredList = null;
	private TurboCommandExecutor dragAndDropExecutor;
	private Label filterLabel;

	public Column(Stage mainStage, Model model, ColumnControl parentColumnControl, SidePanel sidePanel, int columnIndex, TurboCommandExecutor dragAndDropExecutor) {
		this.mainStage = mainStage;
		this.model = model;
		this.parentColumnControl = parentColumnControl;
		this.columnIndex = columnIndex;
		this.sidePanel = sidePanel;
		this.dragAndDropExecutor = dragAndDropExecutor;
		
		getChildren().add(createFilterBox());
		setup();
	}
	
	private void setup() {
		setPrefWidth(380);
		setMaxWidth(380);
		
		getStyleClass().add("borders");
		
		setOnDragOver(e -> {
			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
				DragData dd = DragData.deserialise(e.getDragboard().getString());
				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
					e.acceptTransferModes(TransferMode.MOVE);
				}
			}
		});

		setOnDragEntered(e -> {
			if (e.getDragboard().hasString()) {
				DragData dd = DragData.deserialise(e.getDragboard().getString());
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
				DragData dd = DragData.deserialise(db.getString());
				TurboIssue rightIssue = model.getIssueWithId(dd.getIssueIndex());
				applyCurrentFilterExpressionToIssue(rightIssue, true);
			}
			e.setDropCompleted(success);

			e.consume();
		});
	}

	private Node createFilterBox() {
		HBox filterBox = new HBox();

		filterLabel = new Label(NO_FILTER);
		filterLabel.setPadding(new Insets(3));
		
		filterBox.setOnMouseClicked(e -> onFilterBoxClick());
		filterBox.setAlignment(Pos.TOP_LEFT);
		HBox.setHgrow(filterBox, Priority.ALWAYS);
		filterBox.getChildren().add(filterLabel);
		setupIssueDragEvents(filterBox);
		
		HBox rightAlignBox = new HBox();
	
		Label addIssue = new Label("\u271A");
		addIssue.setStyle("-fx-font-size: 16pt;");
		addIssue.setOnMouseClicked((e) -> {
			TurboIssue issue = new TurboIssue("New issue", "", model);
			applyCurrentFilterExpressionToIssue(issue, false);
			
			sidePanel.displayIssue(issue).thenApply(r -> {
				if (r.equals("done")) {
					model.createIssue(issue);
				}
				parentColumnControl.refresh();
				sidePanel.displayTabs();
				return true;
			}).exceptionally(ex -> {
				ex.printStackTrace();
				return false;
			});
		});
		
		Label closeList = new Label("\u2716");
		closeList.setStyle("-fx-font-size: 16pt;");
		closeList.setOnMouseClicked((e) -> {
			parentColumnControl.closeColumn(columnIndex);
		});
		
		Label toggleHierarchyMode = new Label("\u27A5");
		toggleHierarchyMode.setStyle("-fx-font-size: 16pt;");
		toggleHierarchyMode.setOnMouseClicked((e) -> {
			parentColumnControl.toggleColumn(columnIndex);
		});

		HBox.setMargin(rightAlignBox, new Insets(0,5,0,0));
		rightAlignBox.setSpacing(5);
		rightAlignBox.setAlignment(Pos.TOP_RIGHT);
		HBox.setHgrow(rightAlignBox, Priority.ALWAYS);
		rightAlignBox.getChildren().addAll(toggleHierarchyMode, addIssue, closeList);
		
		HBox topBox = new HBox();
		topBox.setSpacing(5);
		topBox.getChildren().addAll(filterBox, rightAlignBox);
		
		return topBox;
	}
	
	private void setupIssueDragEvents(HBox filterBox) {
		filterBox.setOnDragOver(e -> {
			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
				DragData dd = DragData.deserialise(e.getDragboard().getString());
				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
					e.acceptTransferModes(TransferMode.MOVE);
				}
			}
		});

		filterBox.setOnDragEntered(e -> {
			if (e.getDragboard().hasString()) {
				filterBox.getStyleClass().add("dragged-over");
			}
			e.consume();
		});

		filterBox.setOnDragExited(e -> {
			filterBox.getStyleClass().remove("dragged-over");
			e.consume();
		});
		
		filterBox.setOnDragDropped(e -> {
			Dragboard db = e.getDragboard();
			boolean success = false;
			if (db.hasString()) {
				success = true;
				DragData dd = DragData.deserialise(db.getString());
				TurboIssue rightIssue = model.getIssueWithId(dd.getIssueIndex());
				filterByString("parent(#" + rightIssue.getId() + ")");
			}
			e.setDropCompleted(success);

			e.consume();
		});
	}

	private void onFilterBoxClick() {
		(new FilterDialog(mainStage, filterInput))
			.show()
			.thenApply(filterString -> {
				filterByString(filterString);
				return true;
			}).exceptionally(ex -> {
				ex.printStackTrace();
				return false;
			});
	}
	
	public void filterByString(String filterString) {
		filterInput = filterString;
		if (filterString.isEmpty()) {
			filterLabel.setText(NO_FILTER);
			this.filter(EMPTY_PREDICATE);
		} else {
			try {
				FilterExpression filter = Parser.parse(filterString);
				if (filter != null) {
					filterLabel.setText(filter.toString());
					this.filter(filter);
				} else {
					filterLabel.setText(NO_FILTER);
					this.filter(EMPTY_PREDICATE);
				}
			} catch (ParseException ex) {
				filterLabel.setText("Parse error in filter: " + ex);
				this.filter(EMPTY_PREDICATE);
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
	
	public FilteredList<TurboIssue> getFilteredList() {
		return filteredList;
	}
	
	public void setItems(ObservableList<TurboIssue> items) {
		this.issues = items;
		refreshItems();
	}

	public void refreshItems() {
		filteredList = new FilteredList<TurboIssue>(issues, predicate);
	}
	
	public abstract void deselect();

	private void applyCurrentFilterExpressionToIssue(TurboIssue issue, boolean updateModel) {
		if (currentFilterExpression != EMPTY_PREDICATE) {
			try {
				if (currentFilterExpression.canBeAppliedToIssue()) {
					TurboIssue clone = new TurboIssue(issue);
					currentFilterExpression.applyTo(issue, model);
					if (updateModel){
						dragAndDropExecutor.executeCommand(CommandType.EDIT_ISSUE,  model, clone, issue);
					}
					parentColumnControl.refresh();
				} else {
					throw new PredicateApplicationException("Could not apply predicate " + currentFilterExpression + ".");
				}
			} catch (PredicateApplicationException ex) {
				parentColumnControl.displayMessage(ex.getMessage());
			}
		}
	}
	
	public FilterExpression getCurrentFilterExpression() {
		return currentFilterExpression;
	}
	
	public void updateIndex(int updated) {
		columnIndex = updated;
	}

}
