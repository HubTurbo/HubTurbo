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
import ui.issuepanel.expanded.BrowserComponent;
import ui.sidepanel.SidePanel;
import util.Utility;
import util.events.Event;
import util.events.EventHandler;
import util.events.LoginEvent;

import com.google.common.eventbus.EventBus;

public class UI extends Application {

	private static final String VERSION_NUMBER = " V0.7.13";
	private static final double WINDOW_EXPANDED_WIDTH = 0.6;

	private static final Logger logger = LogManager.getLogger(UI.class.getName());

	// Main UI elements
	
	private Stage mainStage;
	private ColumnControl columns;
	private SidePanel sidePanel;
	private MenuControl menuBar;
	private BrowserComponent browserComponent;

	// Events
	
	private EventBus events;
		
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		//log all uncaught exceptions
		Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            logger.error(throwable.getMessage(), throwable);
        });
		
		events = new EventBus();
		
		browserComponent = new BrowserComponent(this);
		browserComponent.initialise();
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
				triggerEvent(new LoginEvent());
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
			browserComponent.quit();
			Platform.exit();
			System.exit(0);
		});
		
	}
	
	private Parent createRoot() throws IOException {

		sidePanel = new SidePanel(this, mainStage, ServiceManager.getInstance().getModel());
		columns = new ColumnControl(this, mainStage, ServiceManager.getInstance().getModel(), sidePanel);
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
	 * Publish/subscribe API making use of Guava's EventBus.
	 * Takes a lambda expression to be called upon an event being fired.
	 * @param handler
	 */
	public <T extends Event> void registerEvent(EventHandler handler) {
		events.register(handler);
	}
	
	/**
	 * Publish/subscribe API making use of Guava's EventBus.
	 * Triggers all events of a certain type. EventBus will ensure that the
	 * event is fired for all subscribers whose parameter is either the same
	 * or a super type.
	 * @param handler
	 */
	public <T extends Event> void triggerEvent(T event) {
		events.post(event);
	}
	
	public BrowserComponent getBrowserComponent() {
		return browserComponent;
	}
	
	/**
	 * Tracks whether or not the window is in an expanded state.
	 */
	private boolean expanded = true;

	public boolean isExpanded() {
		return expanded;
	}

	/**
	 * Toggles the expansion state of the window.
	 * Returns a boolean value indicating the state.
	 */
	public boolean toggleExpandedWidth() {
		expanded = !expanded;
		setExpandedWidth(expanded);
		return expanded;
	}

	/**
	 * Returns the X position of the edge of the collapsed window.
	 */
	public double getCollapsedX() {
		return getDimensions().getWidth() * WINDOW_EXPANDED_WIDTH;
	}
	
	/**
	 * Returns the dimensions of the screen available for use when
	 * the main window is in a collapsed state.
	 */
	public Rectangle getAvailableDimensions() {
		Rectangle dimensions = getDimensions();
		return new Rectangle(
				(int) (dimensions.getWidth() * (1 - WINDOW_EXPANDED_WIDTH)),
				(int) dimensions.getHeight());
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
