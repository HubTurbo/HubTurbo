package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;

public class SessionConfigurations {
	private static HashMap<String, List<String>> projectFilters;
	private static List<RepoViewRecord> lastViewedRepositories; 
	
	SessionConfigurations() {
		SessionConfigurations.projectFilters = new HashMap<String, List<String>>();
		SessionConfigurations.lastViewedRepositories = new ArrayList<RepoViewRecord>();
	}
	
	SessionConfigurations(HashMap<String, List<String>> projectFilters,
			List<RepoViewRecord> lastViewedRepositories) {
		SessionConfigurations.projectFilters = projectFilters;
		SessionConfigurations.lastViewedRepositories = lastViewedRepositories;
	}
	
	public static void setFiltersForNextSession(IRepositoryIdProvider project, List<String> filter) {
		projectFilters.put(project.generateId(), filter);
	}
	
	public static List<String> getFiltersFromPreviousSession(IRepositoryIdProvider project) {
		System.out.println(project);
		return projectFilters.get(project.generateId());
	}
	
	public static void addToLastViewedRepositories(String repository) {
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
	}
	
	public static List<String> getLastViewedRepositories() {
		List<String> list = new ArrayList<String>();
		for (RepoViewRecord repoViewRecord : lastViewedRepositories) {
			list.add(repoViewRecord.getRepository());
		}
		return list;
	}
	

}
