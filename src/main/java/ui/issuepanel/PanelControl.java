package ui.issuepanel;

import backend.interfaces.IModel;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import prefs.Preferences;
import prefs.PanelInfo;
import ui.GUIController;
import ui.UI;
import ui.components.KeyboardShortcuts;
import ui.listpanel.ListPanel;
import util.events.IssueSelectedEventHandler;
import util.events.PanelClickedEvent;
import util.events.PanelClickedEventHandler;
import util.events.ShowRenamePanelEventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class PanelControl extends HBox {

    private final UI ui;
    private final Preferences prefs;
    private IModel model;
    private GUIController guiController;
    private Optional<Integer> currentlySelectedPanel = Optional.empty();

    public PanelControl(UI ui, Preferences prefs, Stage stage) {
        this.ui = ui;
        this.prefs = prefs;
        
        setSpacing(10);
        setPadding(new Insets(0, 10, 0, 10));

        ui.registerEvent((IssueSelectedEventHandler) e ->
                setCurrentlySelectedPanel(Optional.of(e.panelIndex)));
        ui.registerEvent((PanelClickedEventHandler) e ->
                setCurrentlySelectedPanel(Optional.of(e.panelIndex)));
        ui.registerEvent((ShowRenamePanelEventHandler) e -> Platform.runLater(() -> ui.renamePanel(e.panelId)));

        setupKeyEvents();
    }

    /**
     * Called on login.
     */
    public void init(GUIController guiController) {
        this.guiController = guiController;
        restorePanels();
    }

    public void updateModel(IModel newModel) {
        model = newModel;
    }

    public void saveSession() {
        List<PanelInfo> panels = new ArrayList<>();
        getChildren().forEach(child -> {
            if (child instanceof FilterPanel) {
                PanelInfo panel = ((FilterPanel) child).getCurrentInfo();
                panels.add(panel);
            }
        });
        prefs.setPanelInfo(panels);
    }

    public void restorePanels() {
        getChildren().clear();

        List<PanelInfo> panels = prefs.getPanelInfo();

        if (panels.isEmpty()) {
            addPanel();
            return;
        }

        for (PanelInfo panel : panels) {
            addPanel().restorePanel(panel.getPanelName(), panel.getPanelFilter());
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

    private FilterPanel addPanel() {
        return addPanelAt(getChildren().size());
    }

    public FilterPanel addPanelAt(int index) {
        FilterPanel panel = new ListPanel(ui, model, this, index);
        getChildren().add(index, panel);

        // Populates the panel with the default repo issues.
        guiController.panelFilterExpressionChanged(panel);

        updatePanelIndices();
        setCurrentlySelectedPanel(Optional.of(index));
        return panel;
    }

    private void setCurrentlySelectedPanel(Optional<Integer> selectedPanel) {
        currentlySelectedPanel = selectedPanel;
        updateCSSforPanels();
    }

    private void updateCSSforPanels() {
        if (currentlySelectedPanel.isPresent()) {
            for (int index = 0; index < getChildren().size(); index++) {
                getPanel(index).getStyleClass().remove("panel-focused");
            }
            getPanel(currentlySelectedPanel.get()).getStyleClass().add("panel-focused");
        }
    }

    public AbstractPanel getPanel(int index) {
        return (AbstractPanel) getChildren().get(index);
    }

    public void closeAllPanels() {
        getChildren().clear();
        // There aren't any children left, so we don't need to update indices
    }

    public void openPanels(List<PanelInfo> panels) {
        for (PanelInfo panel : panels) {
            FilterPanel filterPanel = addPanel();
            filterPanel.restorePanel(panel.getPanelName(), panel.getPanelFilter());
        }
    }

    public void closePanel(int index) {
        Node child = getChildren().remove(index);
        updatePanelIndices();
        ((AbstractPanel) child).close();
    }

    private void updatePanelIndices() {
        int i = 0;
        for (Node c : getChildren()) {
            ((AbstractPanel) c).updateIndex(i++);
        }
    }

    public void createNewPanelAtStart() {
        addPanelAt(0);
    }

    public void createNewPanelAtEnd() {
        addPanel();
    }

    public void swapPanels(int panelIndex, int panelIndex2) {
        AbstractPanel one = getPanel(panelIndex);
        AbstractPanel two = getPanel(panelIndex2);
        one.updateIndex(panelIndex2);
        two.updateIndex(panelIndex);
        // This method of swapping is used because Collections.swap
        // will assign one child without removing the other, causing
        // a duplicate child exception. HBoxes are constructed because
        // null also causes an exception.
        getChildren().set(panelIndex, new HBox());
        getChildren().set(panelIndex2, new HBox());
        getChildren().set(panelIndex, two);
        getChildren().set(panelIndex2, one);
    }
    /**
    public void showRenameDialog(int panelId) {
        FilterPanel panel = (FilterPanel) getPanel(panelId);
        String panelName = panel.getCurrentName();
        
        TextInputDialog renameDialog = new TextInputDialog(panelName);
        renameDialog.getEditor().setId("panelrenameinput");
        renameDialog.setTitle("Rename " + panelName);
        renameDialog.setHeaderText("Enter a new name for this panel.");
        Optional<String> result = renameDialog.showAndWait();
        mainStage.show();
        
        String newName = result.orElse(panelName);
        if (newName.equals("")) {
            newName = panelName;
        }
        if (newName.length() > MAX_NAME_LENGTH) {
            newName = newName.substring(0, MAX_NAME_LENGTH);
        }
        panel.renamePanel(newName);
    }
    **/

    public Optional<Integer> getCurrentlySelectedPanel() {
        return currentlySelectedPanel;
    }

    // For dragging purposes
    private int currentlyDraggedPanelIndex = -1;
    public int getCurrentlyDraggedPanelIndex() {
        return currentlyDraggedPanelIndex;
    }
    public void setCurrentlyDraggedPanelIndex(int i) {
        currentlyDraggedPanelIndex = i;
    }

    public void closeCurrentPanel() {
        if (currentlySelectedPanel.isPresent()) {
            int panelIndex = currentlySelectedPanel.get();
            closePanel(panelIndex);
            if (getChildren().size() == 0) {
                setCurrentlySelectedPanel(Optional.empty());
            } else {
                int newPanelIndex = (panelIndex > getChildren().size() - 1)
                                     ? panelIndex - 1
                                     : panelIndex;
                setCurrentlySelectedPanel(Optional.of(newPanelIndex));
                getPanel(currentlySelectedPanel.get()).requestFocus();
            }
        }
    }

    public double getPanelWidth() {
        // PANEL_WIDTH is used instead of
        // ((AbstractPanel) getChildren().get(0)).getWidth();
        // because when this function is called, panels may not have been sized yet.
        // In any case actual panel width is PANEL_WIDTH at minimum, so we can assume
        // that they are that large.
        return 40 + AbstractPanel.PANEL_WIDTH;
    }
    private void setupKeyEvents() {
        addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyboardShortcuts.RIGHT_PANEL || event.getCode() == KeyboardShortcuts.LEFT_PANEL) {
                handleKeys(event.getCode() == KeyboardShortcuts.RIGHT_PANEL);
                assert currentlySelectedPanel.isPresent() : "handleKeys doesn't set selectedIndex!";
            }
        });
    }

    private void handleKeys(boolean isForwardKey) {
        if (!currentlySelectedPanel.isPresent()) {
            return;
        }
        if (getChildren().size() == 0) {
            return;
        }
        AbstractPanel selectedPanel = getPanel(currentlySelectedPanel.get());
        if (selectedPanel instanceof FilterPanel){
            if (((FilterPanel) selectedPanel).filterTextField.isFocused()){
                return;
            } else {
                int newIndex = currentlySelectedPanel.get() + (isForwardKey ? 1 : -1);
                if (newIndex < 0) {
                    newIndex = getChildren().size() - 1;
                } else if (newIndex > getChildren().size() - 1) {
                    newIndex = 0;
                }
                setCurrentlySelectedPanel(Optional.of(newIndex));
                selectedPanel = getPanel(currentlySelectedPanel.get());
                selectedPanel.requestFocus();
            }
        }
        ui.triggerEvent(new PanelClickedEvent(currentlySelectedPanel.get()));
        scrollandShowPanel(currentlySelectedPanel.get(), getChildren().size());
    }

    private void scrollandShowPanel(int selectedPanelIndex, int numOfPanels) {
        ui.getMenuControl().scrollTo(selectedPanelIndex, numOfPanels);
    }

    public GUIController getGUIController() {
        return guiController;
    }

    public int getNumberOfPanels() {
        return getChildren().size();
    }

    public int getNumberOfSavedBoards() {
        return prefs.getAllBoards().size();
    }
}
