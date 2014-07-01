package util;

import java.util.Collections;
import java.util.List;

public class UserConfigurations {
	private List<String> openStatuses;
	public List<String> getOpenStatuses() {
		return Collections.unmodifiableList(openStatuses);
	}
	
	private List<String> inheritedLabels;
	public List<String> getInheritedLabels() {
		return Collections.unmodifiableList(inheritedLabels);
	}
	
	public UserConfigurations(List<String> openStatuses, List<String> inheritedLabels) {
		this.openStatuses = openStatuses;
		this.inheritedLabels = inheritedLabels;
	}
	
	public boolean isOpenStatus(String status) {
		return openStatuses.contains(status);
	}
	
	public boolean isInheritedLabel(String label) {
		return openStatuses.contains(label);
	}
	
}
