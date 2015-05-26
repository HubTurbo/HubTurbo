package ui.issuecolumn;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import backend.resource.TurboUser;
import filter.ParseException;
import filter.Parser;
import filter.QualifierApplicationException;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.TransformationList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.UI;
import ui.components.FilterTextField;
import util.events.ColumnClickedEvent;
import util.events.ModelUpdatedEventHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An IssueColumn is a Column meant for containing issues. The main additions to
 * Column are filtering functionality and a list of issues to be maintained.
 * The IssueColumn does not specify how the list is to be displayed -- subclasses
 * override methods which determine that.
 */
public abstract class IssueColumn extends Column {

	private static final Logger logger = LogManager.getLogger(IssueColumn.class.getName());

	// Collection-related

	private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();

	// Filter-related

	private TransformationList<TurboIssue, TurboIssue> transformedIssueList = null;
	public static final FilterExpression EMPTY = filter.expression.Qualifier.EMPTY;
	private Predicate<TurboIssue> predicate = p -> true;
	private FilterExpression currentFilterExpression = EMPTY;
	protected FilterTextField filterTextField;
	private UI ui;

	public IssueColumn(UI ui, IModel model, ColumnControl parentColumnControl, int columnIndex) {
		super(model, parentColumnControl, columnIndex);
		this.ui = ui;
		getChildren().add(createFilterBox());
//		setupIssueColumnDragEvents(model, columnIndex);
		this.setOnMouseClicked(e-> {
			ui.triggerEvent(new ColumnClickedEvent(columnIndex));
			requestFocus();
		});
		focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> unused, Boolean wasFocused, Boolean isFocused) {
				if (isFocused) {
				    getStyleClass().add("panel-focused");
				} else {
				    getStyleClass().remove("panel-focused");
				}
			}
		});
	}

