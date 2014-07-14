package ui;

import java.io.File;
import java.io.IOException;

import org.controlsfx.control.NotificationPane;

import service.ServiceManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UI extends Application {

	// Main UI elements
	
	private Stage mainStage;

	private ColumnControl columns;
//	private MenuControl menu;
	private NotificationPane notificationPane;

	private SidePanel sidePanel;
	
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {

		mainStage = stage;
		
		Scene scene = new Scene(createRoot(), 800, 600);
		setupMainStage(scene);
		applyCSS(scene);
		
		getUserCredentials();
	}
	
	private void getUserCredentials() {
		new LoginDialog(mainStage).show().thenApply(success -> {
			if (!success) {
//				getUserCredentials();
				mainStage.close();
			} else {
				columns.loadIssues();
				sidePanel.refresh();
			}
			return true;
		}).exceptionally(e -> {
			e.printStackTrace();
			return false;
		});
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

//		menu = new MenuControl(mainStage, ServiceManager.getInstance().getModel(), columns, this);

		Button addColumn = new Button("\u2795");
		addColumn.setStyle("-fx-font-size: 14pt;");
		addColumn.setOnMouseClicked(columns::addColumnEvent);
		
		Button refresh = new Button("F5");
		refresh.setStyle("-fx-font-size: 12pt;");
		refresh.setOnMouseClicked(e -> {
			ServiceManager.getInstance().restartModelUpdate();
			columns.refresh();
		});
		
		VBox buttons = new VBox();
		buttons.getChildren().addAll(addColumn, refresh);

		BorderPane root = new BorderPane(); // TODO the root doesn't have to be a borderpane once the menu is no longer needed
		root.setCenter(notificationPane);
//		root.setTop(menu);
		root.setRight(buttons);

		sidePanel = new SidePanel(mainStage, ServiceManager.getInstance().getModel());

//		Parent panel = FXMLLoader.load(getClass().getResource("/SidePanelTabs.fxml"));
//		((TabPane) panel).getTabs().get(0).setContent(new ManageLabelsDialog(mainStage, ServiceManager.getInstance().getModel()).initialise());
		
        SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(sidePanel, root);
		splitPane.setDividerPositions(0.4);

		return splitPane;
	}
}
