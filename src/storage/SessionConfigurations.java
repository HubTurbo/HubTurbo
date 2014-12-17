package storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;

public class SessionConfigurations {
	private HashMap<String, List<String>> projectFilters;
	private List<RepoViewRecord> lastViewedRepositories; 
	
	public SessionConfigurations() {
		projectFilters = new HashMap<String, List<String>>();
		lastViewedRepositories = new ArrayList<RepoViewRecord>();
	}
	
	public SessionConfigurations(HashMap<String, List<String>> projectFilters,
			List<RepoViewRecord> lastViewedRepositories) {
		this.projectFilters = projectFilters;
		this.lastViewedRepositories = lastViewedRepositories;
	}
	
	public void setFiltersForNextSession(IRepositoryIdProvider project, List<String> filter) {
		if (project != null) {
			projectFilters.put(project.generateId(), filter);
		}
	}
	
	public List<String> getFiltersFromPreviousSession(IRepositoryIdProvider project) {
		if (project == null) {
			return new ArrayList<>();
		}
		return projectFilters.get(project.generateId());
	}
	
	public List<String> addToLastViewedRepositories(String repository) {
		RepoViewRecord latestRepoView = new RepoViewRecord(repository);
		int index = lastViewedRepositories.indexOf(latestRepoView);
		if (index < 0) {
			lastViewedRepositories.add(latestRepoView);
		} else {
			lastViewedRepositories.get(index).setTimestamp(latestRepoView.getTimestamp());
		}
		
		Collections.sort(lastViewedRepositories);
		while (lastViewedRepositories.size() > 10) {
			lastViewedRepositories.remove(lastViewedRepositories.size() - 1);
		}
		
		return getLastViewedRepositories();
	}
	
	public List<String> getLastViewedRepositories() {
		List<String> list = new ArrayList<String>();
		for (RepoViewRecord repoViewRecord : lastViewedRepositories) {
			list.add(repoViewRecord.getRepository());
		}
		return list;
	}
	

}
