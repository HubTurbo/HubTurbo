package ui.issuecolumn;

import java.util.Comparator;
import java.util.List;
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
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;
import ui.DragData;
import ui.FilterTextField;
import ui.StatusBar;
import ui.UI;
import ui.sidepanel.SidePanel;

import command.CommandType;
import command.TurboCommandExecutor;

import filter.ParseException;
import filter.Parser;
import filter.QualifierApplicationException;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;

/**
 * An IssueColumn is a Column meant for containing issues. The main additions to
 * Column are filtering functionality and a list of issues to be maintained.
 * The IssueColumn does not specify how the list is to be displayed -- subclasses
 * override methods which determine that.
 */
public abstract class IssueColumn extends Column {

	// private static final String ADD_ISSUE = "\u271A";
	// private static final String TOGGLE_HIERARCHY = "\u27A5";

	// Collection-related

	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();
	private TransformationList<TurboIssue, TurboIssue> transformedIssueList = null;

	// Filter-related

	public static final FilterExpression EMPTY = filter.expression.Qualifier.EMPTY;
	private Predicate<TurboIssue> predicate = p -> true;
	private FilterExpression currentFilterExpression = EMPTY;
	private FilterTextField filterTextField;

	public IssueColumn(UI ui, Stage mainStage, Model model, ColumnControl parentColumnControl, SidePanel sidePanel,
			int columnIndex, TurboCommandExecutor dragAndDropExecutor) {
		super(mainStage, model, parentColumnControl, columnIndex, dragAndDropExecutor);

		getChildren().add(createFilterBox());
		setupIssueColumnDragEvents(model, columnIndex);
	}

	private void setupIssueColumnDragEvents(Model model, int columnIndex) {
		setOnDragOver(e -> {
			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
				DragData dd = DragData.deserialise(e.getDragboard().getString());
				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
					e.acceptTransferModes(TransferMode.MOVE);
				}
			}
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

	private Node createFilterBox() {
		filterTextField = new FilterTextField("", 0).setOnConfirm((text) -> {
			applyStringFilter(text);
			return text;
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
		layout.setPadding(new Insets(0, 0, 3, 0));

		return layout;
	}

	private void setupIssueFocusEvents(FilterTextField field) {
		field.focusedProperty().addListener((obs, old, newValue) -> {
			if (newValue) {
				// Gained focus
				parentColumnControl.setCurrentlyFocusedColumnIndex(columnIndex);
			} else {
				// Lost focus; do nothing
			}
		});
	}

	private Label[] createButtons() {
		// Label addIssue = new Label(ADD_ISSUE);
		// addIssue.getStyleClass().add("label-button");
		// addIssue.setOnMouseClicked((e) -> {
		// ui.triggerEvent(new IssueCreatedEvent());
		// });

		Label closeList = new Label(CLOSE_COLUMN);
		closeList.getStyleClass().add("label-button");
		closeList.setOnMouseClicked((e) -> {
			parentColumnControl.closeColumn(columnIndex);
		});

		// Label toggleHierarchyMode = new Label(TOGGLE_HIERARCHY);
		// toggleHierarchyMode.getStyleClass().add("label-button");
		// toggleHierarchyMode.setOnMouseClicked((e) -> {
		// parentColumnControl.toggleColumn(columnIndex);
		// });

		return new Label[] { closeList };
	}

	private void setupIssueDragEvents(Node filterBox) {
		filterBox.setOnDragOver(e -> {
			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
				DragData dd = DragData.deserialise(e.getDragboard().getString());
				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
					e.acceptTransferModes(TransferMode.MOVE);
				} else if (dd.getSource() == DragData.Source.LABEL_TAB
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
				} else if (dd.getSource() == DragData.Source.COLUMN) {
					if (parentColumnControl.getCurrentlyDraggedColumnIndex() != columnIndex) {
						// Apparently the dragboard can't be updated while
						// the drag is in progress. This is why we use an
						// external source for updates.
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
				} else if (dd.getSource() == DragData.Source.COLUMN) {
					// This event is never triggered when the drag is ended.
					// It's not a huge deal, as this is only used to
					// reinitialise the currently-dragged slot in ColumnControl.
					// The other main consequence of this is that we can't
					// assert to check if the slot has been cleared when starting a drag-swap.
				} else if (dd.getSource() == DragData.Source.LABEL_TAB) {
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
			
			// Clear displayed message on successful filter
			StatusBar.displayMessage("");
		}
		catch (ParseException ex) {
			this.applyFilterExpression(EMPTY);
			// Overrides message in status bar
			StatusBar.displayMessage("Panel " + (columnIndex + 1) + ": Parse error in filter: " + ex.getMessage());
		}
	}

	private void applyFilterExpression(FilterExpression filter) {
		currentFilterExpression = filter;
		predicate = issue -> Qualifier.process(filter, issue);
		refreshItems();
	}

	// An odd workaround for the above problem: serialising, then
	// immediately parsing a filter expression, just so the update can be
	// triggered
	// through the text contents of the input area changing.

	public void filter(FilterExpression filterExpr) {
		filterByString(filterExpr.toString());
	}

	public void filterByString(String filterString) {
		filterTextField.setFilterText(filterString);
	}

	public FilterExpression getCurrentFilterExpression() {
		return currentFilterExpression;
	}

	public String getCurrentFilterString() {
		return filterTextField.getText();
	}

	private void applyCurrentFilterExpressionToIssue(TurboIssue issue, boolean updateModel) {
		if (currentFilterExpression != EMPTY) {
			try {
				if (currentFilterExpression.canBeAppliedToIssue()) {
					TurboIssue clone = new TurboIssue(issue);
					currentFilterExpression.applyTo(issue, model);
					if (updateModel) {
						dragAndDropExecutor.executeCommand(CommandType.EDIT_ISSUE, model, clone, issue);
					}
					parentColumnControl.refresh();
				} else {
					throw new QualifierApplicationException("Could not apply predicate " + currentFilterExpression
							+ ".");
				}
			} catch (QualifierApplicationException ex) {
				parentColumnControl.displayMessage(ex.getMessage());
			}
		}
	}

	protected TransformationList<TurboIssue, TurboIssue> getIssueList() {
		return transformedIssueList;
	}

	public void setItems(ObservableList<TurboIssue> items) {
		this.issues = items;
		refreshItems();
	}

	/**
	 * To be overridden by subclasses.
	 * 
	 * See docs in Column for refreshItems.
	 * 
	 */

	// deselect is not overriden

	@Override
	public void refreshItems() {
		transformedIssueList = new FilteredList<TurboIssue>(issues, predicate);

		// If parent issue, sort child issues by depth
		if (currentFilterExpression instanceof filter.expression.Qualifier) {
			List<String> names = ((filter.expression.Qualifier) currentFilterExpression).getQualifierNames();
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
}
