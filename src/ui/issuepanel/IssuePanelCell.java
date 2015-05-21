package ui.issuepanel;

import java.util.Arrays;
import java.util.HashSet;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import model.Model;
import model.TurboIssue;
import ui.DragData;
import ui.UI;

import command.TurboIssueAddLabels;
import command.TurboIssueSetAssignee;
import command.TurboIssueSetMilestone;

public class IssuePanelCell extends ListCell<TurboIssue> {

	private final Model model;
	private final int parentColumnIndex;
	private final IssuePanel parent;
	private final HashSet<Integer> issuesWithNewComments;
		
	public IssuePanelCell(UI ui, Model model, IssuePanel parent, int parentColumnIndex, HashSet<Integer> issuesWithNewComments) {
		super();
		this.model = model;
		this.parent = parent;
		this.parentColumnIndex = parentColumnIndex;
		this.issuesWithNewComments = issuesWithNewComments;
		setAlignment(Pos.CENTER);
		getStyleClass().add("bottom-borders");
	}

	@Override
	public void updateItem(TurboIssue issue, boolean empty) {
		super.updateItem(issue, empty);
		if (issue == null)
			return;
		
		setGraphic(new IssuePanelCard(issue, parent, issuesWithNewComments));
		
//		setContextMenu(new IssuePanelContextMenu(model, sidePanel, parentColumnControl, issue).get());
		
		registerDragEvents(issue);
	}

	private void registerDragEvents(TurboIssue issue) {
		setOnDragDetected((event) -> {
			Dragboard db = startDragAndDrop(TransferMode.MOVE);
			ClipboardContent content = new ClipboardContent();
			DragData dd = new DragData(DragData.Source.ISSUE_CARD, parentColumnIndex, issue.getId());
			content.putString(dd.serialise());
			db.setContent(content);
			event.consume();
		});
		
		setOnDragDone((event) -> {
			event.consume();
		});
		
		setOnDragOver(e -> {
			if (e.getGestureSource() != this && e.getDragboard().hasString()) {
				DragData dd = DragData.deserialise(e.getDragboard().getString());
				if (dd.getSource() == DragData.Source.LABEL_TAB
					|| dd.getSource() == DragData.Source.ASSIGNEE_TAB
					|| dd.getSource() == DragData.Source.MILESTONE_TAB) {
					e.acceptTransferModes(TransferMode.COPY);
				}
			}
		});
	
		setOnDragEntered(e -> {
			if (e.getDragboard().hasString()) {
				DragData dd = DragData.deserialise(e.getDragboard().getString());
				if (dd.getSource() == DragData.Source.LABEL_TAB
					|| dd.getSource() == DragData.Source.ASSIGNEE_TAB
					|| dd.getSource() == DragData.Source.MILESTONE_TAB) {
					getStyleClass().add("dragged-over");
				}
			}
			e.consume();
		});
	
		setOnDragExited(e -> {
			getStyleClass().remove("dragged-over");
			e.consume();
		});

		setOnDragDropped(e -> {
			Dragboard db = e.getDragboard();
			boolean success = false;
	
			if (db.hasString()) {
				success = true;
				DragData dd = DragData.deserialise(db.getString());
				if (dd.getSource() == DragData.Source.LABEL_TAB) {
					(new TurboIssueAddLabels(model, issue, Arrays.asList(model.getLabelByGhName(dd.getEntityName())))).execute();
				} else if (dd.getSource() == DragData.Source.ASSIGNEE_TAB) {
					(new TurboIssueSetAssignee(model, issue, model.getUserByGhName(dd.getEntityName()))).execute();
				} else if (dd.getSource() == DragData.Source.MILESTONE_TAB) {
					(new TurboIssueSetMilestone(model, issue, model.getMilestoneByTitle(dd.getEntityName()))).execute();
				}
			}
			e.setDropCompleted(success);
	
			e.consume();
		});
	}
}
