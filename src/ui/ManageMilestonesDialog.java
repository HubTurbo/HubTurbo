package ui;

import java.util.concurrent.CompletableFuture;

import model.Model;
import model.TurboMilestone;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ManageMilestonesDialog implements Dialog<String> {

	private final Stage parentStage;
	private final Model model;
	
	CompletableFuture<String> response;

	public ManageMilestonesDialog(Stage stage, Model model) {
		this.parentStage = stage;
		this.model = model;
		
		response = new CompletableFuture<>();
	}
	
	@Override
	public CompletableFuture<String> show() {
		showDialog();
		return response;
	}


	private void showDialog() {

		VBox layout = new VBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		
		Scene scene = new Scene(layout, 330, 400);

		Stage stage = new Stage();
		stage.setTitle("Manage milestones");
		stage.setScene(scene);

		Platform.runLater(() -> stage.requestFocus());
		
		layout.getChildren().addAll(createListView(stage), createButtons(stage));

		stage.initOwner(parentStage);
		// secondStage.initModality(Modality.APPLICATION_MODAL);

		stage.setX(parentStage.getX());
		stage.setY(parentStage.getY());

		stage.show();
	}

	private Node createListView(Stage stage) {
		ListView<TurboMilestone> listView = new ListView<>();
		
		listView.setItems(model.getMilestones());
		
		listView.setCellFactory(new Callback<ListView<TurboMilestone>, ListCell<TurboMilestone>>() {
			@Override
			public ListCell<TurboMilestone> call(ListView<TurboMilestone> list) {
				return new ManageMilestonesListCell(stage, model);
			}
		});
		
		return listView;
	}

	private Node createButtons(Stage stage) {
		Button close = new Button("Close");
		close.setOnAction(e -> stage.close());

		HBox container = new HBox();
		container.setAlignment(Pos.CENTER_RIGHT);
		container.getChildren().add(close);
		
		return container;
	}
}
