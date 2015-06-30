package ui.issuecolumn;

import backend.interfaces.IModel;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import prefs.Preferences;
import ui.GUIController;
import ui.UI;
import ui.components.KeyboardShortcuts;
import ui.listpanel.ListPanel;
import util.events.ColumnClickedEvent;
import util.events.ColumnClickedEventHandler;
import util.events.IssueSelectedEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class PanelControl extends HBox {

    private final UI ui;
    private final Preferences prefs;
    private IModel model;
    private GUIController guiController;
    private Optional<Integer> currentlySelectedColumn = Optional.empty();

    public PanelControl(UI ui, Preferences prefs) {
        this.ui = ui;
        this.prefs = prefs;

        setSpacing(10);
        setPadding(new Insets(0, 10, 0, 10));

        ui.registerEvent((IssueSelectedEventHandler) e ->
            setCurrentlySelectedColumn(Optional.of(e.columnIndex)));
        ui.registerEvent((ColumnClickedEventHandler) e ->
            setCurrentlySelectedColumn(Optional.of(e.columnIndex)));

        setupKeyEvents();
    }

    /**
     * Called on login.
     */
    public void init(GUIController guiController) {
        this.guiController = guiController;
        restoreColumns();
    }

    public void updateModel(IModel newModel) {
        model = newModel;
    }

    public void saveSession() {
        List<String> sessionFilters = new ArrayList<>();
        getChildren().forEach(child -> {
            if (child instanceof FilterPanel) {
                String filter = ((FilterPanel) child).getCurrentFilterString();
                sessionFilters.add(filter);
            }
        });
        prefs.setLastOpenFilters(sessionFilters);
    }

    public void restoreColumns() {
        getChildren().clear();

        List<String> filters = prefs.getLastOpenFilters();

        if (filters.isEmpty()) {
            addColumn();
            return;
        }

        for (String filter : filters) {
            addColumn().filterByString(filter);
        }
    }

    public void forEach(Consumer<AbstractPanel> callback) {
        getChildren().forEach(child -> callback.accept((AbstractPanel) child));
    }

    /**
     * For a quick refresh (without requesting updates)
     */
    public void refresh() {
        forEach(child -> child.refreshItems(true));
    }

    private FilterPanel addColumn() {
        return addColumnAt(getChildren().size());
    }

    public FilterPanel addColumnAt(int index) {
        FilterPanel panel = new ListPanel(ui, model, this, index);
        getChildren().add(index, panel);

        // Populates the panel with the default repo issues.
        guiController.columnFilterExpressionChanged(panel);

        updateColumnIndices();
        setCurrentlySelectedColumn(Optional.of(index));
        return panel;
    }

    private void setCurrentlySelectedColumn(Optional<Integer> selectedColumn) {
        currentlySelectedColumn = selectedColumn;
        updateCSSforColumns();
    }

    private void updateCSSforColumns() {
        if (currentlySelectedColumn.isPresent()) {
            for (int index = 0; index < getChildren().size(); index++) {
                getColumn(index).getStyleClass().remove("panel-focused");
            }
            getColumn(currentlySelectedColumn.get()).getStyleClass().add("panel-focused");
        }
    }

    public AbstractPanel getColumn(int index) {
        return (AbstractPanel) getChildren().get(index);
    }

    public void closeAllColumns() {
        getChildren().clear();
        // There aren't any children left, so we don't need to update indices
    }

    public void openColumnsWithFilters(List<String> filters) {
        for (String filter : filters) {
            FilterPanel column = addColumn();
            column.filterByString(filter);
        }
    }

    public void closeColumn(int index) {
        Node child = getChildren().remove(index);
        updateColumnIndices();
        ((AbstractPanel) child).close();
    }

    private void updateColumnIndices() {
        int i = 0;
        for (Node c : getChildren()) {
            ((AbstractPanel) c).updateIndex(i++);
        }
    }

    public void createNewPanelAtStart() {
        addColumnAt(0);
    }

    public void createNewPanelAtEnd() {
        addColumn();
    }

    public void swapColumns(int columnIndex, int columnIndex2) {
        AbstractPanel one = getColumn(columnIndex);
        AbstractPanel two = getColumn(columnIndex2);
        one.updateIndex(columnIndex2);
        two.updateIndex(columnIndex);
        // This method of swapping is used because Collections.swap
        // will assign one child without removing the other, causing
        // a duplicate child exception. HBoxes are constructed because
        // null also causes an exception.
        getChildren().set(columnIndex, new HBox());
        getChildren().set(columnIndex2, new HBox());
        getChildren().set(columnIndex, two);
        getChildren().set(columnIndex2, one);
    }

    public Optional<Integer> getCurrentlySelectedColumn() {
        return currentlySelectedColumn;
    }

    // For dragging purposes
    private int currentlyDraggedColumnIndex = -1;
    public int getCurrentlyDraggedColumnIndex() {
        return currentlyDraggedColumnIndex;
    }
    public void setCurrentlyDraggedColumnIndex(int i) {
        currentlyDraggedColumnIndex = i;
    }

    public void closeCurrentColumn() {
        if (currentlySelectedColumn.isPresent()) {
            int columnIndex = currentlySelectedColumn.get();
            closeColumn(columnIndex);
            if (getChildren().size() == 0) {
                setCurrentlySelectedColumn(Optional.empty());
            } else {
                int newColumnIndex = (columnIndex > getChildren().size() - 1)
                                     ? columnIndex - 1
                                     : columnIndex;
                setCurrentlySelectedColumn(Optional.of(newColumnIndex));
                getColumn(currentlySelectedColumn.get()).requestFocus();
            }
        }
    }

    public double getPanelWidth() {
        // COLUMN_WIDTH is used instead of
        // ((AbstractPanel) getChildren().get(0)).getWidth();
        // because when this function is called, columns may not have been sized yet.
        // In any case actual column width is COLUMN_WIDTH at minimum, so we can assume
        // that they are that large.
        return 40 + AbstractPanel.COLUMN_WIDTH;
    }
    private void setupKeyEvents() {
        addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyboardShortcuts.RIGHT_PANEL || event.getCode() == KeyboardShortcuts.LEFT_PANEL) {
                handleKeys(event.getCode() == KeyboardShortcuts.RIGHT_PANEL);
                assert currentlySelectedColumn.isPresent() : "handleKeys doesn't set selectedIndex!";
            }
        });
    }

    private void handleKeys(boolean isForwardKey) {
        if (!currentlySelectedColumn.isPresent()) {
            return;
        }
        if (getChildren().size() == 0) {
            return;
        }
        AbstractPanel selectedPanel = getColumn(currentlySelectedColumn.get());
        if (selectedPanel instanceof FilterPanel){
            if (((FilterPanel) selectedPanel).filterTextField.isFocused()){
                return;
            } else {
                int newIndex = currentlySelectedColumn.get() + (isForwardKey ? 1 : -1);
                if (newIndex < 0) {
                    newIndex = getChildren().size() - 1;
                } else if (newIndex > getChildren().size() - 1) {
                    newIndex = 0;
                }
                setCurrentlySelectedColumn(Optional.of(newIndex));
                selectedPanel = getColumn(currentlySelectedColumn.get());
                selectedPanel.requestFocus();
            }
        }
        ui.triggerEvent(new ColumnClickedEvent(currentlySelectedColumn.get()));
        scrollandShowColumn(currentlySelectedColumn.get(), getChildren().size());
    }

    private void scrollandShowColumn(int selectedColumnIndex, int numOfColumns) {
        ui.getMenuControl().scrollTo(selectedColumnIndex, numOfColumns);
    }

    public GUIController getGUIController() {
        return guiController;
    }

    public int getNumberOfColumns() {
        return getChildren().size();
    }

    public int getNumberOfSavedBoards() {
        return prefs.getAllBoards().size();
    }
}
