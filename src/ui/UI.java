package ui;

import java.io.File;

import org.controlsfx.control.NotificationPane;

import service.ServiceManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class UI extends Application {

	// Main UI elements
	
	private Stage mainStage;

	private ColumnControl columns;
	private MenuControl menu;
	private NotificationPane notificationPane;
	
	// Other components
	
//	private Model model;
//	private GitHubClientExtended client;
//	private ModelUpdater modelUpdater;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {

//		client = new GitHubClientExtended();
//		model = new Model(client);

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
//			if (modelUpdater != null) {
//				modelUpdater.stopModelUpdate();
//			}
			ServiceManager.getInstance().stopModelUpdate();
		});
	}

	private Parent createRoot() {

		notificationPane = new NotificationPane();
		columns = new ColumnControl(mainStage, ServiceManager.getInstance().getModel(), notificationPane);
		notificationPane.setContent(columns);

		menu = new MenuControl(mainStage, ServiceManager.getInstance().getModel(), columns, this);

		BorderPane root = new BorderPane();
		root.setCenter(notificationPane);
		root.setTop(menu);

		return root;
	}
	
//	public void setModelUpdater(ModelUpdater mu) {
//		modelUpdater = mu;
//	}

}
