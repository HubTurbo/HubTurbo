import java.util.concurrent.CompletableFuture;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NewIssueDialog {

	public static final String STYLE_YELLOW = "-fx-background-color: #FFFA73;";
	public static final String STYLE_BORDERS = "-fx-border-color: #000000; -fx-border-width: 1px;";

	Stage parentStage;
	CompletableFuture<String> response;

	public NewIssueDialog(Stage parentStage) {
		this.parentStage = parentStage;

		response = new CompletableFuture<>();
	}
	
	public CompletableFuture<String> show() {
		showDialog();
		return response;
	}
	
	private Node createIssueEntry(String text) {
		HBox layout = new HBox();

		Label issue = new Label();
		issue.setText(text);

		Label x = new Label();
		x.setText("x");
		x.setStyle(STYLE_BORDERS);
		x.setOnMouseClicked((MouseEvent) -> {
			// remove the node
		});

		layout.getChildren().add(issue);
		layout.getChildren().add(x);
		layout.setStyle(STYLE_BORDERS);

		layout.setPrefSize(200, 30);
		layout.setMaxSize(200, 30);
		layout.setMinSize(200, 30);

		return layout;
	}

	private void showDialog() {

		VBox layout = new VBox();
		layout.setPadding(new Insets(15, 12, 15, 12));
		layout.setSpacing(10);

		Scene secondScene = new Scene(layout, 800 - 250, 500);

		Stage secondStage = new Stage();
		secondStage.setTitle("Create New Issue");
		secondStage.setScene(secondScene);
		
		secondStage.requestFocus();
		
		TextField issueName = new TextField();
		issueName.setPromptText("Title");

		TextArea issueDesc = new TextArea();
		issueDesc.setPromptText("Description");

		Label milestoneLabel = new Label("Milestone:");

		FlowPane milestone = new FlowPane();
		milestone.setPrefSize(300, 60);
		milestone.setStyle(STYLE_BORDERS);
		
		Label flowLabel = new Label("Labels:");

		FlowPane labels = new FlowPane();
		// labels.setMinSize(300, 100);
		labels.setPrefSize(300, 100);
		labels.setStyle(STYLE_BORDERS);

		labels.setOnDragOver((event) -> {
			if (event.getGestureSource() != labels
					&& event.getDragboard().hasString()) {
				event.acceptTransferModes(TransferMode.COPY);
			}

			event.consume();
		});

		labels.setOnDragEntered((event) -> {
			if (event.getGestureSource() != labels
					&& event.getDragboard().hasString()) {
				Dragboard db = event.getDragboard();
				if (db.hasString()) {
					DragData dd = DragData.deserialize(db.getString());
					if (dd.source == DragSource.TREE_LABELS) {
						labels.setStyle(STYLE_YELLOW);
					}
				}
			}
			event.consume();
		});

		labels.setOnDragExited((event) -> {
			labels.setStyle(STYLE_BORDERS);

			event.consume();
		});

		labels.setOnDragDropped((event) -> {
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasString()) {
				success = true;

				DragData dd = DragData.deserialize(db.getString());

				if (dd.source == DragSource.TREE_LABELS) {
					labels.getChildren().add(createIssueEntry(dd.text.substring(0, 4) + "..."));
				}
			}
			event.setDropCompleted(success);

			event.consume();
		});
		labels.setPadding(new Insets(15, 12, 15, 12));
		labels.setHgap(10);
		labels.setVgap(5);

		Label assignedToLabel = new Label("Assigned to:");

		FlowPane assignedTo = new FlowPane();
		// assignedTo.setMinSize(300, 100);
		assignedTo.setPrefSize(300, 100);
		assignedTo.setStyle(STYLE_BORDERS);

		HBox buttons = new HBox();
		buttons.setAlignment(Pos.BASELINE_RIGHT);
		Button cancel = new Button();
		cancel.setText("Cancel");
		cancel.setOnMouseClicked((MouseEvent e) -> {
//			System.out.println("cancel");
			response.complete("cancel");
			secondStage.close();
		});

		Button ok = new Button();
		ok.setText("OK");
		ok.setOnMouseClicked((MouseEvent e) -> {
//			System.out.println("ok");
			response.complete("ok");
			secondStage.close();
		});
		HBox.setMargin(ok, new Insets(0, 12, 0, 0)); // top right bottom left

		buttons.getChildren().add(ok);
		buttons.getChildren().add(cancel);

		ObservableList<Node> children = layout.getChildren();
		children.add(issueName);
		children.add(issueDesc);
		children.add(milestoneLabel);
		children.add(milestone);
		children.add(flowLabel);
		children.add(labels);
		children.add(assignedToLabel);
		children.add(assignedTo);
		children.add(buttons);

		// StackPane secondaryLayout = new StackPane();
		// secondaryLayout.getChildren().add(secondLabel);


		secondStage.initOwner(parentStage);
		// This is in case i want it to block input to parent windows
		// secondStage.initModality(Modality.WINDOW_MODAL);
		// APPLICATION_MODAL is another alternative

		// Set position of second window, related to primary window.
		secondStage.setX(parentStage.getX() + 200);
		secondStage.setY(parentStage.getY() + 50);

		secondStage.show();
	}
}
