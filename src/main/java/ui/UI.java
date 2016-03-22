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
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.NotificationPane;
import prefs.Preferences;
import ui.components.HTStatusBar;
import ui.components.KeyboardShortcuts;
import ui.components.StatusUI;
import ui.components.pickers.LabelPicker;
import ui.issuepanel.PanelControl;
import undo.UndoController;
import util.*;
import util.events.*;
import util.events.Event;
import util.events.testevents.PrimaryRepoChangedEvent;
import util.events.testevents.UILogicRefreshEventHandler;

import javax.swing.*;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static ui.components.KeyboardShortcuts.SWITCH_DEFAULT_REPO;

public class UI extends Application implements EventDispatcher {

    public static final int VERSION_MAJOR = 3;
    public static final int VERSION_MINOR = 24;
    public static final int VERSION_PATCH = 0;

    private static final Logger logger = LogManager.getLogger(UI.class.getName());
    private static HWND mainWindowHandle;
    private final GlobalHotkey globalHotkey = new GlobalHotkey(this);

    private static final int REFRESH_PERIOD = 60;

    /**
     * Minimum Java Version Required by HT
     */
    public static final String REQUIRED_JAVA_VERSION = "1.8.0_60";

    public static final String WINDOW_TITLE = "HubTurbo %s (%s)";

    public static final String ARG_UPDATED_TO = "--updated-to";

    private static final String APPLICATION_LOGO_FILENAME = "logo.png";

    public static final String WARNING_MSG_OUTDATED_JAVA_VERSION =
            "Your Java version is older than HubTurbo's requirement. " +
                    "Use it at your own risk.%n%n" +
                    "Required version\t: %s%n" +
                    "Installed version\t: %s";
    public static final String ERROR_MSG_JAVA_RUNTIME_VERSION_PARSING =
            "Java runtime version is not known and may not be compatible with HubTurbo.%n%n" +
                    "Use it at your own risk.%n%nRuntime version: %s";


    // Application-level state

    public UIManager uiManager;
    public Logic logic;
    public static Preferences prefs;
    public static StatusUI status;
    public static EventDispatcher events;
    public EventBus eventBus;
    private TickingTimer refreshTimer;
    public GUIController guiController;
    private NotificationController notificationController;
    public UndoController undoController;


    // Main UI elements

    private Stage mainStage;
    private PanelControl panels;
    private MenuControl menuBar;
    private BrowserComponent browserComponent;
    private ScreenManager screenManager;
    private RepositorySelector repoSelector;
    private Label apiBox;
    private ScrollPane panelsScrollPane;
    private NotificationPane notificationPane;

