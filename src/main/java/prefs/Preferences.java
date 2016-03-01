package prefs;

import org.eclipse.egit.github.core.RepositoryId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents persistent user configuration. Maps to a file on disk.
 *
 * Overrides PMD's recommendation that this class should be final.
 * It cannot be as we need to mock it.
 */
public class Preferences { // NOPMD

    public static final String DIRECTORY = "settings";

    // Standard config filenames used for application and testing
    public static final String SESSION_CONFIG_FILENAME = "global.json";
    public static final String TEST_SESSION_CONFIG_FILENAME = "test.json";

    private final ConfigFile sessionConfigFile;

    private SessionConfig sessionConfig;

    /**
     * Private constructor that prevents external instantiation
     * @param configFileName The name of the preferences file
     * @param createUnconditionally True if the Preferences are to be initialised regardless of previous
     *                              saved preferences
     */
    private Preferences(String configFileName, boolean createUnconditionally) {
        this.sessionConfigFile = new ConfigFile(DIRECTORY, configFileName);

        if (createUnconditionally) {
            initSessionConfig();
        } else {
            loadSessionConfig();
        }
    }

    /**
     * Initialises a Preferences instance which creates its config file, or loads
     * from it if it already exists.
     */
    public static Preferences load(String configFileName) {
        return new Preferences(configFileName, false);
    }

    /**
     * Initialises a Preferences instance which always creates its config file.
     * This will overwrite it with a default configuration if it already exists.
     */
    public static Preferences create(String configFileName) {
        return new Preferences(configFileName, true);
    }

    /**
     * Saves the session config
     */
    public void saveSessionConfig() {
        sessionConfigFile.saveConfig(sessionConfig);
    }

    /**
     * Intiialises the session config
     */
    private void initSessionConfig() {
        sessionConfig = new SessionConfig();
        sessionConfigFile.saveConfig(sessionConfig);
    }

    /**
     * Loads the session config.
     */
    private void loadSessionConfig() {
        sessionConfig = (SessionConfig) sessionConfigFile.loadConfig(SessionConfig.class).orElse(new SessionConfig());
    }

    // Last login credentials. While the main UI is running (i.e. logged in successfully), last login
    // credentials are guaranteed to be the current user's credentials thanks to setLastLoginCredentials
    // being called immediately after a successful login in LoginDialog.
    public String getLastLoginPassword() {
        return sessionConfig.getLastLoginPassword();
    }

    /**
     * Retrieves the last login username
     */
    public String getLastLoginUsername() {
        return sessionConfig.getLastLoginUsername();
    }

    /**
     * Sets the last login credentials
     * @param username The username used during the last login
     * @param password The password used during the last login
     */
    public void setLastLoginCredentials(String username, String password) {
        sessionConfig.setLastLoginCredentials(username, password);
    }

    /**
     * Retrieves a list of the last open filters
     */
    public List<String> getLastOpenFilters() {
        return sessionConfig.getLastOpenFilters();
    }

    /**
     * Retrieves a list of panel names
     */
    public List<String> getPanelNames() {
        return sessionConfig.getPanelNames();
    }

    /**
     * Retrieves a list of panel infos
     */
    public List<PanelInfo> getPanelInfo() {
        return sessionConfig.getPanelInfo();
    }

    /**
     * Sets the panel infos
     * @param panelInfo The list of panel infos to set to
     */
    public void setPanelInfo(List<PanelInfo> panelInfo) {
        sessionConfig.setPanelInfo(panelInfo);
    }

    /**
     * Interface to configuration files
     */

    /**
     * Boards
     */

    public void addBoard(String name, List<PanelInfo> panels) {
        assert name != null && panels != null;
        sessionConfig.addBoard(name, panels);
    }

    /**
     * Retrieves a map of all the boards
     */
    public Map<String, List<PanelInfo>> getAllBoards() {
        return sessionConfig.getAllBoards();
    }

    /**
     * Retrieves a list of all the board names
     */
    public List<String> getAllBoardNames() {
        return new ArrayList<>(getAllBoards().keySet());
    }

