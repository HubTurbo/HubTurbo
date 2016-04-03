package ui.issuepanel;

import filter.expression.FilterExpression;
import filter.expression.Qualifier;
import filter.expression.QualifierType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import prefs.Preferences;
import prefs.PanelInfo;
import ui.GUIController;
import ui.UI;
import ui.components.KeyboardShortcuts;
import ui.listpanel.ListPanel;
import util.events.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PanelControl extends HBox {

    private final UI ui;
    private final Stage mainStage;
    private final Preferences prefs;
    private ScrollPane panelsScrollPane;
    private GUIController guiController;
    private Optional<Integer> currentlySelectedPanel = Optional.empty();

    public PanelControl(UI ui, Stage mainStage, Preferences prefs) {
        this.ui = ui;
        this.mainStage = mainStage;
        this.prefs = prefs;

        setSpacing(10);
        setPadding(new Insets(0, 10, 0, 10));

        ui.registerEvent((IssueSelectedEventHandler) e ->
                setCurrentlySelectedPanel(Optional.of(e.panelIndex)));
        ui.registerEvent((PanelClickedEventHandler) e ->
                setCurrentlySelectedPanel(Optional.of(e.panelIndex)));
        ui.registerEvent((ShowRenamePanelEventHandler) e ->
                ((FilterPanel) getPanel(e.panelId)).startRename());

        setupKeyEvents();
    }

    /**
     * Called on login.
     */
    public void init(GUIController guiController, ScrollPane panelsScrollPane) {
        this.guiController = guiController;
        this.panelsScrollPane = panelsScrollPane;
        restorePanels();
        selectFirstPanel();
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
        forEach(child -> child.refreshItems());
    }

    public FilterPanel generatePanelWithNameAndFilter(String panelName, String filterName) {
        FilterPanel panelAdded = this.addPanelAt(this.getPanelCount());
        panelAdded.setPanelName(panelName);
        panelAdded.setFilterByString(filterName);
        return panelAdded;
    }

    private FilterPanel addPanel() {
        return addPanelAt(getChildren().size());
    }

    public FilterPanel addPanelAt(int index) {
        FilterPanel panel = new ListPanel(ui, mainStage, this, index);
        getChildren().add(index, panel);

        // Populates the panel with the default repo issues.
        guiController.panelFilterExpressionChanged(panel);

        updatePanelIndices();
        setCurrentlySelectedPanel(Optional.of(index));
        return panel;
    }

    public void selectPanel(int index) {
        if (getPanelCount() == 0) {
            return;
        }
        assert index >= 0 && index < getPanelCount();
        setCurrentlySelectedPanel(Optional.of(index));
        scrollToPanel(index);
        getPanel(index).requestFocus();
    }

    public void selectFirstPanel() {
        selectPanel(0);
    }

    public void selectLastPanel() {
        selectPanel(getPanelCount() - 1);
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
        updateFocus(index);

        UI.events.triggerEvent(new UsedReposChangedEvent());
    }

    private void updatePanelIndices() {
        int i = 0;
        for (Node c : getChildren()) {
            ((AbstractPanel) c).updateIndex(i++);
        }
    }

    public void createNewPanelAtStart() {
        addPanelAt(0).filterTextField.requestFocus();
    }

    public void createNewPanelAtEnd() {
        addPanel().filterTextField.requestFocus();
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
        }
    }

    public void updateFocus(int closedPanelIndex) {
        if (closedPanelIndex != currentlySelectedPanel.get()) {
            return;
        } else if (getChildren().size() == 0) {
            setCurrentlySelectedPanel(Optional.empty());
        } else {
            int newPanelIndex =
                    closedPanelIndex > getChildren().size() - 1
                            ? closedPanelIndex - 1
                            : closedPanelIndex;
            setCurrentlySelectedPanel(Optional.of(newPanelIndex));
            getPanel(currentlySelectedPanel.get()).requestFocus();
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
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (KeyboardShortcuts.rightPanel.match(event) || KeyboardShortcuts.leftPanel.match(event)) {
                handleKeys(KeyboardShortcuts.rightPanel.match(event));
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
        if (selectedPanel instanceof FilterPanel) {
            if (((FilterPanel) selectedPanel).filterTextField.isFocused()) {
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
        scrollToCurrentlySelectedPanel();
    }

    public void scrollToCurrentlySelectedPanel() {
        scrollToPanel(currentlySelectedPanel.get());
    }

    public GUIController getGUIController() {
        return guiController;
    }

    public int getPanelCount() {
        return getChildren().size();
    }

    public HashSet<String> getRepositoriesReferencedOnAllPanels() {
        HashSet<String> repositoriesOnPanels = new HashSet<>();

        for (int i = 0; i < getPanelCount(); i++) {
            AbstractPanel currPanel = getPanel(i);

            if (currPanel instanceof FilterPanel) {
                FilterPanel currFilterPanel = (FilterPanel) currPanel;
                FilterExpression panelExpression = currFilterPanel.getCurrentFilterExpression();
                repositoriesOnPanels.addAll(Qualifier.getMetaQualifierContent(panelExpression, QualifierType.REPO));
            }
        }

        return repositoriesOnPanels;
    }

    public int getNumberOfSavedBoards() {
        return prefs.getAllBoards().size();
    }

    public List<String> getAllBoardNames() {
        return prefs.getAllBoardNames();
    }

    private void scrollToPanel(int panelIndex) {
        setHvalue(panelIndex * (panelsScrollPane.getHmax()) / (getPanelCount() - 1));
    }

    private void setHvalue(double val) {
        panelsScrollPane.setHvalue(val);
    }

    /**
     * Returns the list of panel names and filters currently showing the user interface
     *
     * @return
     */
    public List<PanelInfo> getCurrentPanelInfos() {
        return getChildren().stream().flatMap(c -> {
            if (c instanceof FilterPanel) {
                return Stream.of(((FilterPanel) c).getCurrentInfo());
            } else {
                return Stream.of();
            }
        }).collect(Collectors.toList());
    }
}