    @SuppressWarnings("PMD")
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        initPreApplicationState();
        initUI(stage);
        initApplicationState();
        warnIfJavaVersionOutdated();
        login(TestController.isBypassLogin());
    }

    private void login(boolean isBypassLogin) {
        if (isBypassLogin) {
            prefs.setLastLoginCredentials("test", "test");
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

    private void createAndLoadSampleBoard(){
        BoardAutoCreator boardCreator = new BoardAutoCreator(this, panels, prefs);
        boardCreator.createSampleBoard(false);
    }

    private void disableUI(boolean disable) {
        mainStage.setResizable(!disable);
        menuBar.setDisable(disable);
        repoSelector.setDisable(disable);
    }

    private void showMainWindow(String repoId) {
        //We infer this is the first time HT is being used if there are no repo data stored at the start up.
        //This check needs to be done at the very beginning of the startup, before HT downloads any repo data.
        boolean isAFirstTimeUser = logic.getStoredRepos().isEmpty();
        logic.openPrimaryRepository(repoId);
        logic.setDefaultRepo(repoId);
        repoSelector.setText(repoId);
        triggerEvent(new PrimaryRepoChangedEvent(repoId));

        triggerEvent(new BoardSavedEvent()); // Initializes boards

        if (TestController.isTestMode()) {
            if (TestController.isTestChromeDriver()) {
                browserComponent = new BrowserComponent(this, screenManager, true);
                browserComponent.initialise();
            } else {
                browserComponent = new BrowserComponentStub(this);
            }
        } else {
            browserComponent = new BrowserComponent(this, screenManager, false);
            browserComponent.initialise();
        }

        panels.init(guiController, panelsScrollPane);
        // Should only be called after panels have been initialized
        ensureSelectedPanelHasFocus();
        initialisePickers();
        if (isAFirstTimeUser && TestController.shouldOpenSampleBoard()){
            createAndLoadSampleBoard();
        }
    }

    private void initialisePickers() {
        new LabelPicker(this, mainStage);
    }

    protected void registerTestEvents() {
        registerEvent((UILogicRefreshEventHandler) e -> Platform.runLater(logic::refresh));
    }

    private void initPreApplicationState() {
        logger.info(Utility.version(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH));
        UI.events = this;

        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) ->
                logger.error(throwable.getMessage(), throwable));

        TestController.setUI(this, getParameters());
        prefs = TestController.loadApplicationPreferences();
        KeyboardShortcuts.loadKeyboardShortcuts(prefs);

        eventBus = new EventBus();
        if (TestController.isTestMode()) {
            registerTestEvents();
        }
        registerEvent((UnusedStoredReposChangedEventHandler) e -> onRepoOpened());
        registerEvent((UsedReposChangedEventHandler) e -> removeUnusedModelsAndUpdate());

        uiManager = new UIManager(this);
        status = new HTStatusBar(this);
    }

    private void initApplicationState() {
        // In the future, when more arguments are passed to logic,
        // we can pass them in the form of an array.
        logic = new Logic(uiManager, prefs, Optional.empty(), Optional.empty());
        // TODO clear cache if necessary
        refreshTimer = new TickingTimer("Refresh Timer", REFRESH_PERIOD,
            status::updateTimeToRefresh, logic::refresh, TimeUnit.SECONDS);
        refreshTimer.start();
        undoController = new UndoController(notificationController);
    }

    private void initUI(Stage stage) {
        repoSelector = createRepoSelector();
        apiBox = new Label("-/-");
        apiBox.setId("apiBox");

        mainStage = stage;
        stage.setMaximized(false);

        panels = new PanelControl(this, prefs);
        guiController = new GUIController(this, panels, apiBox);

        Scene scene = new Scene(createRootNode());

        setupMainStage(scene);
        setupGlobalKeyboardShortcuts(scene);

        screenManager = new ScreenManager(mainStage);
        screenManager.setupStageDimensions(mainStage, panels.getPanelWidth());
        screenManager.setupPositionListener(stage);

        notificationController = new NotificationController(notificationPane);
        notificationPane.setId("notificationPane");

        loadFonts();
        String css = initCSS();
        applyCSS(css, scene);

        setApplicationIcon(stage);
    }

    public void setApplicationIcon(Stage stage) {
        stage.getIcons().add(new Image(UI.class.getResourceAsStream(APPLICATION_LOGO_FILENAME)));

        // set Icon for OSX
        // - need to use Apple Java Extension, using reflection to load the
        //   class so that HubTurbo is compilable
        if (PlatformSpecific.isOnMac()) {
            try {
                Class util = Class.forName("com.apple.eawt.Application");
                Method getApplication = util.getMethod("getApplication", new Class[0]);
                Object application = getApplication.invoke(util);
                Class params[] = new Class[1];
                params[0] = java.awt.Image.class;
                Method setDockIconImage = util.getMethod("setDockIconImage", params);
                setDockIconImage.invoke(application,
                        new ImageIcon(UI.class.getResource(APPLICATION_LOGO_FILENAME)).getImage());
            } catch (Exception e) {
                logger.info("Not OSX", e);
            }
        }

    }

    public void quit() {
        globalHotkey.quit();
        if (browserComponent != null) {
            browserComponent.onAppQuit();
        }
        if (!TestController.isTestMode() || TestController.isTestGlobalConfig()) {
            panels.saveSession();
            prefs.saveGlobalConfig();
        }
        if (!TestController.isTestMode() || TestController.isCloseOnQuit()) {
            Platform.exit();
            System.exit(0);
        }
    }

    public void onRepoOpened() {
        Platform.runLater(repoSelector::refreshContents);
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
        updateTitle();
        mainStage.setScene(scene);
        mainStage.show();
        mainStage.setOnCloseRequest(e -> quit());
        mainStage.focusedProperty().addListener((unused, wasFocused, isFocused) -> {
            if (!isFocused) {
                return;
            }
            if (browserComponent != null && PlatformSpecific.isOnWindows()) {
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

    private void setupGlobalKeyboardShortcuts(Scene scene) {
        globalHotkey.init();
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (SWITCH_DEFAULT_REPO.match(event)) {
                switchDefaultRepo();
            }
        });
    }

    private static void getMainWindowHandle(String windowTitle) {
        if (PlatformSpecific.isOnWindows()) {
            mainWindowHandle = User32.INSTANCE.FindWindow(null, windowTitle);
        }
    }

    private Parent createRootNode() {

        VBox top = new VBox();

        panelsScrollPane = new ScrollPane(panels);
        panelsScrollPane.getStyleClass().add("transparent-bg");
        panelsScrollPane.setFitToHeight(true);
        panelsScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        HBox.setHgrow(panelsScrollPane, Priority.ALWAYS);
        menuBar = new MenuControl(this, panels, panelsScrollPane, prefs, mainStage);
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

        notificationPane = new NotificationPane(root);

        return notificationPane;
    }

    public Set<String> getCurrentlyUsedRepos() {
        Set<String> currentlyUsedRepos = new HashSet<>();
        String defaultRepo = logic.getDefaultRepo();
        currentlyUsedRepos.addAll(panels.getRepositoriesReferencedOnAllPanels());
        if (!Utility.convertSetToLowerCase(currentlyUsedRepos)
                .contains(defaultRepo.toLowerCase())) {
            currentlyUsedRepos.add(defaultRepo);
        }

        return currentlyUsedRepos;
    }

    public void removeUnusedModelsAndUpdate() {
        logic.removeUnusedModels(Utility.convertSetToLowerCase(getCurrentlyUsedRepos()));

        triggerEvent(new UnusedStoredReposChangedEvent());
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

    public HashMap<String, String> getCommandLineArgs() {
        return TestController.getCommandLineArgs();
    }

    private RepositorySelector createRepoSelector() {
        RepositorySelector repoSelector = new RepositorySelector(this);
        repoSelector.setOnValueChange(this::primaryRepoChanged);
        return repoSelector;
    }

    private void primaryRepoChanged(String repoId) {
        triggerEvent(new PrimaryRepoChangedEvent(repoId));
        logic.setDefaultRepo(repoId);
        logic.openPrimaryRepository(repoId);
        triggerEvent(new UsedReposChangedEvent());
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

        triggerEvent(new UnusedStoredReposChangedEvent());
    }

    private void ensureSelectedPanelHasFocus() {
        if (panels.getCurrentlySelectedPanel().isPresent()) {
            panels.scrollToCurrentlySelectedPanel();
            panels.getPanel(panels.getCurrentlySelectedPanel().get()).requestFocus();
        }
    }

    public MenuControl getMenuControl() {
        return menuBar;
    }

    public PanelControl getPanelControl() {
        return panels;
    }

    /**
     * Returns focus to UI mainStage. Invoked to eliminate NoNodesVisibleException.
     */
    public void showMainStage() {
        mainStage.show();
    }

    public void setDefaultWidth() {
        mainStage.setMaximized(false);
        mainStage.setIconified(false);
        Rectangle dimensions = screenManager.getDimensions();
        mainStage.setWidth(panels.getPanelWidth());
        mainStage.setHeight(dimensions.getHeight());
        mainStage.setX(0);
        mainStage.setY(0);
    }

    public void maximizeWindow() {
        mainStage.setMaximized(true);
        Rectangle dimensions = screenManager.getDimensions();
        mainStage.setWidth(dimensions.getWidth());
        mainStage.setHeight(dimensions.getHeight());
        mainStage.setX(0);
        mainStage.setY(0);
    }

    public void minimizeWindow() {
        mainStage.setIconified(true);
        panels.scrollToCurrentlySelectedPanel();
    }

    public HWND getMainWindowHandle() {
        return mainWindowHandle;
    }

    public void triggerNotificationAction() {
        notificationController.triggerNotificationAction();
    }

    public void hideNotification() {
        notificationController.hideNotification();
    }

    public void updateTitle() {
        String openBoard = prefs.getLastOpenBoard().orElse("none");
        String version = Utility.version(VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH);
        String title = String.format(WINDOW_TITLE, version, openBoard);
        mainStage.setTitle(title);
    }

    public boolean isNotificationPaneShowing() {
        return notificationPane.isShowing();
    }

    public String getTitle() {
        return mainStage.getTitle();
    }

    public boolean isWindowMinimized() {
        return mainStage.isIconified();
    }

    public boolean isWindowFocused() {
        return mainStage.isFocused();
    }

    /**
     * Warns user if the Java runtime version is lower than HT's requirement
     */
    private void warnIfJavaVersionOutdated() {
        JavaVersion requiredVersion;
        try {
            requiredVersion = JavaVersion.fromString(REQUIRED_JAVA_VERSION);
        } catch (IllegalArgumentException e) {
            logger.error("Required Java Version string cannot be parsed. This should have been covered by test.");
            assert false;
            return;
        }

        JavaVersion runtimeVersion;
        String javaRuntimeVersionString = System.getProperty("java.runtime.version");
        try {
            runtimeVersion = JavaVersion.fromString(javaRuntimeVersionString);
        } catch (IllegalArgumentException e) {
            logger.error("Runtime Java Version string cannot be parsed. Look at Java Doc about other version format.");
            showJavaRuntimeVersionNotCompatible(System.getProperty("java.runtime.version"));
            return;
        }

        if (JavaVersion.isJavaVersionLower(runtimeVersion, requiredVersion)) {
            showJavaVersionOutdatedWarning(runtimeVersion, requiredVersion);
        }
    }

    private void showJavaVersionOutdatedWarning(JavaVersion runtimeVersion, JavaVersion requiredVersion) {
        String message = String.format(WARNING_MSG_OUTDATED_JAVA_VERSION,
                                       requiredVersion.toString(), runtimeVersion.toString());
        DialogMessage.showInformationDialog("Update your Java version", message);
    }

    private void showJavaRuntimeVersionNotCompatible(String javaRuntimeVersionString) {
        String message = String.format(ERROR_MSG_JAVA_RUNTIME_VERSION_PARSING, javaRuntimeVersionString);
        DialogMessage.showInformationDialog("Java version unknown", message);
    }
}