    /**
     * Removes the board specified
     * @param name The name of the board to remove
     */
    public void removeBoard(String name) {
        sessionConfig.removeBoard(name);
    }

    /**
     * Sets the last open board
     * @param board The name of the last open board.
     */
    public void setLastOpenBoard(String board) {
        sessionConfig.setLastOpenBoard(board);
    }

    /**
     * Retrieves the name of the last open board.
     * @return An Optional of a nullable board name
     */
    public Optional<String> getLastOpenBoard() {
        return sessionConfig.getLastOpenBoard();
    }

    /**
     * Switches the board to the next one. Cycles through the boards one at a time.
     * @return The new board selected
     */
    public Optional<String> switchBoard() {
        if (getLastOpenBoard().isPresent() && getAllBoards().size() > 1) {
            List<String> boardNames = getAllBoardNames();
            int lastBoard = boardNames.indexOf(getLastOpenBoard().get());
            int index = (lastBoard + 1) % boardNames.size();
            
            setLastOpenBoard(boardNames.get(index));
        }
        
        return getLastOpenBoard();
    }

    /**
     * Clears the last open board
     */
    public void clearLastOpenBoard() {
        sessionConfig.clearLastOpenBoard();
    }

    /**
     * Retrieves the panels of a board.
     * @param boardName The name of the board to retrieve the panels from
     * @return A list of panel infos from that board
     */
    public List<PanelInfo> getBoardPanels(String boardName) {
        return sessionConfig.getBoardPanels(boardName);
    }

    /**
     * Clears all boards.
     */
    public void clearAllBoards() {
        sessionConfig.clearAllBoards();
    }

    /**
     * Session configuration
     */

    /**
     * Sets the last viewed repo to the specified repo
     * @param repository The last viewed repo
     */
    public void setLastViewedRepository(String repository) {
        sessionConfig.setLastViewedRepository(repository);
    }

    /**
     * Retrieves the last viewed repo.
     * @return An Optional of the last viewed repo.
     */
    public Optional<RepositoryId> getLastViewedRepository() {
        if (sessionConfig.getLastViewedRepository().isEmpty()) {
            return Optional.empty();
        } else {
            RepositoryId repositoryId = RepositoryId.createFromId(sessionConfig.getLastViewedRepository());
            if (repositoryId == null) {
                return Optional.empty();
            } else {
                return Optional.of(repositoryId);
            }
        }
    }

    /**
     * Clears marked read at of an issue at the specified repo
     * @param repoId The repo that this issue resides in
     * @param issue The issue to clear
     */
    public void clearMarkedReadAt(String repoId, int issue) {
        sessionConfig.clearMarkedReadAt(repoId, issue);
    }

    /**
     * Sets the marked read at for an issue in a repo, to a certain time
     * @param repoId The repo of the issue
     * @param issue The issue to set
     * @param time The time it was marked read
     */
    public void setMarkedReadAt(String repoId, int issue, LocalDateTime time) {
        sessionConfig.setMarkedReadAt(repoId, issue, time);
    }

    /**
     * Retrieves the marked read at for a specified issue in a repo
     * @param repoId The repo of the issue
     * @param issue The issue to retrieve the marked read at
     * @return An Optional of the marked read at
     */
    public Optional<LocalDateTime> getMarkedReadAt(String repoId, int issue) {
        return sessionConfig.getMarkedReadAt(repoId, issue);
    }

    /**
     * Retrieves the map of keyboard shortcuts
     */
    public Map<String, String> getKeyboardShortcuts() {
        return sessionConfig.getKeyboardShortcuts();
    }

    /**
     * Sets the keyboard shortcuts to the specified mapping
     * @param keyboardShortcuts The mapping of keyboard shortcuts to set to.
     */
    public void setKeyboardShortcuts(Map<String, String> keyboardShortcuts) {
        sessionConfig.setKeyboardShortcuts(keyboardShortcuts);
    }
}
