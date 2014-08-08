package ui;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import service.ServiceManager;
import util.ConfigFileHandler;
import util.SessionConfigurations;

public class UI extends Application {
	private static final Logger logger = LogManager.getLogger(UI.class.getName());
	// Main UI elements
	
	private Stage mainStage;
	private ColumnControl columns;
	private SidePanel sidePanel;
	private MenuControl menuBar;
	
	private SessionConfigurations sessionConfig;
	
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		initCSS();
		sessionConfig = ConfigFileHandler.loadSessionConfig();
		mainStage = stage;
		stage.setMaximized(true);
		Scene scene = new Scene(createRoot());
		setupMainStage(scene);
		loadFonts();
		applyCSS(scene);
		getUserCredentials();
	}
	
	private void getUserCredentials() {
		new LoginDialog(mainStage, columns).show().thenApply(success -> {
			if (!success) {
				mainStage.close();
			} else {
				columns.loadIssues();
				sidePanel.refresh();
			}
			return true;
		}).exceptionally(e -> {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		});
	}
	
	private static String CSS = "";
	
	public void initCSS() {
		CSS = this.getClass().getResource("hubturbo.css").toString();
	}

	public static void applyCSS(Scene scene) {
		scene.getStylesheets().clear();
		scene.getStylesheets().add(CSS);
	}
	
	public static void loadFonts(){
		Font.loadFont(UI.class.getResource("/resources/octicons/octicons-local.ttf").toExternalForm(), 32);
	}

	private void setupMainStage(Scene scene) {
		mainStage.setTitle("HubTurbo");
		mainStage.setMinWidth(800);
		mainStage.setMinHeight(600);
		mainStage.setScene(scene);
		mainStage.show();
		mainStage.setOnCloseRequest(e -> {
			ServiceManager.getInstance().stopModelUpdate();
			columns.saveSession();
			ConfigFileHandler.saveSessionConfig(sessionConfig);
			Platform.exit();
			System.exit(0);
		});
		
	}

	private Parent createRoot() throws IOException {

		sidePanel = new SidePanel(mainStage, ServiceManager.getInstance().getModel());
		columns = new ColumnControl(mainStage, ServiceManager.getInstance().getModel(), sidePanel);
		sidePanel.setColumns(columns);

		ScrollPane columnsScroll = new ScrollPane(columns);
		columnsScroll.getStyleClass().add("transparent-bg");
		columnsScroll.setFitToHeight(true);
		columnsScroll.setVbarPolicy(ScrollBarPolicy.NEVER);
		HBox.setHgrow(columnsScroll, Priority.ALWAYS);
		
		menuBar = new MenuControl(columns, sidePanel, columnsScroll);

		HBox centerContainer = new HBox();
		centerContainer.setPadding(new Insets(5,0,5,0));
		centerContainer.getChildren().addAll(sidePanel.getControlLabel(), columnsScroll);

		BorderPane root = new BorderPane();
		root.setTop(menuBar);
		root.setLeft(sidePanel);
		root.setCenter(centerContainer);
		root.setBottom(StatusBar.getInstance());

		return root;
	}
}
