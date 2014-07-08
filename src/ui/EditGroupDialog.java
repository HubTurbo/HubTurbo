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

public class EditGroupDialog implements Dialog<TurboLabelGroup> {
	
	private final Stage parentStage;

	private CompletableFuture<TurboLabelGroup> response;

	private String generatedName;
	private boolean exclusive;
	
	private boolean showExclusiveCheckbox = true;

	public EditGroupDialog(Stage parentStage, TurboLabelGroup group) {
		this.parentStage = parentStage;
		this.generatedName = group.getValue();
		this.exclusive = group.isExclusive();

		response = new CompletableFuture<>();
	}
	
	public EditGroupDialog setExclusiveCheckboxEnabled(boolean visible) {
		showExclusiveCheckbox = visible;
		return this;
	}
	
	public CompletableFuture<TurboLabelGroup> show() {
		showDialog();
		return response;
	}

	private void showDialog() {

		VBox layout = new VBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		
		Scene scene = new Scene(layout, 330, 90);

		Stage stage = new Stage();

		Platform.runLater(() -> stage.requestFocus());
		
		Button close = new Button("Close");
		HBox buttonContainer = new HBox();
		buttonContainer.setAlignment(Pos.CENTER_RIGHT);
		buttonContainer.getChildren().add(close);
		HBox.setHgrow(buttonContainer, Priority.ALWAYS);
		
		CheckBox checkbox = new CheckBox("Exclusive");
		checkbox.setSelected(exclusive);
		checkbox.setDisable(!showExclusiveCheckbox);
				
		HBox checkBoxContainer = new HBox();
		checkBoxContainer.setAlignment(Pos.CENTER_LEFT);
		checkBoxContainer.getChildren().add(checkbox);
		HBox.setHgrow(checkBoxContainer, Priority.ALWAYS);
		
		HBox bottomContainer = new HBox();
		bottomContainer.getChildren().addAll(checkBoxContainer, buttonContainer);
		
		TextField groupNameField = new TextField();
		groupNameField.setText(generatedName);
		
		layout.getChildren().addAll(groupNameField, bottomContainer);

		stage.setTitle("New Group");
		stage.setScene(scene);
//		stage.setOnCloseRequest(e -> {
//		});
		stage.initOwner(parentStage);
//		 secondStage.initModality(Modality.APPLICATION_MODAL);

		close.setOnAction(e -> {
			respond(groupNameField.getText(), showExclusiveCheckbox ? checkbox.isSelected() : exclusive);
			stage.close();
		});

		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY());

		stage.show();
	}

	private void respond(String name, boolean exclusive) {
		TurboLabelGroup res = new TurboLabelGroup(name);
		res.setExclusive(exclusive);
		response.complete(res);
	}
}
