package ui;

import java.util.concurrent.CompletableFuture;

import model.Model;
import model.TurboMilestone;
import javafx.application.Platform;
import javafx.geometry.Insets;
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

	private static final String NEW_MILESTONE_NAME = "new-milestone";
	
	private final Stage parentStage;
	private final Model model;
	
	CompletableFuture<String> response;
	
	private ListView<TurboMilestone> listView;

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

		HBox layout = new HBox();
		layout.setPadding(new Insets(15));
		layout.setSpacing(10);
		
		Scene scene = new Scene(layout, 420, 400);

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
	
	public void refresh() {
		
		ManageMilestonesDialog that = this;
		
		listView.setCellFactory(new Callback<ListView<TurboMilestone>, ListCell<TurboMilestone>>() {
			@Override
			public ListCell<TurboMilestone> call(ListView<TurboMilestone> list) {
				return new ManageMilestonesListCell(model, that);
			}
		});

	}

	private Node createListView(Stage stage) {
		listView = new ListView<>();
		listView.setItems(model.getMilestones());
		
		refresh();
				
		return listView;
	}

	private Node createButtons(Stage stage) {
		Button create = new Button("Create Milestone");
		create.setOnAction(e -> {
			model.createMilestone(new TurboMilestone(NEW_MILESTONE_NAME));
		});

		Button close = new Button("Close");
		close.setOnAction(e -> stage.close());

		VBox container = new VBox();
		container.setSpacing(5);
		container.getChildren().addAll(create, close);
		
		return container;
	}
}
