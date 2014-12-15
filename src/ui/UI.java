package ui;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Optional;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import service.ServiceManager;
import storage.DataManager;
import ui.issuecolumn.ColumnControl;
import ui.sidepanel.SidePanel;
import util.Utility;

public class UI extends Application {
	private static final Logger logger = LogManager.getLogger(UI.class.getName());
	private static final String VERSION_NUMBER = " V0.7.13";
	// Main UI elements
	
	private static final double WINDOW_EXPANDED_WIDTH = 0.6;
	private Stage mainStage;
	private ColumnControl columns;
	private SidePanel sidePanel;
	private MenuControl menuBar;
		
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		//log all uncaught exceptions
		Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            logger.error(throwable.getMessage(), throwable);
        });
		
		initCSS();
		mainStage = stage;
		stage.setMaximized(false);
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
		mainStage.setTitle("HubTurbo " + VERSION_NUMBER);
		setExpandedWidth(true);
		mainStage.setScene(scene);
		mainStage.show();
		mainStage.setOnCloseRequest(e -> {
			ServiceManager.getInstance().stopModelUpdate();
			columns.saveSession();
			DataManager.getInstance().saveSessionConfig();
			Platform.exit();
			System.exit(0);
		});
		
	}
	
	private Parent createRoot() throws IOException {

		sidePanel = new SidePanel(this, mainStage, ServiceManager.getInstance().getModel());
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

	/**
	 * Sets the dimensions of the stage to the maximum usable size
	 * of the desktop, or to the screen size if this fails.
	 * @param mainStage
	 */
	private Rectangle getDimensions() {
		Optional<Rectangle> dimensions = Utility.getUsableScreenDimensions();
		if (dimensions.isPresent()) {
			return dimensions.get();
		} else {
			return Utility.getScreenDimensions();
		}
	}
	
	/**
	 * UI operations
	 */

	/**
	 * Tracks whether or not the window is in an expanded state.
	 */
	private boolean expanded = true;

	/**
	 * Toggles the expansion state of the window.
	 */
	public void toggleExpandedWidth() {
		expanded = !expanded;
		setExpandedWidth(expanded);
	}

	/**
	 * Controls whether or not the main window is expanded (occupying the
	 * whole screen) or not (occupying a percentage).
	 * @param expanded
	 */
	private void setExpandedWidth(boolean expanded) {
		this.expanded = expanded;
		Rectangle dimensions = getDimensions();
		double width = expanded
				? dimensions.getWidth()
				: dimensions.getWidth() * WINDOW_EXPANDED_WIDTH;
		mainStage.setMinWidth(width);
		mainStage.setMinHeight(dimensions.getHeight());
		mainStage.setMaxWidth(width);
		mainStage.setMaxHeight(dimensions.getHeight());
	}
}
