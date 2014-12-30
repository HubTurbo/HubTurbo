package storage;

import java.util.HashMap;

/**
 * Abstractions for the contents of the project configuration file.
 */
public class LocalConfiguration {
	
	private HashMap<String, String> userAliases = new HashMap<>();
	
	public LocalConfiguration() {
	}

	public String getAlias(String user) {
		return userAliases.get(user);
	}
}
