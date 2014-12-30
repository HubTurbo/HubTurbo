package storage;

import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;

/**
 * A singleton for managing all local files used by HubTurbo.
 * It provides facilities for saving and loading all files and
 * methods for accessing them.
 */
public class DataManager {
	private static final DataManager dataManagerInstance = new DataManager();
	
	private ConfigFileHandler fileHandler;
	private SessionConfigurations sessionConfigurations;
	private ProjectConfigurations projConfigurations;
	private LocalConfigurations localConfigurations;
	
	protected DataManager(){
		fileHandler = new ConfigFileHandler();
		projConfigurations = new ProjectConfigurations();
		sessionConfigurations = fileHandler.loadSessionConfig();
		localConfigurations = fileHandler.loadLocalConfig();
	}
	
	public static DataManager getInstance(){
		return dataManagerInstance;
	}
	
	/**
	 * Operations
	 */
	
	public void saveLocalConfig(){
		fileHandler.saveLocalConfig(localConfigurations);
	}
	
	public void loadProjectConfig(IRepositoryIdProvider repoId){
		projConfigurations = fileHandler.loadProjectConfig(repoId);
	}

	public void saveSessionConfig(){
		fileHandler.saveSessionConfig(sessionConfigurations);
	}
	
	public void setFiltersForNextSession(IRepositoryIdProvider project, List<String> filter){
		sessionConfigurations.setFiltersForNextSession(project, filter);
	}

	/**
	 * Interface to configuration files
	 */

	/**
	 * Local configuration
	 */
	
	public HashMap<String, String> getUserAliases(){
		return localConfigurations.getUserAliases();
	}
	
	/**
	 * Project configuration
	 */

	public List<String> getStatusLabels(){
		return projConfigurations.getStatusLabels();
	}
	
	public boolean isNonInheritedLabel(String label) {
		return projConfigurations.isNonInheritedLabel(label);
	}
	
	public boolean isStatusLabel(String label){
		return projConfigurations.isStatusLabel(label);
	}
	
	public boolean isOpenStatusLabel(String label){
		return projConfigurations.isOpenStatusLabel(label);
	}
	
	public boolean isClosedStatusLabel(String label){
		return projConfigurations.isClosedStatusLabel(label);
	}

	/**
	 * Session configuration
	 */

	public List<String> getFiltersFromPreviousSession(IRepositoryIdProvider project) {
		return sessionConfigurations.getFiltersFromPreviousSession(project);
	}
	
	public void addToLastViewedRepositories(String repository){
		sessionConfigurations.addToLastViewedRepositories(repository);
	}
	
	public List<String> getLastViewedRepositories(){
		return sessionConfigurations.getLastViewedRepositories();
	}
}
