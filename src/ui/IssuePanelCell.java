package ui;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;

public class IssuePanelCell extends ListCell<TurboIssue> {

//	private final Stage mainStage;
	private final Model model;
	private final int parentColumnIndex;
	private SidePanel sidePanel;
	private ColumnControl parentColumnControl;
		
	public IssuePanelCell(Stage mainStage, Model model, IssuePanel parent, int parentColumnIndex, SidePanel sidePanel, ColumnControl parentColumnControl) {
		super();
//		this.mainStage = mainStage;
		this.model = model;
		this.parentColumnIndex = parentColumnIndex;
		this.sidePanel = sidePanel;
		this.parentColumnControl = parentColumnControl;
	}

	@Override
	public void updateItem(TurboIssue issue, boolean empty) {
		super.updateItem(issue, empty);
		if (issue == null)
			return;
		
		setGraphic(new IssuePanelCard(issue));
		setAlignment(Pos.CENTER);
		getStyleClass().add("top-borders");
		
//		registerEvents(issue);
		
		setOnDragDetected((event) -> {
			Dragboard db = startDragAndDrop(TransferMode.MOVE);
			ClipboardContent content = new ClipboardContent();
			DragData dd = new DragData(DragData.Source.ISSUE_CARD, parentColumnIndex, issue.getId());
			content.putString(dd.serialise());
			db.setContent(content);
			event.consume();
		});
		
		setOnDragDone((event) -> {
//			if (event.getTransferMode() == TransferMode.MOVE) {
//			}
			event.consume();
		});
		setContextMenu(new IssuePanelContextMenu(model, sidePanel, parentColumnControl, issue.getId()).get());
	}
}
