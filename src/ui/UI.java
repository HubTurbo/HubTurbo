package ui;

import java.io.File;

import util.GitHubClientExtended;
import util.ModelUpdater;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Model;

public class UI extends Application {

//	public static final String STYLE_YELLOW_BORDERS = "-fx-background-color: #FFFA73; -fx-border-color: #000000; -fx-border-width: 1px;";
//	public static final String STYLE_BORDERS_FADED = "";
//	public static final String STYLE_BORDERS = "-fx-border-color: #000000; -fx-border-width: 1px;";
//	public static final String STYLE_FADED = "";

	// Main UI elements
	
	private Stage mainStage;

	private ColumnControl columns;
	private MenuControl menu;
	
	// Other components
	
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

		setupMainStage(scene);
		applyCSS(scene);
	}

	private static final String CSS = "file:///" + new File("hubturbo.css").getAbsolutePath().replace("\\", "/");

	public static void applyCSS(Scene scene) {
		scene.getStylesheets().clear();
		scene.getStylesheets().add(CSS);
	}

	private void setupMainStage(Scene scene) {
		mainStage.setTitle("HubTurbo");
		mainStage.setMinWidth(800);
		mainStage.setMinHeight(600);
		mainStage.setScene(scene);
		mainStage.show();
		mainStage.setOnCloseRequest(e -> {
			if (modelUpdater != null) {
				modelUpdater.stopModelUpdate();
			}
		});
	}

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
