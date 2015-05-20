package ui;

import backend.Logic;
import backend.UIManager;
import browserview.BrowserComponent;
import com.google.common.eventbus.EventBus;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import ui.components.HTStatusBar;
import ui.issuecolumn.ColumnControl;
import util.PlatformEx;
import util.PlatformSpecific;
import util.Utility;
import util.events.BoardSavedEvent;
import util.events.Event;
import util.events.EventDispatcher;
import util.events.EventHandler;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class UI extends Application implements EventDispatcher {

	private static final int VERSION_MAJOR = 2;
	private static final int VERSION_MINOR = 6;
	private static final int VERSION_PATCH = 0;

	public static final String ARG_UPDATED_TO = "--updated-to";

	private static final double WINDOW_DEFAULT_PROPORTION = 0.6;

	private static final Logger logger = LogManager.getLogger(UI.class.getName());
	private static HWND mainWindowHandle;

	public UIManager uiManager;
	public Logic logic;
	public static EventDispatcher events;

	// Main UI elements

	private Stage mainStage;
	private ColumnControl columns;
	private MenuControl menuBar;
	private BrowserComponent browserComponent;
//	private RepositorySelector repoSelector;

	// Events

	public EventBus eventBus;

	// Application arguments

	private HashMap<String, String> commandLineArgs;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {

		initApplicationState();
		initUI(stage);

		getUserCredentials();
	}

	private void getUserCredentials() {
//		repoSelector.setDisable(true);
		new LoginDialog(this, mainStage).show().thenApply(result -> {
			if (result.success) {
//				repoSelector.refreshComboBoxContents(ServiceManager.getInstance().getRepoId().generateId());
//				repoSelector.setDisable(false);
				logic.openRepository(result.repoId);
//              DataManager.getInstance().addToLastViewedRepositories(repoId);
				triggerEvent(new BoardSavedEvent());
				browserComponent = new BrowserComponent(this, result.repoId);
				setExpandedWidth(false);
				ensureSelectedPanelHasFocus();
			} else {
				quit();
			}
			return true;
		}).exceptionally(e -> {
			logger.error(e.getLocalizedMessage(), e);
			return false;
		});
	}

	private void initApplicationState() {
		Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) ->
			logger.error(throwable.getMessage(), throwable));
		eventBus = new EventBus();
		uiManager = new UIManager(this);
		logic = new Logic(uiManager);

		UI.events = this;

		commandLineArgs = initialiseCommandLineArguments();
