package ui;

import java.lang.ref.WeakReference;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Model;
import model.TurboIssue;
import model.TurboUser;

public class IssuePanelCell extends ListCell<TurboIssue> {

//	private final Stage mainStage;
//	private final Model model;
	private final int parentColumnIndex;
	
	private ChangeListener<String> titleChangeListener;
	
	public IssuePanelCell(Stage mainStage, Model model, IssuePanel parent, int parentColumnIndex) {
		super();
//		this.mainStage = mainStage;
//		this.model = model;
		this.parentColumnIndex = parentColumnIndex;
	}

	private ChangeListener<String> createIssueTitleListener(TurboIssue issue, Text issueName){
		WeakReference<TurboIssue> issueRef = new WeakReference<TurboIssue>(issue);
		titleChangeListener = new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> stringProperty,
					String oldValue, String newValue) {
				TurboIssue issue = issueRef.get();
				if(issue != null){
					issueName.setText("#" + issue.getId() + " " + newValue);
				}
			}
		};
		
		return titleChangeListener;
	}
	@Override
	public void updateItem(TurboIssue issue, boolean empty) {
		super.updateItem(issue, empty);
		if (issue == null)
			return;
		
		Text issueTitle = new Text("#" + issue.getId() + " " + issue.getTitle());
		issueTitle.setWrappingWidth(330);
		issueTitle.getStyleClass().add("issue-panel-name");
		if (!issue.getOpen()) issueTitle.getStyleClass().add("issue-panel-closed");
		issue.titleProperty().addListener(new WeakChangeListener<String>(createIssueTitleListener(issue, issueTitle)));

		LabelDisplayBox labels = new LabelDisplayBox(issue.getLabelsReference(), false, "");

		IssueIndicatorsDisplayBox indicators = new IssueIndicatorsDisplayBox(issue, false);
		
		TurboUser assignee = issue.getAssignee();
		HBox rightAlignBox = new HBox();
		rightAlignBox.setAlignment(Pos.BASELINE_RIGHT);
		HBox.setHgrow(rightAlignBox, Priority.ALWAYS);
		if (assignee != null) {
			Label assigneeName = new Label(assignee.getGithubName());
			assigneeName.getStyleClass().add("display-box-padding");
			rightAlignBox.getChildren().addAll(assigneeName);
		}

		HBox leftAlignBox = new HBox();
		leftAlignBox.setAlignment(Pos.BASELINE_LEFT);
		HBox.setHgrow(leftAlignBox, Priority.ALWAYS);
		leftAlignBox.getChildren().add(indicators);

		HBox bottom = new HBox();
		bottom.setSpacing(5);
		bottom.getChildren().add(leftAlignBox);
		if (assignee != null) bottom.getChildren().add(rightAlignBox);
		
		VBox issueCard = new VBox();
		issueCard.setMaxWidth(350);
		issueCard.getStyleClass().addAll("borders", "rounded-borders");
		issueCard.setPadding(new Insets(5,5,5,5));
		issueCard.setSpacing(5);
		issueCard.getChildren().addAll(issueTitle, labels, bottom);

		setGraphic(issueCard);
		
		setAlignment(Pos.CENTER);
		
		registerEvents(issue);
		
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

	private void registerEvents(TurboIssue issue) {
//		WeakReference<TurboIssue> issueRef = new WeakReference<TurboIssue>(issue);
//		setOnMouseClicked((MouseEvent mouseEvent) -> {
//			if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
//				if (mouseEvent.getClickCount() == 2) {
////					onDoubleClick(issueRef.get());
//				}
//			}
//		});
	}

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
