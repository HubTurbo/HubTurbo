package util;

import java.util.HashMap;

public class LocalConfigurations {
	
	private HashMap<String, String> userAliases = new HashMap<>();
	
	LocalConfigurations() {
	}

	public HashMap<String, String> getUserAliases() {
		return userAliases;
	}
}
