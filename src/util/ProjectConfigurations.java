package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectConfigurations {
	
	private static List<String> nonInheritedLabels;
	
	private static List<String> openStatusLabels;
	private static List<String> getOpenStatusLabels() {
		return Collections.unmodifiableList(openStatusLabels);
	}
	
	private static List<String> closedStatusLabels;
	private static List<String> getClosedStatusLabels() {
		return Collections.unmodifiableList(closedStatusLabels);
	}
	
	public static List<String> getStatusLabels() {
		List<String> statusLabels = new ArrayList<String>();
		statusLabels.addAll(getOpenStatusLabels());
		statusLabels.addAll(getClosedStatusLabels());
		return statusLabels;
	}
	
	ProjectConfigurations(List<String> nonInheritedLabels, 
			List<String> openStatusLabels, List<String> closedStatusLabels) {
		ProjectConfigurations.nonInheritedLabels = nonInheritedLabels;
		ProjectConfigurations.openStatusLabels = openStatusLabels;
		ProjectConfigurations.closedStatusLabels = closedStatusLabels;
	}
	
	public static boolean isNonInheritedLabel(String label) {
		assert nonInheritedLabels != null;
		for (String nonInherited : nonInheritedLabels) {
			if (label.contains(nonInherited)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isStatusLabel(String label){
		return isOpenStatusLabel(label) || isClosedStatusLabel(label);
	}

	public static boolean isOpenStatusLabel(String label) {
		assert openStatusLabels != null;
		for (String openLabel : openStatusLabels) {
			if (label.equalsIgnoreCase(openLabel)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isClosedStatusLabel(String label) {
		assert closedStatusLabels != null;
		for (String closedLabel : closedStatusLabels) {
			if (label.equalsIgnoreCase(closedLabel)) {
				return true;
			}
		}
		return false;
	}

}
