package ui;

import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GroupDialog implements Dialog<String> {
	
	private final Stage parentStage;

	private CompletableFuture<String> response;

	private String generatedName;
	private boolean exclusive;

	public GroupDialog(Stage parentStage, String generatedName, boolean exclusive) {
		this.parentStage = parentStage;
		this.generatedName = generatedName;
		this.exclusive = exclusive;

		response = new CompletableFuture<>();
	}
	
	public CompletableFuture<String> show() {
		showDialog();
		return response;
	}

	private void showDialog() {

		VBox layout = new VBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		
		Scene scene = new Scene(layout, 330, 90);

		Stage stage = new Stage();
		stage.setTitle("New Group");
		stage.setScene(scene);
		stage.setOnCloseRequest(e -> {
			response.complete("ok");
		});

		Platform.runLater(() -> stage.requestFocus());
		
		Button close = new Button("Close");
		close.setOnAction(e -> {
			response.complete("ok");
			stage.close();
		});
		HBox buttonContainer = new HBox();
		buttonContainer.setAlignment(Pos.CENTER_RIGHT);
		buttonContainer.getChildren().add(close);
		HBox.setHgrow(buttonContainer, Priority.ALWAYS);
		
		CheckBox exclusivityCheckbox = new CheckBox("Exclusive");
		exclusivityCheckbox.setSelected(exclusive);
		HBox checkBoxContainer = new HBox();
		checkBoxContainer.setAlignment(Pos.CENTER_LEFT);
		checkBoxContainer.getChildren().add(exclusivityCheckbox);
		HBox.setHgrow(checkBoxContainer, Priority.ALWAYS);
		
		HBox bottomContainer = new HBox();
		bottomContainer.getChildren().addAll(checkBoxContainer, buttonContainer);
		
		TextField groupNameField = new TextField();
		groupNameField.setText(generatedName);
		
		layout.getChildren().addAll(groupNameField, bottomContainer);

		stage.initOwner(parentStage);
//		 secondStage.initModality(Modality.APPLICATION_MODAL);

		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY());

		stage.show();
	}
}
