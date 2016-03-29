package prefs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.egit.github.core.RepositoryId;
import util.FileHelper;
import util.HTLog;
import util.JsonHelper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents persistent user configuration.
 *
 * The Preferences class is the facade of all configuration and preferences information. All preferences and config
 * settings can be set in this class.
 *
 * Use either Preferences.create() or Preferences.load() to obtain a Preferences instance.
 *
 * Overrides PMD's recommendation that this class should be final.
 * It cannot be as we need to mock it.
 * 
 */
public class Preferences { // NOPMD

    private static final Logger logger = LogManager.getLogger(Preferences.class.getName());

    public static final String DIRECTORY = "settings";

    public static final String SESSION_CONFIG_FILENAME = "global.json";
    public static final String USER_CONFIG_FILENAME = "user.json";

    private static final String DEFAULT_FILE_CONTENTS = "{}";

    private final String configDirectory;
    private final String sessionConfigFileName;
    private final String userConfigFileName;

    private final SessionConfig sessionConfig;
    private final UserConfig userConfig;

    /**
     * Initialises a Preferences instance that contains the configurations in the config files specified.
     * @param configDirectory The directory that the config files are held in.
     * @param sessionConfigFileName The name of the session config file to load.
     * @param userConfigFileName The name of the user config file to load.
     * @return The initialised Preferences instance.
     */
    public static Preferences load(String configDirectory, String sessionConfigFileName, String userConfigFileName) {
        return new Preferences(configDirectory, sessionConfigFileName, userConfigFileName, false);
    }

    /**
     * Initialises a Preferences instance that creates new config files.
     * @param configDirectory The directory that the config files are held in.
     * @param sessionConfigFileName The name of the session config file to create.
     * @param userConfigFileName The name of the user config file to create.
     * @return The initialised Preferences instance.
     */
    public static Preferences create(String configDirectory, String sessionConfigFileName, String userConfigFileName) {
        return new Preferences(configDirectory, sessionConfigFileName, userConfigFileName, true);
    }

    /**
     * @param configDirectory The directory that the config files are held in.
     * @param sessionConfigFileName The name of the session config file
     * @param userConfigFileName The name of the user config file
     * @param isIgnoreExisting True if existing config file is to be ignored.
     */
    private Preferences(String configDirectory,
                        String sessionConfigFileName,
                        String userConfigFileName,
                        boolean isIgnoreExisting) {
        this.configDirectory = configDirectory;
        this.sessionConfigFileName = sessionConfigFileName;
        this.userConfigFileName = userConfigFileName;

        if (isIgnoreExisting) {
            this.sessionConfig = createConfig(DEFAULT_FILE_CONTENTS, SessionConfig.class);
            this.userConfig = createConfig(DEFAULT_FILE_CONTENTS, UserConfig.class);
        } else {
            this.sessionConfig = loadConfig(configDirectory, sessionConfigFileName, SessionConfig.class);
            this.userConfig = loadConfig(configDirectory, userConfigFileName, UserConfig.class);
        }
    }

    /**
     * Creates a new Config instance
     * @param contents The JSON representation of an contents of the Config object
     * @param configClass The class of the Config instance
     * @return The created Config instance
     */
    private <T> T createConfig(String contents, Class<T> configClass) {
        return JsonHelper.fromJsonString(contents, configClass);
    }

    /**
     * Loads a Config instance from a file on disk
     * @param configDirectory The directory that the config file is held in.
     * @param configFileName The filename of the config file on disk
     * @param configClass The class of the config to load
     * @return The loaded Config instance
     */
    private <T> T loadConfig(String configDirectory, String configFileName, Class<T> configClass) {
        String fileContents = DEFAULT_FILE_CONTENTS;
        if (FileHelper.isFileExists(configDirectory, configFileName)) {
            try {
                fileContents = FileHelper.getFileContents(configDirectory, configFileName);
            } catch (IOException e) {
                // if we can't read the file, just ignore the existing files
                HTLog.error(logger, e);
                logger.error(e.toString());
            }
        }

        return createConfig(fileContents, configClass);
    }

