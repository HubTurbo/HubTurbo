package ui.issuecolumn;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.collections.transformation.TransformationList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;
import ui.DragData;
import ui.FilterTextField;
import ui.StatusBar;
import ui.sidepanel.SidePanel;

import command.CommandType;
import command.TurboCommandExecutor;

import filter.FilterExpression;
import filter.ParseException;
import filter.Parser;
import filter.PredicateApplicationException;

/**
 * A Column is a JavaFX node that is contained by a ColumnControl.
 * It is in charge of displaying a list of issues and providing functions
 * to filter it. It does not, however, specify how the list is to be
 * displayed -- that is the job of its subclasses.
 */
public abstract class Column extends VBox {
	
	private static final int COLUMN_WIDTH = 400;
	
	//	private static final String TOGGLE_HIERARCHY = "\u27A5";
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
	private FilterTextField filterTextField;

	// Collection-related
	
	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private TransformationList<TurboIssue, TurboIssue> transformedIssueList = null;

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
//		String initialText = isSearchPanel ? "title()" : "";
//		int initialPosition = isSearchPanel ? 6 : 0;
		
		filterTextField = new FilterTextField("", 0)
			.setOnConfirm((text) -> {
				if (Parser.isListOfSymbols(text)) {
					text = "title(" + text + ")";
				}
				applyStringFilter(text);
				return text;
			})
			.setOnCancel(() -> {
//				parentColumnControl.closeColumn(columnIndex);
			});

		setupIssueDragEvents(filterTextField);
		setupIssueFocusEvents(filterTextField);
	
		HBox buttonsBox = new HBox();
		buttonsBox.setSpacing(5);
		buttonsBox.setAlignment(Pos.TOP_RIGHT);
		buttonsBox.setMinWidth(50);
		buttonsBox.getChildren().addAll(createButtons());
		
		HBox layout = new HBox();
		layout.getChildren().addAll(filterTextField, buttonsBox);
		layout.setPadding(new Insets(0,0,3,0));		
		
		setupColumnDragEvents(layout);
		return layout;
	}
	
	private void setupIssueFocusEvents(FilterTextField field) {
		field.focusedProperty().addListener((obs, old, newValue) -> {
			if (newValue) {
				// Gained focus
				parentColumnControl.setCurrentlyFocusedColumnIndex(columnIndex);
			} else {
				// Lost focus
				// Do nothing
			}
		});
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
		
//		Label toggleHierarchyMode = new Label(TOGGLE_HIERARCHY);
//		toggleHierarchyMode.getStyleClass().add("label-button");
//		toggleHierarchyMode.setOnMouseClicked((e) -> {
//			parentColumnControl.toggleColumn(columnIndex);
//		});
		
		return new Label[] {addIssue, closeList};
	}

	private void setupColumn() {
		setPrefWidth(COLUMN_WIDTH);
		setMinWidth(COLUMN_WIDTH);
		setPadding(new Insets(5));
		getStyleClass().addAll("borders", "rounded-borders");
		
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
			DragData dd = new DragData(DragData.Source.COLUMN, -1, -1);
			content.putString(dd.serialise());
			db.setContent(content);
			// We're using this because the content of a dragboard can't be changed
			// while the drag is in progress; this seemed like the simplest workaround
			parentColumnControl.setCurrentlyDraggedColumnIndex(columnIndex);
			event.consume();
		});
		
		setOnDragDone((event) -> {
//			if (event.getTransferMode() == TransferMode.MOVE) {
//			}
			event.consume();
		});
	}

	private void setupIssueDragEvents(Node filterBox) {
		filterBox.setOnDragOver(e -> {
			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
				DragData dd = DragData.deserialise(e.getDragboard().getString());
				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
					e.acceptTransferModes(TransferMode.MOVE);
				}
				else if (dd.getSource() == DragData.Source.LABEL_TAB
						|| dd.getSource() == DragData.Source.ASSIGNEE_TAB
						|| dd.getSource() == DragData.Source.MILESTONE_TAB) {
					e.acceptTransferModes(TransferMode.COPY);
				}
			}
		});

		filterBox.setOnDragEntered(e -> {
			if (e.getDragboard().hasString()) {
				DragData dd = DragData.deserialise(e.getDragboard().getString());
				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
					filterBox.getStyleClass().add("dragged-over");
				}
				else if (dd.getSource() == DragData.Source.COLUMN) {
					if (parentColumnControl.getCurrentlyDraggedColumnIndex() != columnIndex) {
						// Apparently the dragboard can't be updated while
						// the drag is in progress. This is why we use an external source for updates.
						assert parentColumnControl.getCurrentlyDraggedColumnIndex() != -1;
						int previous = parentColumnControl.getCurrentlyDraggedColumnIndex();
						parentColumnControl.setCurrentlyDraggedColumnIndex(columnIndex);
						parentColumnControl.swapColumns(previous, columnIndex);
					}
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
				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
					TurboIssue rightIssue = model.getIssueWithId(dd.getIssueIndex());
					filterByString("parent(#" + rightIssue.getId() + ")");
				}
				else if (dd.getSource() == DragData.Source.COLUMN) {
					// This event is never triggered when the drag is ended.
					// It's not a huge deal, as this is only used to reinitialise
					// the currently-dragged slot in ColumnControl.
					// The other main consequence of this is that we can't assert
					// to check if the slot has been cleared when starting a drag-swap.
				}
				else if (dd.getSource() == DragData.Source.LABEL_TAB) {
					filterByString("label(" + dd.getEntityName() + ")");
				} else if (dd.getSource() == DragData.Source.ASSIGNEE_TAB) {
					filterByString("assignee(" + dd.getEntityName() + ")");
				} else if (dd.getSource() == DragData.Source.MILESTONE_TAB) {
					filterByString("milestone(" + dd.getEntityName() + ")");
				}

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
			StatusBar.displayMessage(""); //Clear displayed message on successful filter
		} catch (ParseException ex) {
			this.applyFilterExpression(EMPTY);
			// Override the text set in the above method

			StatusBar.displayMessage("Panel " + (columnIndex+1) + ": Parse error in filter: " + ex.getMessage());
		}
	}
	
	private void applyFilterExpression(FilterExpression filter) {
		currentFilterExpression = filter;
		
//		if (filter == EMPTY) {
//			statusBar.setText("Panel " + (columnIndex+1) + ": " + NO_FILTER);
//		} else {
//			statusBar.setText("Panel " + (columnIndex+1) + ": " + filter.toString());
//		}

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
		filterTextField.setFilterText(filterString);
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
	
	protected TransformationList<TurboIssue, TurboIssue> getIssueList() {
		return transformedIssueList;
	}
	
	// To be overridden by subclasses
	
	public void refreshItems() {
		transformedIssueList = new FilteredList<TurboIssue>(issues, predicate);

		if (currentFilterExpression instanceof filter.Predicate) {
			List<String> names = ((filter.Predicate) currentFilterExpression).getPredicateNames();
			if (names.size() == 1 && names.get(0).equals("parent")) {
				transformedIssueList = new SortedList<>(transformedIssueList, new Comparator<TurboIssue>() {
				    @Override
				    public int compare(TurboIssue a, TurboIssue b) {
				    	return a.getDepth() - b.getDepth();
				    }
				});
			}
		}
	}
	
	public abstract void deselect();
}
