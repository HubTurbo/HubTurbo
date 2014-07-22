package ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
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
import util.LocalConfigurations;

public class UI extends Application {

	// Main UI elements
	
	private Stage mainStage;
	private ColumnControl columns;
	private SidePanel sidePanel;
	private MenuControl menuBar;
	private StatusBar statusBar;
	
	private LocalConfigurations localConfigurations;
	
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {

		initCSS();
		initLocalConfig();
		
		mainStage = stage;
		stage.setMaximized(true);
		Scene scene = new Scene(createRoot(), 800, 600);
		setupMainStage(scene);
		loadFonts();
		applyCSS(scene);
		getUserCredentials();
	}
	
	private void initLocalConfig() {
		localConfigurations = ConfigFileHandler.loadLocalConfig();
	}

	private void getUserCredentials() {
		new LoginDialog(mainStage, columns).show().thenApply(success -> {
			if (!success) {
//				getUserCredentials();
				mainStage.close();
			} else {
				columns.loadIssues();
				sidePanel.refresh();
				statusBar.setText("Logged in successfully! " + ServiceManager.getInstance().getRemainingRequests() + " requests remaining out of " + ServiceManager.getInstance().getRequestLimit() + ".");
			}
			return true;
		}).exceptionally(e -> {
			e.printStackTrace();
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
		Font.loadFont(UI.class.getResource("/resources/octicons/octicons-local.ttf").toExternalForm(), 24);
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
			Platform.exit();
			System.exit(0);
		});
		
	}

	private Parent createRoot() throws IOException {

		statusBar = new StatusBar();
		sidePanel = new SidePanel(mainStage, ServiceManager.getInstance().getModel());
		columns = new ColumnControl(mainStage, ServiceManager.getInstance().getModel(), sidePanel, statusBar);
		sidePanel.setColumns(columns);
		menuBar = new MenuControl(columns, sidePanel);
		
		ScrollPane columnsScroll = new ScrollPane(columns);
		columnsScroll.setFitToHeight(true);
		columnsScroll.setVbarPolicy(ScrollBarPolicy.NEVER);
		HBox.setHgrow(columnsScroll, Priority.ALWAYS);
		
		HBox centerContainer = new HBox();
		centerContainer.getChildren().addAll(sidePanel, columnsScroll);

        BorderPane root = new BorderPane();
		root.setTop(menuBar);
		root.setCenter(centerContainer);
		root.setBottom(statusBar);

		return root;
	}
}
