package storage;

import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;

public class DataManager {
	private static final DataManager dataManagerInstance = new DataManager();
	private ConfigFileHandler fileHandler;
	private SessionConfigurations sessionConfigurations;
	private ProjectConfigurations projConfigurations;
	private LocalConfigurations localConfigurations;
	
	protected DataManager(){
		fileHandler = new ConfigFileHandler();
		sessionConfigurations = fileHandler.loadSessionConfig();
		localConfigurations = fileHandler.loadLocalConfig();
		projConfigurations = new ProjectConfigurations();
		
	}
	
	public static DataManager getInstance(){
		return dataManagerInstance;
	}
	
	public void setFiltersForNextSession(IRepositoryIdProvider project, List<String> filter){
		sessionConfigurations.setFiltersForNextSession(project, filter);
	}
	
	public List<String> getFiltersFromPreviousSession(IRepositoryIdProvider project) {
		return sessionConfigurations.getFiltersFromPreviousSession(project);
	}
	
	public List<String> addToLastViewedRepositories(String repository){
		return sessionConfigurations.addToLastViewedRepositories(repository);
	}
	
	public List<String> getLastViewedRepositories(){
		return sessionConfigurations.getLastViewedRepositories();
	}
	
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
	
	public void loadProjectConfig(IRepositoryIdProvider repoId){
		projConfigurations = fileHandler.loadProjectConfig(repoId);
	}
	
	public void saveSessionConfig(){
		fileHandler.saveSessionConfig(sessionConfigurations);
	}
	
	public void saveLocalConfig(){
		fileHandler.saveLocalConfig(localConfigurations);
	}
	
	public HashMap<String, String> getUserAliases(){
		return localConfigurations.getUserAliases();
	}
}
