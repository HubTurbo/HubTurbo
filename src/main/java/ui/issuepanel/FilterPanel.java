package ui.issuepanel;

import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import backend.resource.TurboUser;
import filter.ParseException;
import filter.Parser;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import javafx.collections.transformation.TransformationList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import ui.UI;
import ui.components.FilterTextField;
import util.events.ModelUpdatedEventHandler;
import util.events.PanelClickedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An FilterPanel is a AbstractPanel meant for containing issues and an accompanying filter text field,
 * which specifies the issues to be contained within as well as their order.
 *
 * The FilterPanel does not perform the filtering itself - it merely specifies how filtering is to be done.
 *
 * The FilterPanel also does not specify how the list is to be displayed -- subclasses override methods
 * which determine that.
 */
public abstract class FilterPanel extends AbstractPanel {

    private TransformationList<TurboIssue, TurboIssue> transformedIssueList = null;
    protected FilterTextField filterTextField;
    private UI ui;

    protected FilterExpression currentFilterExpression = Qualifier.EMPTY;

    public FilterPanel(UI ui, IModel model, PanelControl parentPanelControl, int panelIndex) {
        super(model, parentPanelControl, panelIndex);
        this.ui = ui;
        getChildren().add(createFilterBox());
        this.setOnMouseClicked(e-> {
            ui.triggerEvent(new PanelClickedEvent(panelIndex));
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
        List<String> all = new ArrayList<>(Qualifier.KEYWORDS);
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
        filterTextField.setId(model.getDefaultRepo() + "_col" + panelIndex + "_filterTextField");

        ui.registerEvent(onModelUpdate);

        filterTextField.setOnMouseClicked(e -> ui.triggerEvent(new PanelClickedEvent(panelIndex)));

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
        Label closeList = new Label(CLOSE_PANEL);
        closeList.setId(model.getDefaultRepo() + "_col" + panelIndex + "_closeButton");
        closeList.getStyleClass().add("label-button");
        closeList.setOnMouseClicked((e) -> {
            e.consume();
            parentPanelControl.closePanel(panelIndex);
        });

        return new Label[] { closeList };
    }

    private void setupPanelDragEvents(Node dropNode) {
        dropNode.setOnDragEntered(e -> {
                if (parentPanelControl.getCurrentlyDraggedPanelIndex() != panelIndex) {
                    // Apparently the dragboard can't be updated while
                    // the drag is in progress. This is why we use an
                    // external source for updates.
                    assert parentPanelControl.getCurrentlyDraggedPanelIndex() != -1;
                    int previous = parentPanelControl.getCurrentlyDraggedPanelIndex();
                    parentPanelControl.setCurrentlyDraggedPanelIndex(panelIndex);
                    parentPanelControl.swapPanels(previous, panelIndex);
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
            UI.status.displayMessage("Panel " + (panelIndex + 1)
                + ": Parse error in filter: " + ex.getMessage());
        }
    }

    /**
     * Triggered after pressing ENTER in the filter box.
     *
     * @param filter The current filter text in the filter box.
     */
    private void applyFilterExpression(FilterExpression filter) {
        currentFilterExpression = filter;

        parentPanelControl.getGUIController().panelFilterExpressionChanged(this);
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

    public void setIssueList(TransformationList<TurboIssue, TurboIssue> transformedIssueList) {
        this.transformedIssueList = transformedIssueList;
    }

    @Override
    public void close() {
        ui.unregisterEvent(onModelUpdate);
    }

}
