package ui;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryId;

import service.ServiceManager;
import storage.DataManager;
import ui.components.StatusBar;
import ui.issuecolumn.ColumnControl;
import util.DialogMessage;
import util.Utility;
import util.events.Event;
import util.events.EventDispatcher;
import util.events.EventHandler;
import util.events.LoginEvent;
import util.events.PanelSavedEvent;
import browserview.BrowserComponent;

import com.google.common.eventbus.EventBus;

public class UI extends Application implements EventDispatcher {
	
	private static final int VERSION_MAJOR = 1;
	private static final int VERSION_MINOR = 5;
	private static final int VERSION_PATCH = 0;
	
	public static final String ARG_UPDATED_TO = "--updated-to";

	private static final double WINDOW_DEFAULT_PROPORTION = 0.6;

	private static final Logger logger = LogManager.getLogger(UI.class.getName());

	// Main UI elements
	
	private Stage mainStage;
	private ColumnControl columns;
	private MenuControl menuBar;
	private BrowserComponent browserComponent;
	private RepositorySelector repoSelector;

	// Events
	
	private EventBus events;
	
	// Application arguments
	
	private HashMap<String, String> commandLineArgs;
		
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	private static UI instance;
	public static UI getInstance() {
		return instance;
	}

	@Override
	public void start(Stage stage) throws IOException {
		
		instance = this;
		
		//log all uncaught exceptions
		Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            logger.error(throwable.getMessage(), throwable);
        });
		
		events = new EventBus();
		
		repoSelector = createRepoFields();
		
		browserComponent = new BrowserComponent(this);
		initCSS();
		mainStage = stage;
		stage.setMaximized(false);
		Scene scene = new Scene(createRoot());
		setupMainStage(scene);
		loadFonts();
		applyCSS(scene);
		getUserCredentials();
		commandLineArgs = initialiseCommandLineArguments();
		
		DataManager.getInstance();
	}

	private void getUserCredentials() {
		repoSelector.setDisable(true);
		new LoginDialog(mainStage, columns).show().thenApply(success -> {
			if (success) {
				browserComponent.initialise();
				setExpandedWidth(false);
				columns.loadIssues();
				triggerEvent(new LoginEvent());
				repoSelector.setDisable(false);
				repoSelector.refreshComboBoxContents();
				repoSelector.setValue(ServiceManager.getInstance().getRepoId().generateId());
				triggerEvent(new PanelSavedEvent());
			} else {
				quit();
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
		
		mainStage.setTitle("HubTurbo " + Utility.version(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH));
		mainStage.setScene(scene);
		mainStage.show();
		mainStage.setOnCloseRequest(e -> quit());
		mainStage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean was, Boolean is) {
				if (is) {
					logger.info("Gained focus; refreshing");
					ServiceManager.getInstance().getModel().refresh();
				}
			}
		});
	}
	
	private HashMap<String, String> initialiseCommandLineArguments() {
		Parameters params = getParameters();
		final List<String> parameters = params.getRaw();
		assert parameters.size() % 2 == 0 : "Parameters should come in pairs";
		HashMap<String, String> commandLineArgs = new HashMap<>();
		for (int i=0; i<parameters.size(); i+=2) {
			commandLineArgs.put(parameters.get(i), parameters.get(i+1));
		}
		return commandLineArgs;
	}

	private void quit() {
		ServiceManager.getInstance().shutdownModelUpdate();
		columns.saveSession();
		DataManager.getInstance().saveLocalConfig();
		DataManager.getInstance().saveSessionConfig();
		browserComponent.quit();
		Platform.exit();
		System.exit(0);
	}
	
	private Parent createRoot() throws IOException {

		columns = new ColumnControl(this, mainStage, ServiceManager.getInstance().getModel());
		
		VBox top = new VBox();

		ScrollPane columnsScrollPane = new ScrollPane(columns);
		columnsScrollPane.getStyleClass().add("transparent-bg");
		columnsScrollPane.setFitToHeight(true);
		columnsScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		HBox.setHgrow(columnsScrollPane, Priority.ALWAYS);
		
		menuBar = new MenuControl(this, columns, columnsScrollPane);
		top.getChildren().addAll(menuBar, repoSelector);

		BorderPane root = new BorderPane();
		root.setTop(top);
		root.setCenter(columnsScrollPane);
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

	@Override
	public <T extends Event> void registerEvent(EventHandler handler) {
		events.register(handler);
		logger.info("Registered event handler " + handler.getClass().getInterfaces()[0].getSimpleName());
	}
	
	@Override
	public <T extends Event> void triggerEvent(T event) {
		logger.info("About to trigger event " + event.getClass().getSimpleName());
		events.post(event);
		logger.info("Triggered event " + event.getClass().getSimpleName());
	}
	
	public BrowserComponent getBrowserComponent() {
		return browserComponent;
	}
	
	/**
	 * Tracks whether or not the window is in an expanded state.
	 */
	private boolean expanded = false;

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
	 * This function may be called before the main stage is initialised, in
	 * which case it simply returns a reasonable default.
	 */
	public double getCollapsedX() {
		if (mainStage == null) {
			return getDimensions().getWidth() * WINDOW_DEFAULT_PROPORTION;
		}
		return mainStage.getWidth();
	}
	
	/**
	 * Returns the dimensions of the screen available for use when
	 * the main window is in a collapsed state.
	 * This function may be called before the main stage is initialised, in
	 * which case it simply returns a reasonable default.
	 */
	public Rectangle getAvailableDimensions() {
		Rectangle dimensions = getDimensions();
		if (mainStage == null) {
			return new Rectangle(
					(int) (dimensions.getWidth() * WINDOW_DEFAULT_PROPORTION),
					(int) dimensions.getHeight());
		}
		return new Rectangle(
				(int) (dimensions.getWidth() - mainStage.getWidth()),
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
				: dimensions.getWidth() * WINDOW_DEFAULT_PROPORTION;

		mainStage.setMinWidth(columns.getColumnWidth());
		mainStage.setMinHeight(dimensions.getHeight());
		mainStage.setMaxWidth(width);
		mainStage.setMaxHeight(dimensions.getHeight());
		mainStage.setX(0);
		mainStage.setY(0);
		mainStage.setMaxWidth(dimensions.getWidth());
		browserComponent.resize(mainStage.getWidth());
	}

	public HashMap<String, String> getCommandLineArgs() {
		return commandLineArgs;
	}
	
	private RepositorySelector createRepoFields() {
		RepositorySelector repoIdBox = new RepositorySelector();
		repoIdBox.setComboValueChangeMethod(this::loadRepo);
		return repoIdBox;
	}
	
	private boolean checkRepoAccess(IRepositoryIdProvider currRepo){
		try {
			if(!ServiceManager.getInstance().isRepositoryValid(currRepo)){
				Platform.runLater(() -> {
					DialogMessage.showWarningDialog("Error loading repository", "Repository does not exist or you do not have permission to access the repository");
				});
				return false;
			}
		} catch (SocketTimeoutException e){
			DialogMessage.showWarningDialog("Internet Connection Timeout", 
					"Timeout while connecting to GitHub, please check your internet connection.");
			logger.error(e.getLocalizedMessage(), e);
		} catch (UnknownHostException e){
			DialogMessage.showWarningDialog("No Internet Connection", 
					"Please check your internet connection and try again.");
			logger.error(e.getLocalizedMessage(), e);
		}catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	private void loadRepo(String repoString) {
		RepositoryId repoId = RepositoryId.createFromId(repoString);
		if(repoId == null 
		  || repoId.equals(ServiceManager.getInstance().getRepoId()) 
		  || !checkRepoAccess(repoId)){
			return;
		}
		
		logger.info("Switching repository to " + repoString + " in progress");
		
		repoSelector.setDisable(true);
		columns.saveSession();
		DataManager.getInstance().addToLastViewedRepositories(repoId.generateId());
		
		Task<Boolean> task = new Task<Boolean>(){
			@Override
			protected Boolean call() throws IOException {
				ServiceManager.getInstance().stopModelUpdate();
				HashMap<String, List> items =  ServiceManager.getInstance().getResources(repoId);
			
				ServiceManager.getInstance().getModel().populateComponents(repoId, items);
				
				try {
					ServiceManager.getInstance().updateModelNow().await();
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}

				final CountDownLatch latch = new CountDownLatch(1);
				Platform.runLater(() -> {
					// Re-enable repository switching when everything is done
					repoSelector.setDisable(false);
					
					columns.restoreColumns();
					triggerEvent(new PanelSavedEvent());
					latch.countDown();
				});
				try {
					latch.await();
				} catch (InterruptedException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
				return true;
			}
		};
		DialogMessage.showProgressDialog(task, "Loading issues from " + repoId.generateId() + "...");
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
			
		task.setOnSucceeded(wse -> {
			repoSelector.refreshComboBoxContents();
			StatusBar.displayMessage("Issues loaded successfully!");
			ServiceManager.getInstance().updateModelPeriodically();
			logger.info("Repository " + repoString + " successfully switched to!");
		});
			
		task.setOnFailed(wse -> {
			Throwable err = task.getException();
			logger.error(err.getLocalizedMessage(), err);
			StatusBar.displayMessage("An error occurred: " + err);
		});

	}
}