//		DataManager.getInstance();
		clearCacheIfNecessary();
	}

	private void initUI(Stage stage) {
//		repoSelector = createRepoSelector();
		mainStage = stage;
		stage.setMaximized(false);

		Scene scene = new Scene(createRoot());
		setupMainStage(scene);

		loadFonts();
		String css = initCSS();
		applyCSS(css, scene);
	}

	/**
	 * TODO Stop-gap measure pending a more robust updater
	 */
	private void clearCacheIfNecessary() {
		if (getCommandLineArgs().containsKey(ARG_UPDATED_TO)) {
			// TODO
//			CacheFileHandler.deleteCacheDirectory();
		}
	}

	public String initCSS() {
		return getClass().getResource("hubturbo.css").toString();
	}

	public static void applyCSS(String css, Scene scene) {
		scene.getStylesheets().clear();
		scene.getStylesheets().add(css);
	}

	public static void loadFonts(){
		Font.loadFont(UI.class.getResource("/resources/octicons/octicons-local.ttf").toExternalForm(), 32);
	}

	private void setupMainStage(Scene scene) {
		mainStage.setTitle("HubTurbo " + Utility.version(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH));
		mainStage.setScene(scene);
		mainStage.show();
		mainStage.setOnCloseRequest(e -> quit());
		initialiseJNA(mainStage.getTitle());
		mainStage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> unused, Boolean wasFocused, Boolean isFocused) {
				if (!isFocused) {
					return;
				}
				if (PlatformSpecific.isOnWindows()) {
					browserComponent.focus(mainWindowHandle);
				}
				PlatformEx.runLaterDelayed(() -> {
					// A refresh is triggered if:
					// 1. Repo-switching is not disabled (meaning an update is not in progress)
					// 2. The repo-switching box is not in focus (clicks on it won't trigger this)
//					boolean shouldRefresh = isRepoSwitchingAllowed() && !repoSelector.isInFocus() && browserComponent.hasBviewChanged();
					boolean shouldRefresh = browserComponent.hasBviewChanged();

					if (shouldRefresh) {
						logger.info("Gained focus; refreshing");
						logic.refresh();
					}
				});
			}
		});
	}

	private static void initialiseJNA(String windowTitle) {
		if (PlatformSpecific.isOnWindows()) {
			mainWindowHandle = User32.INSTANCE.FindWindow(null, windowTitle);
		}
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

	public void quit() {
		columns.saveSession();
//		DataManager.getInstance().saveLocalConfig();
//		DataManager.getInstance().saveSessionConfig();
		if (browserComponent != null) {
			browserComponent.onAppQuit();
		}
		Platform.exit();
		System.exit(0);
	}

	private Parent createRoot() {

		columns = new ColumnControl(this);

		VBox top = new VBox();

		ScrollPane columnsScrollPane = new ScrollPane(columns);
		columnsScrollPane.getStyleClass().add("transparent-bg");
		columnsScrollPane.setFitToHeight(true);
		columnsScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		HBox.setHgrow(columnsScrollPane, Priority.ALWAYS);

		menuBar = new MenuControl(this, columns, columnsScrollPane);
		top.getChildren().addAll(menuBar);//, repoSelector);

		BorderPane root = new BorderPane();
		root.setTop(top);
		root.setCenter(columnsScrollPane);
		root.setBottom(HTStatusBar.getInstance());

		return root;
	}

	/**
	 * Sets the dimensions of the stage to the maximum usable size
	 * of the desktop, or to the screen size if this fails.
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
	public void registerEvent(EventHandler handler) {
		eventBus.register(handler);
		logger.info("Registered event handler " + handler.getClass().getInterfaces()[0].getSimpleName());
	}

	@Override
	public void unregisterEvent(EventHandler handler) {
		eventBus.unregister(handler);
		logger.info("Unregistered event handler " + handler.getClass().getInterfaces()[0].getSimpleName());
	}

	@Override
	public <T extends Event> void triggerEvent(T event) {
		logger.info("About to trigger event " + event.getClass().getSimpleName());
		eventBus.post(event);
		logger.info("Triggered event " + event.getClass().getSimpleName());
	}

	public BrowserComponent getBrowserComponent() {
		return browserComponent;
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

//	private RepositorySelector createRepoSelector() {
//		RepositorySelector repoSelector = new RepositorySelector();
//		repoSelector.setOnValueChange(this::loadRepo);
//		return repoSelector;
//	}

//	private boolean checkRepoAccess(IRepositoryIdProvider currRepo){
		// TODO
//		try {
//			if(!ServiceManager.getInstance().isRepositoryValid(currRepo)){
//				Platform.runLater(() -> {
//					DialogMessage.showWarningDialog("Error loading repository", "Repository does not exist or you do not have permission to access the repository");
//				});
//				return false;
//			}
//		} catch (SocketTimeoutException e){
//			DialogMessage.showWarningDialog("Internet Connection Timeout",
//					"Timeout while connecting to GitHub, please check your internet connection.");
//			logger.error(e.getLocalizedMessage(), e);
//		} catch (UnknownHostException e){
//			DialogMessage.showWarningDialog("No Internet Connection",
//					"Please check your internet connection and try again.");
//			logger.error(e.getLocalizedMessage(), e);
//		}catch (IOException e) {
//			logger.error(e.getLocalizedMessage(), e);
//		}
//		return true;
//	}

//	private boolean repoSwitchingAllowed = true;

//	public boolean isRepoSwitchingAllowed() {
//		return repoSwitchingAllowed;
//	}

//	public void enableRepositorySwitching() {
//		repoSwitchingAllowed = true;
//		repoSelector.setLabelText("");
//		repoSelector.enable();
//	}

//	public void disableRepositorySwitching() {
//		repoSwitchingAllowed = false;
//		repoSelector.setLabelText("Syncing...");
//		repoSelector.disable();
//	}

//	private void loadRepo(String repoString) {
//		RepositoryId repoId = RepositoryId.createFromId(repoString);
//		if(repoId == null
////		  || repoId.equals(ServiceManager.getInstance().getRepoId())
//		  || !checkRepoAccess(repoId)){
//			return;
//		}
//
//		logger.info("Switching repository to " + repoString + "...");
//
//		columns.saveSession();
////		DataManager.getInstance().addToLastViewedRepositories(repoId.generateId());
//
//		Task<Boolean> task = new Task<Boolean>(){
//			@Override
//			protected Boolean call() throws IOException {
//
////				updateProgress(0, 1);
////				updateMessage(String.format("Switching to %s...",
////					ServiceManager.getInstance().getRepoId().generateId()));
////
////				ServiceManager.getInstance().switchRepository(repoId, (message, progress) -> {
////					updateProgress(progress * 100, 100);
////					updateMessage(message);
////				});
//				// TODO
//				logic.openRepository(repoId.generateId());
//
//                PlatformEx.runAndWait(() -> {
//                    columns.restoreColumns();
//                    triggerEvent(new BoardSavedEvent());
//                });
//				return true;
//			}
//		};
//		DialogMessage.showProgressDialog(task, "Switching to " + repoId.generateId() + "...");
//		Thread thread = new Thread(task);
//		thread.setDaemon(true);
//		thread.start();
//
//		task.setOnSucceeded(wse -> {
////			repoSelector.refreshComboBoxContents(ServiceManager.getInstance().getRepoId().generateId());
//			logger.info("Repository " + repoString + " successfully switched to!");
//			ensureSelectedPanelHasFocus();
//		});
//
//		task.setOnFailed(wse -> {
//			Throwable err = task.getException();
//			logger.error(err.getLocalizedMessage(), err);
//			HTStatusBar.displayMessage("An error occurred with repository switching: " + err);
//		});
//	}

	private void ensureSelectedPanelHasFocus() {
		if(columns.getCurrentlySelectedColumn().isPresent()) {
			getMenuControl().scrollTo(columns.getCurrentlySelectedColumn().get(), columns.getChildren().size());
			columns.getColumn(columns.getCurrentlySelectedColumn().get()).requestFocus();
		}
	}

	public MenuControl getMenuControl() {
		return menuBar;
	}

	public void setDefaultWidth() {
		mainStage.setMaximized(false);
		Rectangle dimensions = getDimensions();
		mainStage.setMinWidth(columns.getColumnWidth());
		mainStage.setMinHeight(dimensions.getHeight());
		mainStage.setMaxWidth(columns.getColumnWidth());
		mainStage.setMaxHeight(dimensions.getHeight());
		mainStage.setX(0);
		mainStage.setY(0);
	}

	public void maximizeWindow() {
		mainStage.setMaximized(true);
		Rectangle dimensions = getDimensions();
		mainStage.setMinWidth(dimensions.getWidth());
		mainStage.setMinHeight(dimensions.getHeight());
		mainStage.setMaxWidth(dimensions.getWidth());
		mainStage.setMaxHeight(dimensions.getHeight());
		mainStage.setX(0);
		mainStage.setY(0);
	}

	public void minimizeWindow() {
		mainStage.setIconified(true);
		menuBar.scrollTo(columns.getCurrentlySelectedColumn().get(), columns.getChildren().size());
	}

	public HWND getMainWindowHandle() {
		return mainWindowHandle;
	}
}