//	private void setupIssueColumnDragEvents(Model model, int columnIndex) {
//		setOnDragOver(e -> {
//			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
//				DragData dd = DragData.deserialise(e.getDragboard().getString());
//				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
//					e.acceptTransferModes(TransferMode.MOVE);
//				}
//			}
//		});
//
//		setOnDragDropped(e -> {
//			Dragboard db = e.getDragboard();
//			boolean success = false;
//
//			if (db.hasString()) {
//				success = true;
//				DragData dd = DragData.deserialise(db.getString());
//				if (dd.getColumnIndex() != columnIndex) {
//					Optional<TurboIssue> rightIssue = model.getIssueById(dd.getIssueIndex());
//					assert rightIssue.isPresent();
//					applyCurrentFilterExpressionToIssue(rightIssue.get(), true);
//				}
//			}
//			e.setDropCompleted(success);
//
//			e.consume();
//		});
//	}

	private final ModelUpdatedEventHandler onModelUpdate = e -> {
		List<String> all = new ArrayList<>(Arrays.asList(
			"label", "milestone",
			"involves", "assignee", "author",
			"title", "body",
			"is", "issue", "pr", "merged", "unmerged",
			"no", "type", "has",
			"state", "open", "closed",
			"created",
			"updated"));
		all.addAll(e.model.getUsers().stream()
			.map(TurboUser::getLoginName)
			.collect(Collectors.toList()));

		filterTextField.setKeywords(all);
	};

	private Node createFilterBox() {
		filterTextField = new FilterTextField("", 0).setOnConfirm((text) -> {
			applyStringFilter(text);
			return text;
		});

		ui.registerEvent(onModelUpdate);

		filterTextField.setOnMouseClicked(e -> ui.triggerEvent(new ColumnClickedEvent(columnIndex)));

//		setupIssueDragEvents(filterTextField);

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

	private Label[] createButtons() {
		// Label addIssue = new Label(ADD_ISSUE);
		// addIssue.getStyleClass().add("label-button");
		// addIssue.setOnMouseClicked((e) -> {
		// ui.triggerEvent(new IssueCreatedEvent());
		// });

		Label closeList = new Label(CLOSE_COLUMN);
		closeList.getStyleClass().add("label-button");
		closeList.setOnMouseClicked((e) -> {
			e.consume();
			parentColumnControl.closeColumn(columnIndex);
		});

		// Label toggleHierarchyMode = new Label(TOGGLE_HIERARCHY);
		// toggleHierarchyMode.getStyleClass().add("label-button");
		// toggleHierarchyMode.setOnMouseClicked((e) -> {
		// parentColumnControl.toggleColumn(columnIndex);
		// });

		return new Label[] { closeList };
	}

//	private void setupIssueDragEvents(Node filterBox) {
//		filterBox.setOnDragOver(e -> {
//			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
//				DragData dd = DragData.deserialise(e.getDragboard().getString());
//				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
//					e.acceptTransferModes(TransferMode.MOVE);
//				} else if (dd.getSource() == DragData.Source.LABEL_TAB
//						|| dd.getSource() == DragData.Source.ASSIGNEE_TAB
//						|| dd.getSource() == DragData.Source.MILESTONE_TAB) {
//					e.acceptTransferModes(TransferMode.COPY);
//				}
//			}
//		});
//
//		filterBox.setOnDragEntered(e -> {
//			if (e.getDragboard().hasString()) {
//				DragData dd = DragData.deserialise(e.getDragboard().getString());
//				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
//					filterBox.getStyleClass().add("dragged-over");
//				} else if (dd.getSource() == DragData.Source.COLUMN) {
//					if (parentColumnControl.getCurrentlyDraggedColumnIndex() != columnIndex) {
//						// Apparently the dragboard can't be updated while
//						// the drag is in progress. This is why we use an
//						// external source for updates.
//						assert parentColumnControl.getCurrentlyDraggedColumnIndex() != -1;
//						int previous = parentColumnControl.getCurrentlyDraggedColumnIndex();
//						parentColumnControl.setCurrentlyDraggedColumnIndex(columnIndex);
//						parentColumnControl.swapColumns(previous, columnIndex);
//					}
//				}
//			}
//			e.consume();
//		});
//
//		filterBox.setOnDragExited(e -> {
//			filterBox.getStyleClass().remove("dragged-over");
//			e.consume();
//		});
//
//		filterBox.setOnDragDropped(e -> {
//			Dragboard db = e.getDragboard();
//			boolean success = false;
//			if (db.hasString()) {
//				success = true;
//				DragData dd = DragData.deserialise(db.getString());
//				if (dd.getSource() == DragData.Source.ISSUE_CARD) {
//
//					assert model.getIssueById(dd.getIssueIndex()).isPresent();
//					TurboIssue rightIssue = model.getIssueById(dd.getIssueIndex()).get();
//					if (rightIssue.getLabels().size() == 0) {
//						// If the issue has no labels, show it by its title to inform
//						// the user that there are no similar issues
//						filter(new Qualifier("keyword", rightIssue.getTitle()));
//					} else {
//						// Otherwise, take the disjunction of its labels to show similar
//						// issues.
//						List<TurboLabel> labels = model.getLabelsOfIssue(rightIssue);
//						FilterExpression result = new Qualifier("label", labels.get(0).getName());
//						List<FilterExpression> rest = labels.stream()
//							.skip(1)
//							.map(label -> new Qualifier("label", label.getName()))
//							.collect(Collectors.toList());
//
//						for (FilterExpression label : rest) {
//							result = new Disjunction(label, result);
//						}
//
//						filter(result);
//					}
//				} else if (dd.getSource() == DragData.Source.COLUMN) {
//					// This event is never triggered when the drag is ended.
//					// It's not a huge deal, as this is only used to
//					// reinitialise the currently-dragged slot in ColumnControl.
//					// The other main consequence of this is that we can't
//					// assert to check if the slot has been cleared when starting a drag-swap.
//				} else if (dd.getSource() == DragData.Source.LABEL_TAB) {
//					filter(new Qualifier("label", dd.getEntityName()));
//				} else if (dd.getSource() == DragData.Source.ASSIGNEE_TAB) {
//					filter(new Qualifier("assignee", dd.getEntityName()));
//				} else if (dd.getSource() == DragData.Source.MILESTONE_TAB) {
//					filter(new Qualifier("milestone", dd.getEntityName()));
//				}
//
//			}
//			e.setDropCompleted(success);
//			e.consume();
//		});
//	}

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
			UI.status.clear();
		}
		catch (ParseException ex) {
			this.applyFilterExpression(EMPTY);
			// Overrides message in status bar
			UI.status.displayMessage("Panel " + (columnIndex + 1) + ": Parse error in filter: " + ex.getMessage());
		}
	}

	private void applyFilterExpression(FilterExpression filter) {
		currentFilterExpression = filter;
		applyCurrentFilterExpression();
		refreshItems();
	}

	/**
	 * Same as applyFilterExpression, but does not call refreshItems or change the
	 * current filter. Meant to be called from refreshItems() so as not to go into
	 * infinite mutual recursion.
	 */
	private void applyCurrentFilterExpression() {
		predicate = issue -> Qualifier.process(model, currentFilterExpression, issue);
		Qualifier.processMetaQualifierEffects(currentFilterExpression, qualifier -> {
			if (qualifier.getName().equals("repo") && qualifier.getContent().isPresent()) {
				ui.logic.openRepository(qualifier.getContent().get());
			}
		});
	}

	// An odd workaround for the above problem: serialising, then
	// immediately parsing a filter expression, just so the update can be
	// triggered through the text contents of the input area changing.

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
					// TODO re-enable
//					TurboIssue clone = new TurboIssue(issue);
					currentFilterExpression.applyTo(issue, model);
					if (updateModel) {
						// TODO re-enable
//						dragAndDropExecutor.executeCommand(CommandType.EDIT_ISSUE, models, clone, issue);
					}
					parentColumnControl.refresh();
				} else {
					throw new QualifierApplicationException(
						"Could not apply predicate " + currentFilterExpression + ".");
				}
			} catch (QualifierApplicationException ex) {
				UI.status.displayMessage(ex.getMessage());
			}
		}
	}
	
	public TransformationList<TurboIssue, TurboIssue> getIssueList() {
		return transformedIssueList;
	}

	public void setItems(List<TurboIssue> items) {
		this.issues = FXCollections.observableArrayList(items);
		refreshItems();
	}

	@Override
	public void close() {
		ui.unregisterEvent(onModelUpdate);
	}


	@Override
	public void refreshItems() {
		applyCurrentFilterExpression();
		transformedIssueList = new FilteredList<>(issues, predicate);
	}
}
