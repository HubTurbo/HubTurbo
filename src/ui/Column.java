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
import javafx.scene.input.ClipboardContent;
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
	
	private static final String TOGGLE_HIERARCHY = "\u27A5";
	private static final String CLOSE_LIST = "\u2716";
	private static final String ADD_ISSUE = "\u271A";
	
	public static final String NO_FILTER = "No Filter";
	public static final FilterExpression EMPTY = filter.Predicate.EMPTY;

	private final Model model;
	private final ColumnControl parentColumnControl;
	private int columnIndex;
	private final SidePanel sidePanel;
	private boolean isSearchPanel = false;
	
	// Filter-related
	
	private Predicate<TurboIssue> predicate = p -> true;
	private FilterExpression currentFilterExpression = EMPTY;
	private EditableLabel filterInputArea;
	private String filterResponse;

	// Collection-related
	
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private FilteredList<TurboIssue> filteredList = null;

	private TurboCommandExecutor dragAndDropExecutor;

	public Column(Stage mainStage, Model model, ColumnControl parentColumnControl, SidePanel sidePanel, int columnIndex, TurboCommandExecutor dragAndDropExecutor, boolean isSearchPanel) {
		this.model = model;
		this.parentColumnControl = parentColumnControl;
		this.columnIndex = columnIndex;
		this.sidePanel = sidePanel;
		this.dragAndDropExecutor = dragAndDropExecutor;
		this.isSearchPanel = isSearchPanel;
		
		getChildren().add(createFilterBox());
		setupColumn();
	}
	
	private Node createFilterBox() {
		filterInputArea = new EditableLabel(NO_FILTER)
			.setEditTransformation(s -> {
				if (isSearchPanel) {
					isSearchPanel = false;
					return "title(" + s + ")";
				}
				return s;
			})
			.setTranslationFunction(filterString -> {
				applyStringFilter(filterString);
				// filterResponse is set after filterByString is called
				return filterResponse;
			})
			.setOnCancel(() -> {
				if (isSearchPanel) {
					parentColumnControl.closeColumn(columnIndex);
				}
			});

		setupColumnDragEvents(filterInputArea);
		if (isSearchPanel) filterInputArea.triggerEdit();
		
		HBox filterFieldBox = new HBox();
		filterFieldBox.setAlignment(Pos.BASELINE_LEFT);
		HBox.setHgrow(filterFieldBox, Priority.ALWAYS);
		setupIssueDragEvents(filterFieldBox);
		filterFieldBox.getChildren().add(filterInputArea);
		
		HBox buttonBox = new HBox();
		HBox.setMargin(buttonBox, new Insets(0,5,0,0));
		buttonBox.setSpacing(5);
		buttonBox.setAlignment(Pos.TOP_RIGHT);
		HBox.setHgrow(buttonBox, Priority.ALWAYS);
		buttonBox.getChildren().addAll(createButtons());
		
		HBox layout = new HBox();
		layout.setSpacing(5);
		layout.getChildren().addAll(filterFieldBox, buttonBox);
		
		return layout;
	}
	
	private Label[] createButtons() {
		Label addIssue = new Label(ADD_ISSUE);
		addIssue.getStyleClass().add("label-button");
		addIssue.setOnMouseClicked((e) -> {
			TurboIssue issue = new TurboIssue("", "", model);
			applyCurrentFilterExpressionToIssue(issue, false);
			sidePanel.triggerIssueCreate(issue);
		});
		
		Label closeList = new Label(CLOSE_LIST);
		closeList.getStyleClass().add("label-button");
		closeList.setOnMouseClicked((e) -> {
			parentColumnControl.closeColumn(columnIndex);
		});
		
		Label toggleHierarchyMode = new Label(TOGGLE_HIERARCHY);
		toggleHierarchyMode.getStyleClass().add("label-button");
		toggleHierarchyMode.setOnMouseClicked((e) -> {
			parentColumnControl.toggleColumn(columnIndex);
		});
		
		return new Label[] {toggleHierarchyMode, addIssue, closeList};
	}

	private void setupColumn() {
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
				if (dd.getColumnIndex() != columnIndex) {
					TurboIssue rightIssue = model.getIssueWithId(dd.getIssueIndex());
					applyCurrentFilterExpressionToIssue(rightIssue, true);
				}
			}
			e.setDropCompleted(success);
	
			e.consume();
		});
	}
	
	private void setupColumnDragEvents(HBox box) {
		setOnDragDetected((event) -> {
			Dragboard db = startDragAndDrop(TransferMode.MOVE);
			ClipboardContent content = new ClipboardContent();
			DragData dd = new DragData(DragData.Source.COLUMN, columnIndex, -1);
			content.putString(dd.serialise());
			db.setContent(content);
			event.consume();
		});
		
		setOnDragDone((event) -> {
//			if (event.getTransferMode() == TransferMode.MOVE) {
//			}
			event.consume();
		});
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
				DragData dd = DragData.deserialise(e.getDragboard().getString());
				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
					filterBox.getStyleClass().add("dragged-over");
				}
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

	// These two methods are triggered by the contents of the input area
	// changing. As such they should not be invoked manually, or the input
	// area won't update.
	
	private void applyStringFilter(String filterString) {
		try {
			FilterExpression filter = Parser.parse(filterString);
			if (filter != null) {
				this.applyFilterExpression(filter);
			} else {
				this.applyFilterExpression(EMPTY);
			}
		} catch (ParseException ex) {
			this.applyFilterExpression(EMPTY);
			// Override the text set in the above method
			filterResponse = "Parse error in filter: " + ex.getMessage();
		}
	}
	
	private void applyFilterExpression(FilterExpression filter) {
		currentFilterExpression = filter;
		
		if (filter == EMPTY) {
			filterResponse = NO_FILTER;
		} else {
			filterResponse = filter.toString();
		}

		// This cast utilises a functional interface
		final BiFunction<TurboIssue, Model, Boolean> temp = filter::isSatisfiedBy;
		predicate = i -> temp.apply(i, model);
		
		refreshItems();
	}
	
	// An odd workaround for the above problem: serialising, then
	// immediately parsing a filter expression, just so the update can be triggered
	// through the text contents of the input area changing.
	
	public void filter(FilterExpression filterExpr) {
		filterByString(filterExpr.toString());
	}

	public void filterByString(String filterString) {
		filterInputArea.setTextFieldText(filterString);
	}

	public void setItems(ObservableList<TurboIssue> items) {
		this.issues = items;
		refreshItems();
	}

	private void applyCurrentFilterExpressionToIssue(TurboIssue issue, boolean updateModel) {
		if (currentFilterExpression != EMPTY) {
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
	
	public boolean isSearchPanel() {
		return isSearchPanel;
	}
	
	// To be called by ColumnControl in order to update indices
	
	void updateIndex(int updated) {
		columnIndex = updated;
	}

	// To be called by subclasses
	
	protected FilteredList<TurboIssue> getFilteredList() {
		return filteredList;
	}
	
	// To be overridden by subclasses
	
	public void refreshItems() {
		filteredList = new FilteredList<TurboIssue>(issues, predicate);
	}
	
	public abstract void deselect();
}
