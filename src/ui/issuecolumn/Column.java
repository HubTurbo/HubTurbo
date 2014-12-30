package ui.issuecolumn;

import javafx.geometry.Insets;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Model;
import ui.DragData;

import command.TurboCommandExecutor;

/**
 * A Column is a JavaFX node that is contained by a ColumnControl.
 * It is in charge of displaying arbitrary content and provides functionality
 * for being reordered via dragging.
 */
public abstract class Column extends VBox {
	
	public static final int COLUMN_WIDTH = 400;
	
	public static final String CLOSE_COLUMN = "\u2716";
	
	protected final Model model;
	protected final ColumnControl parentColumnControl;
	protected int columnIndex;
	private boolean isSearchPanel = false;
	
	protected TurboCommandExecutor dragAndDropExecutor;

	public Column(Stage mainStage, Model model, ColumnControl parentColumnControl, int columnIndex, TurboCommandExecutor dragAndDropExecutor, boolean isSearchPanel) {
		this.model = model;
		this.parentColumnControl = parentColumnControl;
		this.columnIndex = columnIndex;
		this.dragAndDropExecutor = dragAndDropExecutor;
		this.isSearchPanel = isSearchPanel;
		
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
		
		setOnDragDone((event) -> {
//			if (event.getTransferMode() == TransferMode.MOVE) {
//			}
			event.consume();
		});
	}
	
	public boolean isSearchPanel() {
		return isSearchPanel;
	}
	
	/**
	 * To be called by ColumnControl in order to update indices.
	 * Should not be called externally.
	 */
	void updateIndex(int updated) {
		columnIndex = updated;
	}

	/**
	 * To be overridden by subclasses
	 */
	
	public abstract void refreshItems();
	public abstract void deselect();
}
