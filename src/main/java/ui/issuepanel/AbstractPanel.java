package ui.issuepanel;

import backend.interfaces.IModel;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import ui.DragData;

/**
 * A AbstractPanel is a JavaFX node that is contained by a PanelControl.
 * It is in charge of displaying arbitrary content and provides
 * functionality for being added, removed, and reordered (via dragging).
 *
 * Since objects of this class are JavaFX nodes, content can be displayed
 * simply by adding child nodes to them.
 */
public abstract class AbstractPanel extends VBox {

    public static final int PANEL_WIDTH = 400;

    public static final String CLOSE_PANEL = "\u2716";
    public static final String RENAME_PANEL = "\u270E";

    protected final IModel model;
    protected final PanelControl parentPanelControl;
    protected int panelIndex;

    public AbstractPanel(IModel model, PanelControl parentPanelControl, int panelIndex) {
        this.model = model;
        this.parentPanelControl = parentPanelControl;
        this.panelIndex = panelIndex;

        setupPanel();
        setupPanelDragEvents();
    }

    private void setupPanel() {
        setPrefWidth(PANEL_WIDTH);
        setMinWidth(PANEL_WIDTH);
        setPadding(new Insets(5));
        getStyleClass().addAll("borders", "rounded-borders");
    }

    private void setupPanelDragEvents() {
        setOnDragEntered(e -> {
            if (e.getDragboard().hasString()) {
                DragData dd = DragData.deserialise(e.getDragboard().getString());
                if (dd.getPanelIndex() != panelIndex) {
                    getStyleClass().add("dragged-over");
                }
            }
            e.consume();
        });

        setOnDragExited(e -> {
            getStyleClass().remove("dragged-over");
            e.consume();
        });

        setOnDragDetected((event) -> {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            DragData dd = new DragData(DragData.Source.PANEL, -1, -1);
            content.putString(dd.serialise());
            db.setContent(content);
            // We're using this because the content of a dragboard can't be changed
            // while the drag is in progress; this seemed like the simplest workaround
            parentPanelControl.setCurrentlyDraggedPanelIndex(panelIndex);
            event.consume();
        });

        setOnDragDone(Event::consume);
    }

    /**
     * To be called by PanelControl in order to have indices updated.
     * Should not be called externally.
     */
    void updateIndex(int updated) {
        panelIndex = updated;
    }

    /**
     * To be overridden by subclasses.
     */

    /**
     * This method is called when the item list is to be refreshed. This mainly happens
     * when the user selects Refresh from the menu. Subclasses may also require it.
     * Currently implemented by ListPanel to re-render the list of IssuePanelCards.
     *
     * @param hasMetadata implies the issues to be filtered have metadata inside,
     *                    and thus should be displayed and sorted accordingly.
     *                    Currently it indicates whether the IssuePanelCards will
     *                    show metadata details.
     */
    public abstract void refreshItems(boolean hasMetadata);

    /**
     * This method is called when the panel control is deselected. It used to happen when
     * the issue panel was closed.
     */
    public abstract void close();
}
