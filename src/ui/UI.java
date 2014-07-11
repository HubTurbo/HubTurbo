package ui;

import java.io.File;
import java.io.IOException;

import org.controlsfx.control.NotificationPane;

import service.ServiceManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class UI extends Application {

	// Main UI elements
	
	private Stage mainStage;

	private ColumnControl columns;
	private MenuControl menu;
	private NotificationPane notificationPane;
	
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {

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
			ServiceManager.getInstance().stopModelUpdate();
		});
	}

	private Parent createRoot() throws IOException {

		notificationPane = new NotificationPane();
		columns = new ColumnControl(mainStage, ServiceManager.getInstance().getModel(), notificationPane);
		notificationPane.setContent(columns);

		menu = new MenuControl(mainStage, ServiceManager.getInstance().getModel(), columns, this);

		// TODO the root doesn't have to be a borderpane any more,
		// once the menu is no longer needed
		BorderPane root = new BorderPane();
		root.setCenter(notificationPane);
		root.setTop(menu);

		Parent panel = FXMLLoader.load(getClass().getResource("/SidePanelTabs.fxml"));
		
        SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(panel, root);
		splitPane.setDividerPositions(0.2);

		return splitPane;
	}
}
