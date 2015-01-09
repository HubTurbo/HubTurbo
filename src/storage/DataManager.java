package storage;

import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;

/**
 * A singleton for managing all local files used by HubTurbo. It provides
 * facilities for saving and loading all files and methods for accessing them.
 */
public class DataManager {
	private static final DataManager dataManagerInstance = new DataManager();

	private ConfigFileHandler fileHandler;
	private SessionConfiguration sessionConfiguration;
	private ProjectConfiguration projConfiguration;
	private LocalConfiguration localConfiguration;

	protected DataManager() {
		fileHandler = new ConfigFileHandler();

		sessionConfiguration = fileHandler.loadSessionConfig();
		localConfiguration = fileHandler.loadLocalConfig();

		// Actually loaded later via loadProjectConfig
		projConfiguration = new ProjectConfiguration();
	}

	public static DataManager getInstance() {
		return dataManagerInstance;
	}

	/**
	 * Operations
	 */

	public void loadProjectConfig(IRepositoryIdProvider repoId) {
		projConfiguration = fileHandler.loadProjectConfig(repoId);
	}

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

	/**
	 * Project configuration
	 */

	public List<String> getStatusLabels() {
		return projConfiguration.getStatusLabels();
	}

	public boolean isNonInheritedLabel(String label) {
		return projConfiguration.isNonInheritedLabel(label);
	}

	public boolean isStatusLabel(String label) {
		return projConfiguration.isStatusLabel(label);
	}

	public boolean isOpenStatusLabel(String label) {
		return projConfiguration.isOpenStatusLabel(label);
	}

	public boolean isClosedStatusLabel(String label) {
		return projConfiguration.isClosedStatusLabel(label);
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

	public List<String> getLastViewedRepositories() {
		return sessionConfiguration.getLastViewedRepositories();
	}

	public String getLastLoginUsername() {
		return sessionConfiguration.getLastLoginUsername();
	}
}
