package util;

import java.util.HashMap;
import java.util.List;

import org.eclipse.egit.github.core.IRepositoryIdProvider;

public class SessionConfigurations {
	private static HashMap<String, List<String>> projectFilters;
	
	SessionConfigurations(HashMap<String, List<String>> projectFilters) {
		SessionConfigurations.projectFilters = projectFilters;
	}
	
	public static void setFiltersForNextSession(IRepositoryIdProvider project, List<String> filter) {
		projectFilters.put(project.generateId(), filter);
	}
	
	public static List<String> getFiltersFromPreviousSession(IRepositoryIdProvider project) {
		return projectFilters.get(project.generateId());
	}

}
