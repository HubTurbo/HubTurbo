package util;

import java.util.Collections;
import java.util.List;

public class UserConfigurations {
	private List<String> openStatuses;
	public List<String> getOpenStatuses() {
		return Collections.unmodifiableList(openStatuses);
	}
	
	private List<String> excludedLabels;
	public List<String> getExcludedLabels() {
		return Collections.unmodifiableList(excludedLabels);
	}
	
	public UserConfigurations(List<String> openStatuses, List<String> excludedLabels) {
		this.openStatuses = openStatuses;
		this.excludedLabels = excludedLabels;
	}

	boolean isOpenStatus(String status) {
		return openStatuses.contains(status);
	}
	
	boolean isExcludedLabel(String label) {
		return excludedLabels.contains(label);
	}
	
}
