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
	
	private static List<String> closedStatusLabels;
	public List<String> getclosedStatusLabels() {
		return Collections.unmodifiableList(closedStatusLabels);
	}
	
	UserConfigurations(List<String> openStatuses, List<String> excludedLabels, List<String> closedStatusLabels) {
		UserConfigurations.openStatuses = openStatuses;
		UserConfigurations.excludedLabels = excludedLabels;
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

	public static boolean isclosedStatusLabel(String label) {
		for (String closedLabel : closedStatusLabels) {
			if (label.equalsIgnoreCase(closedLabel)) {
				return true;
			}
		}
		return false;
	}

}
