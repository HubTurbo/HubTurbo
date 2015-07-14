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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
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
import ui.components.KeyboardShortcuts;
import ui.components.StatusUI;
import ui.issuepanel.PanelControl;
import util.PlatformEx;
import util.PlatformSpecific;
import util.TickingTimer;
import util.Utility;
import util.events.*;
import util.events.Event;
import util.events.testevents.PrimaryRepoChangedEvent;
import util.events.testevents.UILogicRefreshEventHandler;

import java.awt.*;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class UI extends Application implements EventDispatcher {

    private static final int VERSION_MAJOR = 2;
    private static final int VERSION_MINOR = 9;
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
    public GUIController guiController;

    // Main UI elements

    private Stage mainStage;
    private PanelControl panels;
    private MenuControl menuBar;
    private BrowserComponent browserComponent;
    private RepositorySelector repoSelector;
    private Label apiBox;

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        initPreApplicationState();
        initUI(stage);
        initApplicationState();
        login(isBypassLogin());
    }

    private void login(boolean isBypassLogin) {
        if (isBypassLogin) {
            showMainWindow("dummy/dummy");
            mainStage.show();
        } else {
            if (logic.loginController.attemptLogin()) {
                showMainWindow(logic.loginController.getRepoId());
                mainStage.show();
            } else {
                disableUI(true);
                status.displayMessage("Waiting for login...");
                mainStage.show();
                new LoginDialog(this,
                                mainStage,
                                logic.loginController.getOwner(),
                                logic.loginController.getRepo(),
                                logic.loginController.getUsername(),
                                logic.loginController.getPassword())
                                .show().thenApply(isLoggedIn -> {
                    if (isLoggedIn) {
                        showMainWindow(logic.loginController.getRepoId());
                        disableUI(false);
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
        getMainWindowHandle(mainStage.getTitle());
    }

    private void disableUI(boolean disable) {
        mainStage.setResizable(!disable);
        menuBar.setDisable(disable);
        repoSelector.setDisable(disable);
    }

    private void showMainWindow(String repoId) {
        triggerEvent(new PrimaryRepoChangedEvent(repoId));
        logic.openPrimaryRepository(repoId);
        logic.setDefaultRepo(repoId);
        repoSelector.setText(repoId);

        triggerEvent(new BoardSavedEvent()); // Initializes boards

        if (isTestMode()) {
            if (isTestChromeDriver()) {
                browserComponent = new BrowserComponent(this, true);
                browserComponent.initialise();
            } else {
                browserComponent = new BrowserComponentStub(this);
            }
        } else {
            browserComponent = new BrowserComponent(this, false);
            browserComponent.initialise();
        }

        setExpandedWidth(false);
        panels.init(guiController);
        // Should only be called after panels have been initialized
        ensureSelectedPanelHasFocus();
    }

    protected void registerTestEvents() {
        registerEvent((UILogicRefreshEventHandler) e -> Platform.runLater(logic::refresh));
    }

    private void initPreApplicationState() {
        logger.info(Utility.version(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH));
        UI.events = this;

        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) ->
            logger.error(throwable.getMessage(), throwable));

        commandLineArgs = initialiseCommandLineArguments();
        prefs = new Preferences(isTestMode());
        KeyboardShortcuts.loadKeyboardShortcuts(prefs);

        eventBus = new EventBus();
        if (isTestMode()) {
            registerTestEvents();
        }
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
        apiBox = new Label("-/-");

        mainStage = stage;
        stage.setMaximized(false);

        panels = new PanelControl(this, prefs);
        guiController = new GUIController(this, panels, apiBox);

        Scene scene = new Scene(createRoot());
        setupMainStage(scene);

        loadFonts();
        String css = initCSS();
        applyCSS(css, scene);
    }

    // Test mode should only be run as a test task (Gradle / JUnit), as quit()
    // leaves the JVM alive during test mode (which is cleaned up by Gradle).
    // Manually feeding --test=true into the command line arguments will leave the JVM
    // running after the HT window has been closed, and thus will require the
    // process to be closed manually afterwards (Force Quit / End Process).
    public boolean isTestMode() {
        return commandLineArgs.getOrDefault("test", "false").equalsIgnoreCase("true") ||
                isBypassLogin() ||
                isTestJSONEnabled() ||
                isTestChromeDriver() ||
                isTestGlobalConfig();
    }

    // Public for use in LoginDialog
    public boolean isTestGlobalConfig() {
        return commandLineArgs.getOrDefault("testconfig", "false").equalsIgnoreCase("true");
    }

    // When --bypasslogin=true is passed as an argument, the username and password
    // are empty strings.
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
            panels.saveSession();
            prefs.saveGlobalConfig();
            Platform.exit();
            System.exit(0);
        }
        if (isTestGlobalConfig()) {
            panels.saveSession();
            prefs.saveGlobalConfig();
        }
    }

    public void onRepoOpened() {
        Platform.runLater(repoSelector::refreshContents);
    }

    /**
     * TODO Stop-gap measure pending a more robust updater
     */
    private void clearCacheIfNecessary() {
        if (getCommandLineArgs().containsKey(ARG_UPDATED_TO)) {
            // TODO
//          CacheFileHandler.deleteCacheDirectory();
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
        mainStage.focusedProperty().addListener((unused, wasFocused, isFocused) -> {
            if (!isFocused) {
                return;
            }
            if (PlatformSpecific.isOnWindows()) {
                browserComponent.focus(UI.mainWindowHandle);
            }
            PlatformEx.runLaterDelayed(() -> {
                if (browserComponent != null) {
                    boolean shouldRefresh = browserComponent.hasBviewChanged();
                    if (shouldRefresh) {
                        logger.info("Browser view has changed; refreshing");
                        logic.refresh();
                        refreshTimer.restart();
                    }
                }
            });
        });
        mainStage.hide();
    }

    private static void getMainWindowHandle(String windowTitle) {
        if (PlatformSpecific.isOnWindows()) {
            mainWindowHandle = User32.INSTANCE.FindWindow(null, windowTitle);
        }
    }

    private HashMap<String, String> initialiseCommandLineArguments() {
        Parameters params = getParameters();
        return new HashMap<>(params.getNamed());
    }

    private Parent createRoot() {

        VBox top = new VBox();

        ScrollPane panelsScrollPane = new ScrollPane(panels);
        panelsScrollPane.getStyleClass().add("transparent-bg");
        panelsScrollPane.setFitToHeight(true);
        panelsScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        HBox.setHgrow(panelsScrollPane, Priority.ALWAYS);

        menuBar = new MenuControl(this, panels, panelsScrollPane, prefs);
        menuBar.setUseSystemMenuBar(true);

        HBox repoSelectorBar = new HBox();
        repoSelectorBar.setAlignment(Pos.CENTER_LEFT);
        apiBox.getStyleClass().add("text-grey");
        apiBox.setTooltip(new Tooltip("Remaining calls / Minutes to next refresh"));
        repoSelectorBar.getChildren().addAll(repoSelector, apiBox);

        top.getChildren().addAll(menuBar, repoSelectorBar);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(panelsScrollPane);
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

        mainStage.setMinWidth(panels.getPanelWidth());
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
        triggerEvent(new PrimaryRepoChangedEvent(repoId));
        logic.openPrimaryRepository(repoId);
        logic.setDefaultRepo(repoId);
    }
    
    public void switchDefaultRepo(){
        String[] openRepos = repoSelector.getContents().toArray(new String[0]);
        String currentRepo = logic.getDefaultRepo();
        
        // Cycle to the next open repository
        for (int i = 0; i < openRepos.length; i++) {
            if (openRepos[i].equals(currentRepo)) {
                if (i == openRepos.length - 1) {
                    primaryRepoChanged(openRepos[0]);
                    repoSelector.setText(openRepos[0]);
                } else {
                    primaryRepoChanged(openRepos[i + 1]);
                    repoSelector.setText(openRepos[i + 1]);
                }
            }
        }
    }

    private void ensureSelectedPanelHasFocus() {
        if (panels.getCurrentlySelectedPanel().isPresent()) {
            getMenuControl().scrollTo(
                panels.getCurrentlySelectedPanel().get(),
                panels.getChildren().size());
            panels.getPanel(panels.getCurrentlySelectedPanel().get()).requestFocus();
        }
    }

    public MenuControl getMenuControl() {
        return menuBar;
    }

    public void setDefaultWidth() {
        mainStage.setMaximized(false);
        Rectangle dimensions = getDimensions();
        mainStage.setMinWidth(panels.getPanelWidth());
        mainStage.setMinHeight(dimensions.getHeight());
        mainStage.setMaxWidth(panels.getPanelWidth());
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
        menuBar.scrollTo(panels.getCurrentlySelectedPanel().get(), panels.getChildren().size());
    }

    public HWND getMainWindowHandle() {
        return mainWindowHandle;
    }
}
