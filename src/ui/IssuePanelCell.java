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
//	private final Model model;
	private final int parentColumnIndex;
		
	public IssuePanelCell(Stage mainStage, Model model, IssuePanel parent, int parentColumnIndex) {
		super();
//		this.mainStage = mainStage;
//		this.model = model;
		this.parentColumnIndex = parentColumnIndex;
	}

	@Override
	public void updateItem(TurboIssue issue, boolean empty) {
		super.updateItem(issue, empty);
		if (issue == null)
			return;
		
		setGraphic(new IssuePanelCard(issue));
		setAlignment(Pos.CENTER);
		
//		registerEvents(issue);
		
		setOnDragDetected((event) -> {
			Dragboard db = startDragAndDrop(TransferMode.MOVE);
			ClipboardContent content = new ClipboardContent();
			IssuePanelDragData dd = new IssuePanelDragData(parentColumnIndex, issue.getId());
			content.putString(dd.serialise());
			db.setContent(content);
			event.consume();
		});
		
		setOnDragDone((event) -> {
//			if (event.getTransferMode() == TransferMode.MOVE) {
//			}
			event.consume();
		});
	}

//	private void registerEvents(TurboIssue issue) {
//		WeakReference<TurboIssue> issueRef = new WeakReference<TurboIssue>(issue);
//		setOnMouseClicked((MouseEvent mouseEvent) -> {
//			if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
//				if (mouseEvent.getClickCount() == 2) {
////					onDoubleClick(issueRef.get());
//				}
//			}
//		});
//	}

//	private void onDoubleClick(TurboIssue issue) {
//		TurboIssue oldIssue = new TurboIssue(issue);
//		TurboIssue modifiedIssue = new TurboIssue(issue);
//		(new IssueDialog(mainStage, model, modifiedIssue)).show().thenApply(
//				response -> {
//					if (response.equals("done")) {
//						model.updateIssue(oldIssue, modifiedIssue);
//					}
//					return true;
//				}).exceptionally(ex -> {
//					ex.printStackTrace();
//					return false;
//				});
//	}
}
