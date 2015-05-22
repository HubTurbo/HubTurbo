package ui.issuecolumn;

import backend.interfaces.IModel;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import ui.DragData;

/**
 * A Column is a JavaFX node that is contained by a ColumnControl.
 * It is in charge of displaying arbitrary content and provides
 * functionality for being added, removed, and reordered (via dragging).
 * 
 * Since objects of this class are JavaFX nodes, content can be displayed 
 * simply by adding child nodes to them.
 */
public abstract class Column extends VBox {
	
	public static final int COLUMN_WIDTH = 400;
	
	public static final String CLOSE_COLUMN = "\u2716";
	
	protected final IModel model;
	protected final ColumnControl parentColumnControl;
	protected int columnIndex;
	
	public Column(IModel model, ColumnControl parentColumnControl, int columnIndex) {
		this.model = model;
		this.parentColumnControl = parentColumnControl;
		this.columnIndex = columnIndex;

		setupColumn();
		setupColumnDragEvents();
	}

	private void setupColumn() {
		setPrefWidth(COLUMN_WIDTH);
		setMinWidth(COLUMN_WIDTH);
		setPadding(new Insets(5));
		getStyleClass().addAll("borders", "rounded-borders");
	}

	private void setupColumnDragEvents() {
		setOnDragEntered(e -> {
			if (e.getDragboard().hasString()) {
				DragData dd = DragData.deserialise(e.getDragboard().getString());
				if (dd.getColumnIndex() != columnIndex) {
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
			DragData dd = new DragData(DragData.Source.COLUMN, -1, -1);
			content.putString(dd.serialise());
			db.setContent(content);
			// We're using this because the content of a dragboard can't be changed
			// while the drag is in progress; this seemed like the simplest workaround
			parentColumnControl.setCurrentlyDraggedColumnIndex(columnIndex);
			event.consume();
		});
		
		setOnDragDone(Event::consume);
	}
	
	/**
	 * To be called by ColumnControl in order to have indices updated.
	 * Should not be called externally.
	 */
	void updateIndex(int updated) {
		columnIndex = updated;
	}

	/**
	 * To be overridden by subclasses.
	 */
	
	/**
	 * This method is called when the item list is to be refreshed. This mainly happens
	 * when the user selects Refresh from the menu. Subclasses may also require it.
	 */
	public abstract void refreshItems();
	
	/**
	 * This method is called when the column control is deselected. It used to happen when
	 * the issue panel was closed.
	 */
	public abstract void close();
}
