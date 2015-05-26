package ui.issuepanel;

import backend.interfaces.IModel;
import backend.resource.Model;
import backend.resource.TurboIssue;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import ui.DragData;

import java.util.HashSet;
import java.util.Optional;

public class IssuePanelCell extends ListCell<TurboIssue> {

	private final IModel model;
	private final int parentColumnIndex;
	private final IssuePanel parent;
	private final HashSet<Integer> issuesWithNewComments;
		
	public IssuePanelCell(IModel model, IssuePanel parent,
	                      int parentColumnIndex, HashSet<Integer> issuesWithNewComments) {
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
		if (issue == null) { return; }
		Optional<Model> currentModel = model.getModelById(issue.getRepoId());
		assert currentModel.isPresent() : "Invalid repo id " + issue.getRepoId() + " for issue " + issue.getId();
		setGraphic(new IssuePanelCard(currentModel.get(), issue, parent, issuesWithNewComments));
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
		
		setOnDragDone(Event::consume);
		
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
//					Optional<TurboLabel> label = model.getLabelByActualName(dd.getEntityName());
//					assert label.isPresent();
//					(new TurboIssueAddLabels(model, issue, Arrays.asList(label.get()))).execute();
				} else if (dd.getSource() == DragData.Source.ASSIGNEE_TAB) {
//					Optional<TurboUser> user = model.getUserByLogin(dd.getEntityName());
//					assert user.isPresent();
//					(new TurboIssueSetAssignee(model, issue, user.get())).execute();
				} else if (dd.getSource() == DragData.Source.MILESTONE_TAB) {
//					Optional<TurboMilestone> milestone = model.getMilestoneByTitle(dd.getEntityName());
//					assert milestone.isPresent();
//					(new TurboIssueSetMilestone(model, issue, milestone.get())).execute();
				}
			}
			e.setDropCompleted(success);
	
			e.consume();
		});
	}
}
