package service;

import java.util.HashMap;
import java.util.Map;

/**
 * Models an event that could happen to an issue.
 */
public class TurboIssueEvent {
	private IssueEventType type;
	private Map<String, String> properties;

	public TurboIssueEvent(IssueEventType type) {
		this.type = type;
		this.properties = new HashMap<String, String>();
	}
	public IssueEventType getType() {
		return type;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}
