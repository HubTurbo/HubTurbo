package util;

import java.util.HashMap;

public class LocalConfigurations {
	
	private static LocalConfigurations instance = null;
	
	public static LocalConfigurations getInstance() {
		if (instance == null) {
			instance = ConfigFileHandler.loadLocalConfig();
		}
		return instance;
	}
	
	private HashMap<String, String> userAliases = new HashMap<>();
	
	LocalConfigurations() {
	}

	public HashMap<String, String> getUserAliases() {
		return userAliases;
	}
}
