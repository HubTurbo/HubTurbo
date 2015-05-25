package prefs;

import org.eclipse.egit.github.core.RepositoryId;
import ui.UI;
import ui.issuecolumn.ColumnControl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Preferences {

	public static final String DIRECTORY = "settings";
	private static final String GLOBAL_CONFIG_FILE = "global.json";

	private final ConfigFileHandler fileHandler;
	private final UI ui;
	private final ColumnControl columns;

	public GlobalConfig global;

	public Preferences(UI ui, ColumnControl columns) {
		this.ui = ui;
		this.columns = columns;
		this.fileHandler = new ConfigFileHandler(DIRECTORY, GLOBAL_CONFIG_FILE);

		loadGlobalConfig();
	}

	public void saveGlobalConfig() {
		fileHandler.saveGlobalConfig(global);
	}

	public void loadGlobalConfig() {
		global = fileHandler.loadGlobalConfig();
	}

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

	public void setLastOpenFilters(List<String> filter) {
		global.setLastOpenFilters(filter);
	}

	/**
	 * Interface to configuration files
	 */

	/**
	 * Boards
	 */

	public void addBoard(String name, List<String> filterExprs) {
		assert name != null && filterExprs != null;
		global.addBoard(name, filterExprs);
	}

//	public List<String> getBoardPanels(String name) {
//		return global.getBoardPanels(name);
//	}

	public Map<String, List<String>> getAllBoards() {
		return global.getAllBoards();
	}

	public void removeBoard(String name) {
		global.removeBoard(name);
	}

	/**
	 * Session configuration
	 */

	public void addToLastViewedRepositories(String repository) {
		global.addToLastViewedRepositories(repository);
	}

	/**
	 * Helper method to get the most recently viewed repository,
	 * allowing for failure if there are none (on first run)
	 * @return
	 */
	public Optional<RepositoryId> getLastViewedRepository() {
		List<String> lastViewed = global.getLastViewedRepositories();
		if (lastViewed.isEmpty()) {
			return Optional.empty();
		} else {
			String id = lastViewed.get(lastViewed.size()-1);
			return Optional.of(RepositoryId.createFromId(id));
		}
	}

	public List<String> getLastViewedRepositories() {
		return global.getLastViewedRepositories();
	}

	/**
	 * Testing
	 */

//	public void setConfigFileHandler(ConfigFileHandler handler) {
//		this.fileHandler = handler;
//		init();
//	}
//
}
