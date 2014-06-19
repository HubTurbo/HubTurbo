package ui;

import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.TurboIssue;

public class IssueDialog {

	private static final double HEIGHT_FACTOR = 0.3;

	private static final int TITLE_SPACING = 5;
	private static final int ELEMENT_SPACING = 10;
	private static final int MIDDLE_SPACING = 20;
	
	public static final String STYLE_YELLOW = "-fx-background-color: #FFFA73;";
	public static final String STYLE_BORDERS = "-fx-border-color: #000000; -fx-border-width: 1px;";

	Stage parentStage;
	TurboIssue issue;

	CompletableFuture<String> response;

	public IssueDialog(Stage parentStage, TurboIssue issue) {
		this.parentStage = parentStage;
		this.issue = issue;

		response = new CompletableFuture<>();
	}

	public CompletableFuture<String> show() {
		showDialog();
		return response;
	}

	private Parent left() {

		HBox title = new HBox();
		title.setAlignment(Pos.BASELINE_LEFT);
		title.setSpacing(TITLE_SPACING);

		Label issueId = new Label("#" + issue.getId());
		TextField issueTitle = new TextField(issue.getTitle());
		issueTitle.setPromptText("Title");
		issueTitle.textProperty().addListener((observable, oldValue, newValue) -> {
			issue.setTitle(newValue);
		});
		title.getChildren().addAll(issueId, issueTitle);

		TextArea issueDesc = new TextArea(issue.getDescription());
		issueDesc.setPrefRowCount(5);
		issueDesc.setPrefColumnCount(42);
		issueDesc.setPromptText("Description");
		issueDesc.textProperty().addListener((observable, oldValue, newValue) -> {
			issue.setDescription(newValue);
		});

		VBox left = new VBox();
		left.setSpacing(ELEMENT_SPACING);
		left.getChildren().addAll(title, issueDesc);

		return left;

	}

	private Parent right(Stage stage) {

		TextField milestoneField = new TextField();
		milestoneField.setPromptText("Milestone");

		TextField labelsField = new TextField();
		labelsField.setPromptText("Labels");

		TextField assigneeField = new TextField();
		assigneeField.setPromptText("Assignee");

		HBox buttons = new HBox();
		buttons.setAlignment(Pos.BASELINE_RIGHT);

		Button cancel = new Button();
		cancel.setText("Cancel");
		cancel.setOnMouseClicked((MouseEvent e) -> {
			response.complete("cancel");
			stage.close();
		});

		Button ok = new Button();
		ok.setText("OK");
		ok.setOnMouseClicked((MouseEvent e) -> {
			response.complete("ok");
			stage.close();
		});
		HBox.setMargin(ok, new Insets(0, 12, 0, 0)); // top right bottom left

		buttons.getChildren().addAll(ok, cancel);

		VBox right = new VBox();
		right.setSpacing(ELEMENT_SPACING);
		right.getChildren().addAll(milestoneField, labelsField, assigneeField,
				buttons);

		return right;
	}

	private void showDialog() {

		// TODO bind changes to the issue directly?
		// TODO make text field read only until a button is pressed

		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(MIDDLE_SPACING);

		Scene scene = new Scene(layout, parentStage.getWidth(), parentStage.getHeight() * HEIGHT_FACTOR);

		Stage stage = new Stage();
		stage.setTitle("Issue #" + issue.getId() + ": " + issue.getTitle());
		stage.setScene(scene);

		Platform.runLater(() -> stage.requestFocus());

		layout.getChildren().addAll(left(), right(stage));

		stage.initOwner(parentStage);
		// secondStage.initModality(Modality.APPLICATION_MODAL);

		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY() + parentStage.getHeight() * (1 - HEIGHT_FACTOR));

		stage.show();
	}
}
