package ui.issuepanel;

import static ui.components.KeyboardShortcuts.DEFAULT_SIZE_WINDOW;
import static ui.components.KeyboardShortcuts.JUMP_TO_FILTER_BOX;
import static ui.components.KeyboardShortcuts.MAXIMIZE_WINDOW;
import static ui.components.KeyboardShortcuts.MINIMIZE_WINDOW;
import static ui.components.KeyboardShortcuts.SWITCH_BOARD;

import filter.expression.QualifierType;
import ui.components.PanelMenuBar;
import backend.interfaces.IModel;
import backend.resource.TurboIssue;
import backend.resource.TurboUser;
import filter.ParseException;
import filter.Parser;
import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.input.KeyEvent;
import ui.TestController;
import ui.UI;
import ui.components.FilterTextField;
import util.events.*;
import util.events.testevents.UIComponentFocusEvent;
import prefs.PanelInfo;

import java.util.ArrayList;
import java.util.Collections;
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

    private ObservableList<TurboIssue> issuesToDisplay = null;

    public PanelMenuBar panelMenuBar;
    protected FilterTextField filterTextField;
    private final UI ui;

    protected FilterExpression currentFilterExpression = Qualifier.EMPTY;

    public FilterPanel(UI ui, IModel model, PanelControl parentPanelControl, int panelIndex) {
        super(model, parentPanelControl, panelIndex);
        this.ui = ui;

        panelMenuBar = new PanelMenuBar(this, model, ui);
        getChildren().addAll(panelMenuBar, createFilterBox());
        setUpEventHandler();
        focusedProperty().addListener((unused, wasFocused, isFocused) -> {
            if (isFocused) {
                getStyleClass().add("panel-focused");
            } else {
                getStyleClass().remove("panel-focused");
            }
        });
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (MAXIMIZE_WINDOW.match(event)) {
                ui.maximizeWindow();
            } else if (MINIMIZE_WINDOW.match(event)) {
                ui.minimizeWindow();
            } else if (DEFAULT_SIZE_WINDOW.match(event)) {
                ui.setDefaultWidth();
            } else if (SWITCH_BOARD.match(event)) {
                ui.getMenuControl().switchBoard();
            } else if (JUMP_TO_FILTER_BOX.match(event)) {
                setFocusToFilterBox();
            }
        });
    }
    
    private void setFocusToFilterBox() {
        if (TestController.isTestMode()) {
            ui.triggerEvent(new UIComponentFocusEvent(UIComponentFocusEvent.EventType.FILTER_BOX));
        }
        filterTextField.requestFocus();
        filterTextField.setText(filterTextField.getText().trim());
        filterTextField.positionCaret(filterTextField.getLength());
    }

    private void setUpEventHandler() {
        this.setOnMouseClicked(e-> {
            ui.triggerEvent(new PanelClickedEvent(this.panelIndex));
            requestFocus();
        });
    }

    private final ModelUpdatedEventHandler onModelUpdate = e -> {

        // Update keywords
        List<String> all = new ArrayList<>(QualifierType.getCompletionKeywords());
        all.addAll(e.model.getUsers().stream()
            .map(TurboUser::getLoginName)
            .collect(Collectors.toList()));

        // Ensure that completions appear in lexicographical order
        Collections.sort(all);

        filterTextField.setCompletionKeywords(all);
    };

    private Node createFilterBox() {
        filterTextField = new FilterTextField("")
                .setOnConfirm((text) -> {
                    applyStringFilter(text);
                    return text;
                })
                .setOnCancel(this::requestFocus);
        filterTextField.setId(model.getDefaultRepo() + "_col" + panelIndex + "_filterTextField");
        filterTextField.setMinWidth(388);
        filterTextField.setMaxWidth(388);

        ui.registerEvent(onModelUpdate);

        filterTextField.setOnMouseClicked(e -> ui.triggerEvent(new PanelClickedEvent(panelIndex)));

        HBox layout = new HBox();
        layout.getChildren().addAll(filterTextField);
        layout.setPadding(new Insets(0, 0, 3, 0));

        setupPanelDragEvents(layout);

        return layout;
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
        ui.updateTitle(parentPanelControl.hasUnsavedPanels());
    }

    public void setFilterByString(String filterString) {
        filterTextField.setFilterText(filterString);
    }

    public FilterExpression getCurrentFilterExpression() {
        return currentFilterExpression;
    }
    
    public void restorePanel(String name, String filterString) {
        filterTextField.setFilterText(filterString);
        panelMenuBar.setPanelName(name);
    }

    public void startRename(){
        panelMenuBar.initRenameableTextFieldAndEvents();
    }

    public void setPanelName(String newName) {
        panelMenuBar.setPanelName(newName);
        ui.updateTitle(parentPanelControl.hasUnsavedPanels());
    }
    
    public PanelInfo getCurrentInfo() {
        return new PanelInfo(this.panelMenuBar.getPanelName(), filterTextField.getText());
    }

    public ObservableList<TurboIssue> getIssueList() {
        return issuesToDisplay;
    }

    public Text getNameText() {
        return this.panelMenuBar.getNameText();
    }

    public FilterTextField getFilterTextField() {
        return this.filterTextField;
    }

    public Label getRenameButton() {
        return this.panelMenuBar.getRenameButton();
    }

    public Label getCloseButton() {
        return this.panelMenuBar.getCloseButton();
    }

    public void setIssueList(List<TurboIssue> transformedIssueList) {
        this.issuesToDisplay = FXCollections.observableArrayList(transformedIssueList);
    }

    public void updatePanel(List<TurboIssue> filteredAndSortedIssues) {
        setIssueList(filteredAndSortedIssues);
        refreshItems();
    }

    @Override
    public void close() {
        ui.unregisterEvent(onModelUpdate);
    }
}
