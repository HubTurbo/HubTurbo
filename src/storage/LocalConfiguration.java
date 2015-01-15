package storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstractions for the contents of the project configuration file.
 */
public class LocalConfiguration {
	
	private Map<String, String> userAliases = new HashMap<>();
	private Map<String, Map<String, List<String>>> panelSets = new HashMap<>();
	
	public LocalConfiguration() {
	}

	public String getAlias(String user) {
		return userAliases.get(user);
	}
	
	public void addPanelSet(String repo, String name, List<String> filterExprs) {
		if (!panelSets.containsKey(repo)) {
			panelSets.put(repo, new HashMap<>());
		}
		panelSets.get(repo).put(name, filterExprs);
	}
	
	public List<String> getPanelSet(String repo, String name) {
		return panelSets.get(repo).get(name);
	}

	public Map<String, List<String>> getAllPanelSets(String repo) {
		return panelSets.get(repo);
	}

	public void removePanelSet(String repo, String name) {
		assert panelSets.containsKey(repo);
		panelSets.get(repo).remove(name);
	}
}
