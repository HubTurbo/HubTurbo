package util;

import java.util.Collections;
import java.util.List;

public class UserConfigurations {
	private static List<String> openStatuses;
	public List<String> getOpenStatuses() {
		return Collections.unmodifiableList(openStatuses);
	}
	
	private static List<String> excludedLabels;
	public List<String> getExcludedLabels() {
		return Collections.unmodifiableList(excludedLabels);
	}
	
	private static List<String> openStatusLabels;
	public List<String> getOpenStatusLabels() {
		return Collections.unmodifiableList(openStatusLabels);
	}
	
	private static List<String> closedStatusLabels;
	public List<String> getClosedStatusLabels() {
		return Collections.unmodifiableList(closedStatusLabels);
	}
	
	UserConfigurations(List<String> openStatuses, List<String> excludedLabels, 
			List<String> openStatusLabels, List<String> closedStatusLabels) {
		UserConfigurations.openStatuses = openStatuses;
		UserConfigurations.excludedLabels = excludedLabels;
		UserConfigurations.openStatusLabels = openStatusLabels;
		UserConfigurations.closedStatusLabels = closedStatusLabels;
	}

	public static boolean isOpenStatus(String status) {
		return openStatuses.contains(status);
	}
	
	public static boolean isExcludedLabel(String label) {
		for (String excluded : excludedLabels) {
			if (label.contains(excluded)) {
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
