package ui;

import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Model;

public class ManageLabelsDialog implements Dialog<String> {

	Stage parentStage;
	Model model;

	CompletableFuture<String> response;

	public ManageLabelsDialog(Stage parentStage, Model model) {
		this.parentStage = parentStage;
		this.model = model;

		response = new CompletableFuture<>();
	}

	public CompletableFuture<String> show() {
		showDialog();
		return response;
	}

	private void showDialog() {

		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		
		Scene scene = new Scene(layout, 300, 400);

		Stage stage = new Stage();
		stage.setTitle("Manage labels");
		stage.setScene(scene);

		Platform.runLater(() -> stage.requestFocus());

		layout.getChildren().addAll(createTreeView());

		stage.initOwner(parentStage);
		// secondStage.initModality(Modality.APPLICATION_MODAL);

		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY());

		stage.show();
	}

	private Node createTreeView() {
		return null;
	}
}
