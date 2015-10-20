package ui.issuepanel;

import static ui.components.KeyboardShortcuts.DEFAULT_SIZE_WINDOW;
import static ui.components.KeyboardShortcuts.JUMP_TO_FILTER_BOX;
import static ui.components.KeyboardShortcuts.MAXIMIZE_WINDOW;
import static ui.components.KeyboardShortcuts.MINIMIZE_WINDOW;
import static ui.components.KeyboardShortcuts.SWITCH_BOARD;
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
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import ui.TestController;
import ui.UI;
import ui.components.FilterTextField;
import ui.components.PanelNameTextField;
import util.events.*;
import util.events.testevents.UIComponentFocusEvent;
import prefs.PanelInfo;

import java.util.ArrayList;
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

    protected HBox panelMenuBar;
    protected Text nameText;
    protected HBox nameBox;
    protected HBox menuBarNameArea;
    protected HBox menuBarCloseButton;
    protected Label renameButton;
    protected PanelNameTextField renameTextField;
    protected FilterTextField filterTextField;
    
    private String panelName = "Panel";
    private static final int NAME_FIELD_WIDTH = PANEL_WIDTH - 15;
    private static final int NAME_DISPLAY_WIDTH = PANEL_WIDTH - 70;
    private UI ui;

    protected FilterExpression currentFilterExpression = Qualifier.EMPTY;

    public FilterPanel(UI ui, IModel model, PanelControl parentPanelControl, int panelIndex) {
        super(model, parentPanelControl, panelIndex);
        this.ui = ui;
        
        getChildren().addAll(createPanelMenuBar(), createFilterBox());
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
        List<String> all = new ArrayList<>(Qualifier.KEYWORDS);
        all.addAll(e.model.getUsers().stream()
            .map(TurboUser::getLoginName)
            .collect(Collectors.toList()));

        filterTextField.setKeywords(all);
    };
    
    private Node createPanelMenuBar() {
        menuBarNameArea = createNameArea();
        menuBarCloseButton = createCloseButton();
        
        panelMenuBar = new HBox();
        panelMenuBar.setSpacing(2);
        panelMenuBar.setMinWidth(PANEL_WIDTH);
        panelMenuBar.setMaxWidth(PANEL_WIDTH);
        panelMenuBar.getChildren().addAll(menuBarNameArea, menuBarCloseButton);
        panelMenuBar.setPadding(new Insets(0, 0, 8, 0));
        return panelMenuBar;
    }
    
    private HBox createNameArea() {
        HBox nameArea = new HBox();
        
        nameText = new Text(panelName);
        nameText.setId(model.getDefaultRepo() + "_col" + panelIndex + "_nameText");
        
        nameBox = new HBox();
        nameBox.getChildren().add(nameText);
        nameBox.setMinWidth(NAME_DISPLAY_WIDTH);
        nameBox.setMaxWidth(NAME_DISPLAY_WIDTH);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        
        nameBox.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 2) {
                    mouseEvent.consume();
                    activateInplaceRename();
                }
            }
        });
        
        renameButton = new Label(RENAME_PANEL);
        renameButton.getStyleClass().add("label-button");
        renameButton.setId(model.getDefaultRepo() + "_col" + panelIndex + "_renameButton");
        renameButton.setOnMouseClicked(e -> {
            e.consume();
            activateInplaceRename();
        });
        
        HBox renameBox = new HBox();
        renameBox.getChildren().add(renameButton);
        renameBox.setAlignment(Pos.TOP_RIGHT);
        
        nameArea.getChildren().addAll(nameBox, renameButton);
        nameArea.setMinWidth(360);
        nameArea.setMaxWidth(360);
        return nameArea;
    }

    private HBox createCloseButton() {
        HBox closeArea = new HBox();
        
        Label closeButton = new Label(CLOSE_PANEL);
        closeButton.setId(model.getDefaultRepo() + "_col" + panelIndex + "_closeButton");
        closeButton.getStyleClass().add("label-button");
        closeButton.setOnMouseClicked((e) -> {
            e.consume();
            parentPanelControl.closePanel(panelIndex);
        });
        
        closeArea.getChildren().add(closeButton);

        return closeArea;
    }

    private Node createFilterBox() {
        filterTextField = new FilterTextField("", 0)
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
    }

    public void filterByString(String filterString) {
        filterTextField.setFilterText(filterString);
    }

    public FilterExpression getCurrentFilterExpression() {
        return currentFilterExpression;
    }
    
    public void restorePanel(String name, String filterString) {
        filterTextField.setFilterText(filterString);
        this.panelName = name;
        this.nameText.setText(panelName);
    }
    
    private void activateInplaceRename() {
        ui.triggerEvent(new ShowRenamePanelEvent(this.panelIndex));
    }
    
    public void showRenameTextField() {
        renameTextField = new PanelNameTextField(panelName, this);
        renameTextField.setId(model.getDefaultRepo() + "_col" + panelIndex + "_renameTextField");
        renameTextField.setMaxWidth(NAME_FIELD_WIDTH);
        renameTextField.setMinWidth(NAME_FIELD_WIDTH);
        menuBarNameArea.getChildren().removeAll(nameBox, renameButton);
        panelMenuBar.getChildren().remove(menuBarCloseButton);
        menuBarNameArea.getChildren().addAll(renameTextField);
    }
    
    public void setPanelName(String newName) {
        this.panelName = newName;
        nameText.setText(newName);
    }
    
    public void closeRenameTextField(PanelNameTextField renameTextField) {
        menuBarNameArea.getChildren().remove(renameTextField);
        panelMenuBar.getChildren().add(menuBarCloseButton);
        menuBarNameArea.getChildren().addAll(nameBox, renameButton);
        this.requestFocus();
    }
    
    public String getCurrentName() {
        return this.panelName;
    }

    public String getCurrentFilterString() {
        return filterTextField.getText();
    }
    
    public PanelInfo getCurrentInfo() {
        return new PanelInfo(this.panelName, filterTextField.getText());
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
