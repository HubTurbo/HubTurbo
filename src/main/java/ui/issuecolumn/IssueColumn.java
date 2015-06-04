package ui.issuecolumn;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import backend.resource.TurboUser;
import filter.ParseException;
import filter.Parser;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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
import java.util.Comparator;
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
	protected FilterTextField filterTextField;
	private UI ui;

	private FilterExpression currentFilterExpression = Qualifier.EMPTY;
	private Predicate<TurboIssue> predicate = p -> true;
	private Comparator<TurboIssue> comparator = (a, b) -> 0;

	// This controls whether a metadata update will be triggered on the next refresh.
	// It's toggled to false by model updates that are not supposed to trigger updates.
	// It's toggled to true each time a refresh DOES NOT trigger an update.
	private boolean triggerMetadataUpdate = true;

	public IssueColumn(UI ui, IModel model, ColumnControl parentColumnControl, int columnIndex) {
		super(model, parentColumnControl, columnIndex);
		this.ui = ui;
		getChildren().add(createFilterBox());
//		setupIssueColumnDragEvents(model, columnIndex);
		this.setOnMouseClicked(e-> {
			ui.triggerEvent(new ColumnClickedEvent(columnIndex));
			requestFocus();
		});
		focusedProperty().addListener((unused, wasFocused, isFocused) -> {
            if (isFocused) {
                getStyleClass().add("panel-focused");
            } else {
                getStyleClass().remove("panel-focused");
            }
        });
	}

	private final ModelUpdatedEventHandler onModelUpdate = e -> {

		// Update keywords
		List<String> all = new ArrayList<>(Arrays.asList(Qualifier.KEYWORDS));
		all.addAll(e.model.getUsers().stream()
			.map(TurboUser::getLoginName)
			.collect(Collectors.toList()));

		filterTextField.setKeywords(all);

		// Make metadata update state consistent
		if (!e.triggerMetadataUpdate) {
			this.triggerMetadataUpdate = false;
		}
	};

	private Node createFilterBox() {
		filterTextField = new FilterTextField("", 0).setOnConfirm((text) -> {
			applyStringFilter(text);
			return text;
		});
		filterTextField.setId(model.getDefaultRepo() + "_col" + columnIndex + "_filterTextField");

		ui.registerEvent(onModelUpdate);

		filterTextField.setOnMouseClicked(e -> ui.triggerEvent(new ColumnClickedEvent(columnIndex)));

		HBox buttonsBox = new HBox();
		buttonsBox.setSpacing(5);
		buttonsBox.setAlignment(Pos.TOP_RIGHT);
		buttonsBox.setMinWidth(50);
		buttonsBox.getChildren().addAll(createButtons());

		HBox layout = new HBox();
		layout.getChildren().addAll(filterTextField, buttonsBox);
		layout.setPadding(new Insets(0, 0, 3, 0));

		setupPanelDragEvents(layout);

		return layout;
	}

	private Label[] createButtons() {
		Label closeList = new Label(CLOSE_COLUMN);
		closeList.setId(model.getDefaultRepo() + "_col" + columnIndex + "_closeButton");
		closeList.getStyleClass().add("label-button");
		closeList.setOnMouseClicked((e) -> {
			e.consume();
			parentColumnControl.closeColumn(columnIndex);
		});

		return new Label[] { closeList };
	}

	private void setupPanelDragEvents(Node dropNode) {
		dropNode.setOnDragEntered(e -> {
				if (parentColumnControl.getCurrentlyDraggedColumnIndex() != columnIndex) {
					// Apparently the dragboard can't be updated while
					// the drag is in progress. This is why we use an
					// external source for updates.
					assert parentColumnControl.getCurrentlyDraggedColumnIndex() != -1;
					int previous = parentColumnControl.getCurrentlyDraggedColumnIndex();
					parentColumnControl.setCurrentlyDraggedColumnIndex(columnIndex);
					parentColumnControl.swapColumns(previous, columnIndex);
				}
				e.consume();
			}
		);

		dropNode.setOnDragExited(e -> {
			dropNode.getStyleClass().remove("dragged-over");
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
				this.applyFilterExpression(Qualifier.EMPTY);
			}

			// Clear displayed message on successful filter
			UI.status.clear();
		} catch (ParseException ex) {
			this.applyFilterExpression(Qualifier.EMPTY);
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
		comparator = Qualifier.getSortComparator(model, "id", true);
		Qualifier.processMetaQualifierEffects(currentFilterExpression, (qualifier, metaQualifierInfo) -> {
			if (qualifier.getContent().isPresent() && qualifier.getName().equals(Qualifier.REPO)) {
				ui.logic.openRepository(qualifier.getContent().get());
			} else if (qualifier.getName().equals(Qualifier.SORT)) {
				comparator = qualifier.getCompoundSortComparator(model);
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

		transformedIssueList = new SortedList<>(new FilteredList<>(issues, predicate), comparator);

		if (!triggerMetadataUpdate) {
			triggerMetadataUpdate = true;
		} else if (currentFilterExpression.getQualifierNames().contains(Qualifier.UPDATED)) {
			// Group all filtered issues by repo, then trigger updates for each group
			transformedIssueList.stream()
				.collect(Collectors.groupingBy(TurboIssue::getRepoId))
				.entrySet().forEach(e ->
				ui.logic.getIssueMetadata(e.getKey(),
					e.getValue().stream()
						.map(TurboIssue::getId)
						.collect(Collectors.toList())));
		}
	}
}
