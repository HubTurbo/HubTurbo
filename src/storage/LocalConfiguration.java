package storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstractions for the contents of the project configuration file.
 */
public class LocalConfiguration {
	
	private Map<String, String> userAliases = new HashMap<>();
	private Map<String, List<String>> panelSets = new HashMap<>();
	
	public LocalConfiguration() {
	}

	public String getAlias(String user) {
		return userAliases.get(user);
	}
	
	public void addPanelSet(String name, List<String> filterExprs) {
		panelSets.put(name, filterExprs);
	}
	
	public List<String> getPanelSet(String name) {
		return panelSets.get(name);
	}

	public Map<String, List<String>> getAllPanelSets() {
		return panelSets;
	}
}
