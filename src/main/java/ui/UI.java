package ui;

import backend.Logic;
import backend.UIManager;
import browserview.BrowserComponent;
import browserview.BrowserComponentStub;
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
import prefs.Preferences;
import ui.components.HTStatusBar;
import ui.components.StatusUI;
import ui.issuecolumn.ColumnControl;
import util.PlatformEx;
import util.PlatformSpecific;
import util.TickingTimer;
import util.Utility;
import util.events.*;
import util.events.Event;

import java.awt.*;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class UI extends Application implements EventDispatcher {

	private static final int VERSION_MAJOR = 2;
	private static final int VERSION_MINOR = 7;
	private static final int VERSION_PATCH = 0;

	public static final String ARG_UPDATED_TO = "--updated-to";

	private static final double WINDOW_DEFAULT_PROPORTION = 0.6;

	private static final Logger logger = LogManager.getLogger(UI.class.getName());
	private static HWND mainWindowHandle;

	private static final int REFRESH_PERIOD = 60;

	// Application-level state

	public UIManager uiManager;
	public Logic logic;
	public Preferences prefs;
	public static StatusUI status;
	public static EventDispatcher events;
	public EventBus eventBus;
	private HashMap<String, String> commandLineArgs;
	private TickingTimer refreshTimer;

	// Main UI elements

	private Stage mainStage;
	private ColumnControl columns;
	private MenuControl menuBar;
	private BrowserComponent browserComponent;
	private RepositorySelector repoSelector;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {

		initPreApplicationState();
		initUI(stage);
		initApplicationState();

		getUserCredentials();
	}

	private void getUserCredentials() {
		if (isBypassLogin()) {
			showMainWindow("dummy/dummy");
		} else {
			new LoginDialog(this, prefs, mainStage).show().thenApply(result -> {
				if (result.success) {
					showMainWindow(result.repoId);
				} else {
					quit();
				}
				return true;
			}).exceptionally(e -> {
				logger.error(e.getLocalizedMessage(), e);
				return false;
			});
		}
	}

	private void showMainWindow(String repoId) {
		logic.openRepository(repoId);
		logic.setDefaultRepo(repoId);
		repoSelector.setText(repoId);

		triggerEvent(new BoardSavedEvent());

		if (isTestMode()) {
			if (isTestChromeDriver()) {
				browserComponent = new BrowserComponent(this, true);
				browserComponent.initialise();
			} else {
				browserComponent = new BrowserComponentStub(this);
			}
			registerTestEvents();
		} else {
			browserComponent = new BrowserComponent(this, false);
			browserComponent.initialise();
		}

		setExpandedWidth(false);
		ensureSelectedPanelHasFocus();
		columns.init();
	}

	private void registerTestEvents() {
		registerEvent((UILogicRefreshEventHandler) e -> Platform.runLater(logic::refresh));
	}

	private void initPreApplicationState() {
		UI.events = this;

		Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) ->
			logger.error(throwable.getMessage(), throwable));

		commandLineArgs = initialiseCommandLineArguments();
		prefs = new Preferences(isTestMode());

		eventBus = new EventBus();
		registerEvent((RepoOpenedEventHandler) e -> onRepoOpened());

		uiManager = new UIManager(this);
		status = new HTStatusBar(this);
	}

	private void initApplicationState() {
		// In the future, when more arguments are passed to logic,
		// we can pass them in the form of an array.
		logic = new Logic(uiManager, prefs, isTestMode(), isTestJSONEnabled());
		clearCacheIfNecessary();
		refreshTimer = new TickingTimer("Refresh Timer", REFRESH_PERIOD,
			status::updateTimeToRefresh, logic::refresh, TimeUnit.SECONDS);
		refreshTimer.start();
	}

	private void initUI(Stage stage) {
		repoSelector = createRepoSelector();
		mainStage = stage;
		stage.setMaximized(false);

		Scene scene = new Scene(createRoot());
		setupMainStage(scene);

		loadFonts();
		String css = initCSS();
		applyCSS(css, scene);
	}

	public boolean isTestMode() {
		if (commandLineArgs.getOrDefault("test", "false").equalsIgnoreCase("true")) return true;
		if (isBypassLogin()) return true;
		return false;
	}

	private boolean isBypassLogin() {
		return commandLineArgs.getOrDefault("bypasslogin", "false").equalsIgnoreCase("true");
	}

	private boolean isTestJSONEnabled() {
		return commandLineArgs.getOrDefault("testjson", "false").equalsIgnoreCase("true");
	}

	private boolean isTestChromeDriver() {
		return commandLineArgs.getOrDefault("testchromedriver", "false").equalsIgnoreCase("true");
	}

	public void quit() {
		if (browserComponent != null) {
			browserComponent.onAppQuit();
		}
		if (!isTestMode()) {
			columns.saveSession();
			prefs.saveGlobalConfig();
			Platform.exit();
			System.exit(0);
		}
	}

	public void onRepoOpened() {
		repoSelector.refreshContents();
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
		Font.loadFont(UI.class.getResource("octicons/octicons-local.ttf").toExternalForm(), 32);
	}

	private void setupMainStage(Scene scene) {
		mainStage.setTitle("HubTurbo " + Utility.version(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH));
		mainStage.setScene(scene);
		mainStage.show();
		mainStage.setOnCloseRequest(e -> quit());
		initialiseJNA(mainStage.getTitle());
		mainStage.focusedProperty().addListener((unused, wasFocused, isFocused) -> {
            if (!isFocused) {
                return;
            }
            if (PlatformSpecific.isOnWindows()) {
                browserComponent.focus(mainWindowHandle);
            }
            PlatformEx.runLaterDelayed(() -> {
                boolean shouldRefresh = browserComponent.hasBviewChanged();
                if (shouldRefresh) {
                    logger.info("Browser view has changed; refreshing");
                    logic.refresh();
                    refreshTimer.restart();
                }
            });
        });
	}

	private static void initialiseJNA(String windowTitle) {
		if (PlatformSpecific.isOnWindows()) {
			mainWindowHandle = User32.INSTANCE.FindWindow(null, windowTitle);
		}
	}

	private HashMap<String, String> initialiseCommandLineArguments() {
		Parameters params = getParameters();
		HashMap<String, String> commandLineArgs = new HashMap<>(params.getNamed());
		return commandLineArgs;
	}

	private Parent createRoot() {

		columns = new ColumnControl(this, prefs);

		VBox top = new VBox();

		ScrollPane columnsScrollPane = new ScrollPane(columns);
		columnsScrollPane.getStyleClass().add("transparent-bg");
		columnsScrollPane.setFitToHeight(true);
		columnsScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		HBox.setHgrow(columnsScrollPane, Priority.ALWAYS);

		menuBar = new MenuControl(this, columns, columnsScrollPane, prefs);
		top.getChildren().addAll(menuBar, repoSelector);

		BorderPane root = new BorderPane();
		root.setTop(top);
		root.setCenter(columnsScrollPane);
		root.setBottom((HTStatusBar) status);

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

		mainStage.setMinWidth(columns.getPanelWidth());
		mainStage.setMinHeight(dimensions.getHeight());
		mainStage.setMaxWidth(width);
		mainStage.setMaxHeight(dimensions.getHeight());
		mainStage.setX(0);
		mainStage.setY(0);
		mainStage.setMaxWidth(dimensions.getWidth());
	}

	public HashMap<String, String> getCommandLineArgs() {
		return commandLineArgs;
	}

	private RepositorySelector createRepoSelector() {
		RepositorySelector repoSelector = new RepositorySelector(this);
		repoSelector.setOnValueChange(this::primaryRepoChanged);
		return repoSelector;
	}

	private void primaryRepoChanged(String repoId) {
		logic.openRepository(repoId);
		logic.setDefaultRepo(repoId);
		repoSelector.setText(repoId);
		columns.refresh();
	}

	private void ensureSelectedPanelHasFocus() {
		if (columns.getCurrentlySelectedColumn().isPresent()) {
			getMenuControl().scrollTo(
				columns.getCurrentlySelectedColumn().get(), columns.getChildren().size());
			columns.getColumn(columns.getCurrentlySelectedColumn().get()).requestFocus();
		}
	}

	public MenuControl getMenuControl() {
		return menuBar;
	}

	public void setDefaultWidth() {
		mainStage.setMaximized(false);
		Rectangle dimensions = getDimensions();
		mainStage.setMinWidth(columns.getPanelWidth());
		mainStage.setMinHeight(dimensions.getHeight());
		mainStage.setMaxWidth(columns.getPanelWidth());
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
