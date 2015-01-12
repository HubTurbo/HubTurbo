package storage;

import java.util.List;
import java.util.Optional;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryId;

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

	public boolean isNonInheritedLabel(String label) {
		return projConfiguration.isNonInheritedLabel(label);
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
}
