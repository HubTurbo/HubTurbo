package storage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryId;

import service.ServiceManager;

/**
 * A singleton for managing all local files used by HubTurbo. It provides
 * facilities for saving and loading all files and methods for accessing them.
 */
public class DataManager {

	private static DataManager instance = null;
	
	public static DataManager getInstance() {
		if (instance == null) {
			instance = new DataManager();
		}
		return instance;
	}

	private static final String FILE_CONFIG_SESSION = "session-config.json";
	private static final String FILE_CONFIG_LOCAL = "local-config.json";

	private ConfigFileHandler fileHandler;
	private SessionConfiguration sessionConfiguration;
	private LocalConfiguration localConfiguration;

	public DataManager() {
		fileHandler = new ConfigFileHandler(FILE_CONFIG_SESSION, FILE_CONFIG_LOCAL);
		initialiseConfigFiles();
	}
	
	/**
	 * Initialises and/or loads all configuration files, except project configuration,
	 * which will be loaded later.
	 */
	private void initialiseConfigFiles() {
		sessionConfiguration = fileHandler.loadSessionConfig();
		localConfiguration = fileHandler.loadLocalConfig();
	}

	/**
	 * Operations
	 */

	public void saveLocalConfig() {
		fileHandler.saveLocalConfig(localConfiguration);
	}

	public void saveSessionConfig() {
		fileHandler.saveSessionConfig(sessionConfiguration);
	}

	public void setFiltersForNextSession(IRepositoryIdProvider project, List<String> filter) {
		sessionConfiguration.setFiltersForNextSession(project, filter);
	}
	
	public void setLastLoginUsername(String name) {
		sessionConfiguration.setLastLoginUsername(name);
	}

	/**
	 * Interface to configuration files
	 */

	/**
	 * Local configuration
	 */

	public String getUserAlias(String user) {
		return localConfiguration.getAlias(user);
	}
	
	private String getCurrentRepoId() {
		return ServiceManager.getInstance().getRepoId().generateId();
	}

	public void addBoard(String name, List<String> filterExprs) {
		assert name != null && filterExprs != null;
		localConfiguration.addBoard(getCurrentRepoId(), name, filterExprs);
	}
	
	public List<String> getBoardPanels(String name) {
		return localConfiguration.getBoardPanels(getCurrentRepoId(), name);
	}
	
	public Map<String, List<String>> getAllBoards() {
		return localConfiguration.getAllBoards(getCurrentRepoId());
	}
	
	public void removeBoard(String name) {
		localConfiguration.removeBoard(getCurrentRepoId(), name);
	}

	/**
	 * Session configuration
	 */

	public List<String> getFiltersFromPreviousSession(IRepositoryIdProvider project) {
		return sessionConfiguration.getFiltersFromPreviousSession(project);
	}

	public void addToLastViewedRepositories(String repository) {
		sessionConfiguration.addToLastViewedRepositories(repository);
	}

	/**
	 * Helper method to get the most recently viewed repository,
	 * allowing for failure if there are none (on first run)
	 * @return
	 */
	public Optional<RepositoryId> getLastViewedRepository() {
		List<String> lastViewed = sessionConfiguration.getLastViewedRepositories();
		if (lastViewed.isEmpty()) {
			return Optional.empty();
		} else {
			String id = lastViewed.get(lastViewed.size()-1);
			return Optional.of(RepositoryId.createFromId(id));
		}
	}

	public List<String> getLastViewedRepositories() {
		return sessionConfiguration.getLastViewedRepositories();
	}

	public String getLastLoginUsername() {
		return sessionConfiguration.getLastLoginUsername();
	}
	
	public void setLastLoginPassword(String password) {
		sessionConfiguration.setLastLoginPassword(password);
	}

	public String getLastLoginPassword() {
		return sessionConfiguration.getLastLoginPassword();
	}

	/**
	 * Testing
	 */
	
	public void setConfigFileHandler(ConfigFileHandler handler) {
		this.fileHandler = handler;
		initialiseConfigFiles();
	}
}
