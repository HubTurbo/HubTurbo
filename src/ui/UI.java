package ui;

import util.GitHubClientExtended;
import util.ModelUpdater;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Model;

public class UI extends Application {

	public static final String STYLE_YELLOW_BORDERS = "-fx-background-color: #FFFA73; -fx-border-color: #000000; -fx-border-width: 1px;";
	public static final String STYLE_BORDERS_FADED = "-fx-border-color: #B2B1AE; -fx-border-width: 1px; -fx-border-radius: 3;";
	public static final String STYLE_BORDERS = "-fx-border-color: #000000; -fx-border-width: 1px;";
	public static final String STYLE_FADED = "-fx-text-fill: #B2B1AE;";

	private Stage mainStage;
	
	private ColumnControl columns;
	private MenuControl menu;
	
	private Model model;
	private GitHubClientExtended client;

	private ModelUpdater modelUpdater;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {

		client = new GitHubClientExtended();
		model = new Model(client);

		mainStage = stage;

		Scene scene = new Scene(createRoot(), 800, 600);
		// setUpHotkeys(scene);

		setupStage(stage, scene);
	}

	private void setupStage(Stage stage, Scene scene) {
		stage.setTitle("HubTurbo");
		stage.setMinWidth(800);
		stage.setMinHeight(600);
		stage.setScene(scene);
		stage.show();
		stage.setOnCloseRequest(e -> {
			if (modelUpdater != null) {
				modelUpdater.stopModelUpdate();
			}
		});
	}

	// Node definitions

	private Parent createRoot() {

		columns = new ColumnControl(mainStage, model);
		menu = new MenuControl(mainStage, model, client, columns, this);

		BorderPane root = new BorderPane();
		root.setCenter(columns);
		root.setTop(menu);

		return root;
	}
	
	public void setModelUpdater(ModelUpdater mu) {
		modelUpdater = mu;
	}

}
