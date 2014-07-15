package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Defaults {
	
	public static List<String> getDefaultStatusLabels() {
		List<String> defaultStatuses = new ArrayList<String>();
		defaultStatuses.addAll(getDefaultOpenStatusLabels());
		defaultStatuses.addAll(getDefaultClosedStatusLabels());
		return defaultStatuses;
	}
	
	public static List<String> getDefaultOpenStatusLabels() {
		List<String> defaultOpenStatuses = new ArrayList<String>();
		defaultOpenStatuses.add("status.new");
		defaultOpenStatuses.add("status.accepted");
		defaultOpenStatuses.add("status.started");
		defaultOpenStatuses.add("status.reopened");
		return defaultOpenStatuses;
	}

	public static List<String> getDefaultClosedStatusLabels() {
		List<String> defaultClosedStatuses = new ArrayList<String>();
		defaultClosedStatuses.add("status.closed");
		defaultClosedStatuses.add("status.fixed");
		defaultClosedStatuses.add("status.verified");
		defaultClosedStatuses.add("status.invalid");
		defaultClosedStatuses.add("status.duplicate");
		defaultClosedStatuses.add("status.wontfix");
		defaultClosedStatuses.add("status.done");
		return defaultClosedStatuses;
	}

	public static List<String> getDefaultNonInheritedLabels() {
		List<String> defaultNonInheritedLabels = new ArrayList<String>();
		defaultNonInheritedLabels.add("status.");
		return defaultNonInheritedLabels;
	}
}
