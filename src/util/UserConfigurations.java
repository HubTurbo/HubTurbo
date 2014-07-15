package util;

import java.util.Collections;
import java.util.List;

public class UserConfigurations {
	
	private static List<String> nonInheritedLabels;
	public List<String> getExcludedLabels() {
		return Collections.unmodifiableList(nonInheritedLabels);
	}
	
	private static List<String> openStatusLabels;
	public List<String> getOpenStatusLabels() {
		return Collections.unmodifiableList(openStatusLabels);
	}
	
	private static List<String> closedStatusLabels;
	public List<String> getClosedStatusLabels() {
		return Collections.unmodifiableList(closedStatusLabels);
	}
	
	UserConfigurations(List<String> excludedLabels, 
			List<String> openStatusLabels, List<String> closedStatusLabels) {
		UserConfigurations.nonInheritedLabels = excludedLabels;
		UserConfigurations.openStatusLabels = openStatusLabels;
		UserConfigurations.closedStatusLabels = closedStatusLabels;
		boolean isModified = false;
		if (UserConfigurations.nonInheritedLabels.isEmpty()) {
			UserConfigurations.nonInheritedLabels.addAll(Defaults.getDefaultNonInheritedLabels());
			isModified = true;
		}
		if (UserConfigurations.openStatusLabels.isEmpty()) {
			UserConfigurations.openStatusLabels.addAll(Defaults.getDefaultOpenStatusLabels());
			isModified = true;
		}
		if (UserConfigurations.closedStatusLabels.isEmpty()) {
			UserConfigurations.closedStatusLabels.addAll(Defaults.getDefaultClosedStatusLabels());
			isModified = true;
		}
		if (isModified) {
			ConfigFileHandler.saveConfig(this);
		}
	}
	
	public static boolean isNonInheritedLabel(String label) {
		for (String nonInherited : nonInheritedLabels) {
			if (label.contains(nonInherited)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isOpenStatusLabel(String label) {
		for (String openLabel : openStatusLabels) {
			if (label.equalsIgnoreCase(openLabel)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isClosedStatusLabel(String label) {
		for (String closedLabel : closedStatusLabels) {
			if (label.equalsIgnoreCase(closedLabel)) {
				return true;
			}
		}
		return false;
	}

}
