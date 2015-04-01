package storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstractions for the contents of the project configuration file.
 */
public class LocalConfiguration {
	
	private Map<String, String> userAliases = new HashMap<>();
	private Map<String, Map<String, List<String>>> boards = new HashMap<>();
	
	public LocalConfiguration() {
	}

	public String getAlias(String user) {
		return userAliases.get(user);
	}
	
	public void addBoard(String repo, String name, List<String> filterExprs) {
		if (!boards.containsKey(repo)) {
			boards.put(repo, new HashMap<>());
		}
		boards.get(repo).put(name, filterExprs);
	}
	
	public List<String> getBoardPanels(String repo, String name) {
		return boards.get(repo).get(name);
	}

	public Map<String, List<String>> getAllBoards(String repo) {
		if (!boards.containsKey(repo)) {
			boards.put(repo, new HashMap<>());
		}
		return boards.get(repo);
	}

	public void removeBoard(String repo, String name) {
		assert boards.containsKey(repo);
		boards.get(repo).remove(name);
	}
}
