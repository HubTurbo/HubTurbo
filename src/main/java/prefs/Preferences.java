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
    public static final String GLOBAL_CONFIG_FILE = "global.json";
    public static final String TEST_CONFIG_FILE = "test.json";

    private final ConfigFileHandler fileHandler;

    public GlobalConfig global;

    private Preferences(String configFileName, boolean createUnconditionally) {
        this.fileHandler = new ConfigFileHandler(DIRECTORY, configFileName);

        if (createUnconditionally) {
            initGlobalConfig();
        } else {
            loadGlobalConfig();
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

    public void saveGlobalConfig() {
        fileHandler.saveGlobalConfig(global);
    }

    private void initGlobalConfig() {
        global = fileHandler.initGlobalConfig();
    }

    private void loadGlobalConfig() {
        global = fileHandler.loadGlobalConfig();
    }

    // Last login credentials. While the main UI is running (i.e. logged in successfully), last login
    // credentials are guaranteed to be the current user's credentials thanks to setLastLoginCredentials
    // being called immediately after a successful login in LoginDialog.
    public String getLastLoginPassword() {
        return global.getLastLoginPassword();
    }

    public String getLastLoginUsername() {
        return global.getLastLoginUsername();
    }

    public void setLastLoginCredentials(String username, String password) {
        global.setLastLoginCredentials(username, password);
    }

    public List<String> getLastOpenFilters() {
        return global.getLastOpenFilters();
    }

    public List<String> getPanelNames() {
        return global.getPanelNames();
    }
    
    public List<PanelInfo> getPanelInfo() {
        return global.getPanelInfo();
    }
    
    public void setPanelInfo(List<PanelInfo> panelInfo) {
        global.setPanelInfo(panelInfo);
    }

    /**
     * Interface to configuration files
     */

    /**
     * Boards
     */

    public void addBoard(String name, List<PanelInfo> panels) {
        assert name != null && panels != null;
        global.addBoard(name, panels);
    }

    public Map<String, List<PanelInfo>> getAllBoards() {
        return global.getAllBoards();
    }

    public List<String> getAllBoardNames() {
        return new ArrayList<>(getAllBoards().keySet());
    }

    public void removeBoard(String name) {
        global.removeBoard(name);
    }

    public void setLastDeletedBoardIndex(String deletedBoardName){
        List<String> boardNames = getAllBoardNames();
        int index = boardNames.indexOf(deletedBoardName);
        global.setLastDeletedBoardIndex(index);
    }

    public Optional<Integer> getLastDeletedBoardIndex() {
        return global.getLastDeletedBoardIndex();
    }
    
    public void setLastOpenBoard(String board) {
        global.setLastOpenBoard(board);
    }
    
    public Optional<String> getLastOpenBoard() {
        return global.getLastOpenBoard();
    }
    
    public Optional<String> switchBoard() {
        setBoardToSwitch();
        return getLastOpenBoard();
    }

    /**
     * Sets the lastOpenBoard to the board to switch to
     */
    private void setBoardToSwitch(){
        List<String> boardNames = getAllBoardNames();
        boolean hasLastOpenBoard = getLastOpenBoard().isPresent();
        boolean hasLastDeletedBoardOpen = getLastDeletedBoardIndex().isPresent();
        int index = 0;

        if (getAllBoardNames().size() <= 1 ) {
            return;
        }

        if (hasLastOpenBoard) {
            int lastBoard = boardNames.indexOf(getLastOpenBoard().get());
            index = (lastBoard + 1) % boardNames.size();
        } else if (hasLastDeletedBoardOpen) {
            int lastDeletedBoardIndex = getLastDeletedBoardIndex().get();
            index = lastDeletedBoardIndex % boardNames.size();
        }

        setLastOpenBoard(boardNames.get(index));
    }
    
    public void clearLastOpenBoard() {
        global.clearLastOpenBoard();
    }
    
    public List<PanelInfo> getBoardPanels(String board) {
        return global.getBoardPanels(board);
    }

    public void clearAllBoards() {
        global.clearAllBoards();
    }

    /**
     * Session configuration
     */

    public void setLastViewedRepository(String repository) {
        global.setLastViewedRepository(repository);
    }

    public Optional<RepositoryId> getLastViewedRepository() {
        if (global.getLastViewedRepository().isEmpty()) {
            return Optional.empty();
        } else {
            RepositoryId repositoryId = RepositoryId.createFromId(global.getLastViewedRepository());
            if (repositoryId == null) {
                return Optional.empty();
            } else {
                return Optional.of(repositoryId);
            }
        }
    }

    public void clearMarkedReadAt(String repoId, int issue) {
        global.clearMarkedReadAt(repoId, issue);
    }

    public void setMarkedReadAt(String repoId, int issue, LocalDateTime time) {
        global.setMarkedReadAt(repoId, issue, time);
    }

    public Optional<LocalDateTime> getMarkedReadAt(String repoId, int issue) {
        return global.getMarkedReadAt(repoId, issue);
    }

    public Map<String, String> getKeyboardShortcuts() {
        return global.getKeyboardShortcuts();
    }

    public void setKeyboardShortcuts(Map<String, String> keyboardShortcuts) {
        global.setKeyboardShortcuts(keyboardShortcuts);
    }
}