    /**
     * Saves the session and user configs to file
     */
    private void save() {
        try {
            saveConfig(sessionConfig, configDirectory, sessionConfigFileName, SessionConfig.class);

        } catch (IOException e) {
            HTLog.error(logger, e);
            logger.error("Could not save session config");
        }

        try {
            saveConfig(userConfig, configDirectory, userConfigFileName, UserConfig.class);

        } catch (IOException e) {
            HTLog.error(logger, e);
            logger.error("Could not save user config");
        }
    }

    /**
     * Saves a Config to file
     * @param config The config object to save.
     * @param configDirectory The directory that the config file is held in.
     * @param configFileName The file name of the config file on disk
     * @param configClass The class of the config object
     */
    private <T> void saveConfig(T config, String configDirectory, String configFileName, Class configClass)
            throws IOException {
        String jsonString = JsonHelper.toJsonString(config, configClass);
        FileHelper.writeFileContents(configDirectory, configFileName, jsonString);
    }

    public String getLastLoginPassword() {
        return sessionConfig.getLastLoginPassword();
    }

    public String getLastLoginUsername() {
        return sessionConfig.getLastLoginUsername();
    }

    public void setLastLoginCredentials(String lastLoginUsername, String lastLoginPassword) {
        sessionConfig.setLastLoginCredentials(lastLoginUsername, lastLoginPassword);
        save();
    }

    public List<PanelInfo> getPanelInfo() {
        return sessionConfig.getPanelInfo();
    }

    public void setPanelInfo(List<PanelInfo> panelInfo) {
        sessionConfig.setPanelInfo(panelInfo);
        save();
    }

    public void addBoard(String boardName, List<PanelInfo> panelsInBoard) {
        assert boardName != null && panelsInBoard != null;
        sessionConfig.addBoard(boardName, panelsInBoard);
        save();
    }

    public Map<String, List<PanelInfo>> getAllBoards() {
        return sessionConfig.getAllBoards();
    }

    public List<String> getAllBoardNames() {
        return new ArrayList<>(getAllBoards().keySet());
    }

    public void removeBoard(String boardName) {
        sessionConfig.removeBoard(boardName);
        save();
    }

    public void setLastOpenBoard(String boardName) {
        sessionConfig.setLastOpenBoard(boardName);
        save();
    }

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

    public void clearLastOpenBoard() {
        sessionConfig.clearLastOpenBoard();
        save();
    }

    public List<PanelInfo> getBoardPanels(String boardName) {
        return sessionConfig.getBoardPanels(boardName);
    }

    public void clearAllBoards() {
        sessionConfig.clearAllBoards();
        save();
    }

    public void setLastViewedRepository(String repositoryName) {
        sessionConfig.setLastViewedRepository(repositoryName);
        save();
    }

    public Optional<RepositoryId> getLastViewedRepository() {
        if (sessionConfig.getLastViewedRepository().isEmpty()) {
            return Optional.empty();
        }
        RepositoryId repositoryId = RepositoryId.createFromId(sessionConfig.getLastViewedRepository());
        if (repositoryId == null) {
            return Optional.empty();
        }
        return Optional.of(repositoryId);
    }

    /**
     * Clears marked read at of an issue at the specified repo
     * @param repoId The repo that this issue resides in
     * @param issue The issue to clear
     */
    public void clearMarkedReadAt(String repoId, int issue) {
        sessionConfig.clearMarkedReadAt(repoId, issue);
        save();
    }

    /**
     * Sets the marked read at of an issue in a repo, to a certain time
     * @param repoId The repo of the issue
     * @param issue The issue to set
     * @param timeReadAt The time it was marked read
     */
    public void setMarkedReadAt(String repoId, int issue, LocalDateTime timeReadAt) {
        sessionConfig.setMarkedReadAt(repoId, issue, timeReadAt);
        save();
    }

    /**
     * Retrieves the marked read at of a specified issue in a repo
     * @param repoId The repo of the issue
     * @param issue The issue to retrieve the marked read at
     * @return An Optional of the marked read at
     */
    public Optional<LocalDateTime> getMarkedReadAt(String repoId, int issue) {
        return sessionConfig.getMarkedReadAt(repoId, issue);
    }

    public Map<String, String> getKeyboardShortcuts() {
        return sessionConfig.getKeyboardShortcuts();
    }

    public void setKeyboardShortcuts(Map<String, String> keyboardShortcuts) {
        sessionConfig.setKeyboardShortcuts(keyboardShortcuts);
        save();
    }
}
