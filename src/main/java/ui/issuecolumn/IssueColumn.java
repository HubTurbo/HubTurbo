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

    // Collection-related

//    private ObservableList<TurboIssue> issues = FXCollections.observableArrayList();

    // Filter-related

    private TransformationList<TurboIssue, TurboIssue> transformedIssueList = null;
    protected FilterTextField filterTextField;
    private UI ui;

    protected FilterExpression currentFilterExpression = Qualifier.EMPTY;
//    private Predicate<TurboIssue> predicate = p -> true;
//    private Comparator<TurboIssue> comparator = (a, b) -> 0;

    public IssueColumn(UI ui, IModel model, ColumnControl parentColumnControl, int columnIndex) {
        super(model, parentColumnControl, columnIndex);
        this.ui = ui;
        getChildren().add(createFilterBox());
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
    };

    private Node createFilterBox() {
        filterTextField = new FilterTextField("", 0)
                .setOnConfirm((text) -> {
                    applyStringFilter(text);
                    return text;
                })
                .setOnCancel(this::requestFocus);
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
        } catch (ParseException ex) {
            this.applyFilterExpression(Qualifier.EMPTY);
            // Overrides message in status bar
            UI.status.displayMessage("Panel " + (columnIndex + 1)
                + ": Parse error in filter: " + ex.getMessage());
        }
    }

    /**
     * Triggered after pressing ENTER in the filter box. Therefore, the hasMetadata call
     * is false.
     *
     * @param filter The current filter text in the filter box
     */
    private void applyFilterExpression(FilterExpression filter) {
        currentFilterExpression = filter;
//        refreshItems(false);

        // NEW
        parentColumnControl.getGUIController().columnFilterExpressionChanged(this);
    }

    /**
     * Same as applyFilterExpression, but does not call refreshItems or change the
     * current filter. Meant to be called from refreshItems() so as not to go into
     * infinite mutual recursion.
     */
//    private void applyCurrentFilterExpression(boolean isSortableByNonSelfUpdates) {
//        predicate = issue -> Qualifier.process(model, currentFilterExpression, issue);
//        comparator = Qualifier.getSortComparator(model, "id", true, false);
//
//        // BiConsumer is used here as we need to update the comparator, and at the same time call
//        // openRepository() if necessary.
//        Qualifier.processMetaQualifierEffects(currentFilterExpression, (qualifier, metaQualifierInfo) -> {
//            if (qualifier.getContent().isPresent() && qualifier.getName().equals(Qualifier.REPO)) {
//                // Even when the qualifier is wrapped by a negation ("-repo:HubTurbo/HubTurbo"),
//                // repo is still opened.
//                ui.logic.openRepository(qualifier.getContent().get());
//            } else if (qualifier.getName().equals(Qualifier.UPDATED)
//                    && !currentFilterExpression.getQualifierNames().contains(Qualifier.SORT)) {
//                // no sort order specified, implicitly assumed to sort by last-non-self-update
//                comparator = Qualifier.getSortComparator(model, "nonSelfUpdate", true, true);
//            } else if (qualifier.getName().equals(Qualifier.SORT)) {
//                comparator = qualifier.getCompoundSortComparator(model, isSortableByNonSelfUpdates);
//            }
//        });
//    }

    // An odd workaround for the above problem: serialising, then
    // immediately parsing a filter expression, just so the update can be
    // triggered through the text contents of the input area changing.

//    public void filter(FilterExpression filterExpr) {
//        filterByString(filterExpr.toString());
//    }

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

//    public void setItems(List<TurboIssue> items, boolean hasMetadata) {
//        this.issues = FXCollections.observableArrayList(items);
//        refreshItems(hasMetadata);
//    }

    // RELEVANT
    public void setIssueList(TransformationList<TurboIssue, TurboIssue> transformedIssueList) {
        this.transformedIssueList = transformedIssueList;
    }

    @Override
    public void close() {
        ui.unregisterEvent(onModelUpdate);
    }

    @Override
    public void refreshItems(boolean hasMetadata) {
//        boolean hasUpdatedQualifier = currentFilterExpression.getQualifierNames().contains(Qualifier.UPDATED);
//
//        // Transforms predicate and comparator to the desired values
//        // If hasUpdatedQualifier && hasMetadata, we allow sorting by non-self update times (if the user
//        // specifies the updated and/or sort qualifiers)
//        applyCurrentFilterExpression(hasUpdatedQualifier && hasMetadata);
//
//        // Then use them to filter & sort.
//        transformedIssueList = new SortedList<>(new FilteredList<>(issues, predicate), comparator);
//
//        // If the updated qualifier is specified, after initiallly filtering with hasMetadata as false
//        // (which, since we don't have metadata, filters by getUpdatedAt) we trigger getIssueMetadata,
//        // which will eventually return a model with metadata and trigger this method again with
//        // hasMetadata as true. However, since we already have metadata this time, we don't trigger
//        // getIssueMetadata here again.
//        if (hasUpdatedQualifier && !hasMetadata) {
//            // Group all filtered issues by repo, then trigger updates for each group
//            transformedIssueList.stream()
//                .collect(Collectors.groupingBy(TurboIssue::getRepoId))
//                .entrySet().forEach(repoSetEntry ->
//                ui.logic.getIssueMetadata(repoSetEntry.getKey(),
//                    repoSetEntry.getValue().stream()
//                        .map(TurboIssue::getId)
//                        .collect(Collectors.toList())));
//        }
    }
}
