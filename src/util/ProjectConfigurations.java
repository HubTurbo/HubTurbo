package util;

import java.util.Collections;
import java.util.List;

public class ProjectConfigurations {
	
	private static List<String> nonInheritedLabels;
	public List<String> getNonInheritedLabels() {
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
	
	ProjectConfigurations(List<String> nonInheritedLabels, 
			List<String> openStatusLabels, List<String> closedStatusLabels) {
		ProjectConfigurations.nonInheritedLabels = nonInheritedLabels;
		ProjectConfigurations.openStatusLabels = openStatusLabels;
		ProjectConfigurations.closedStatusLabels = closedStatusLabels;
	}
	
	public static boolean isNonInheritedLabel(String label) {
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
